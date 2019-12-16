import { stringify } from "query-string";
import map from "lodash/map";
import every from "lodash/every";
import {
  fetchUtils,
  GET_LIST,
  GET_ONE,
  GET_MANY,
  GET_MANY_REFERENCE,
  CREATE,
  UPDATE,
  UPDATE_MANY,
  DELETE,
  DELETE_MANY
} from "ra-core";

const convertFileToBase64 = file =>
  new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);

    reader.onload = () => resolve(reader.result);
    reader.onerror = reject;
  });

/**
 * Maps react-admin queries to a simple REST API
 *
 * The REST dialect is similar to the one of FakeRest
 * @see https://github.com/marmelab/FakeRest
 * @example
 * GET_LIST     => GET http://my.api.url/posts?sort=['title','ASC']&range=[0, 24]
 * GET_ONE      => GET http://my.api.url/posts/123
 * GET_MANY     => GET http://my.api.url/posts?filter={ids:[123,456,789]}
 * UPDATE       => PUT http://my.api.url/posts/123
 * CREATE       => POST http://my.api.url/posts
 * DELETE       => DELETE http://my.api.url/posts/123
 */
export default (apiUrl, httpClient = fetchUtils.fetchJson) => {
  /**
   * @param {String} type One of the constants appearing at the top if this file, e.g. 'UPDATE'
   * @param {String} resource Name of the resource to fetch, e.g. 'posts'
   * @param {Object} params The data request params, depending on the type
   * @returns {Object} { url, options } The HTTP request parameters
   */
  const convertDataRequestToHTTP = async (type, resource, params) => {
    let url = "";
    const options = {};
    switch (type) {
      case GET_LIST: {
        const { page, perPage } = params.pagination;

        const { field, order: orderDirection } = params.sort;

        const query = {
          orderField: field,
          orderDir: orderDirection || "asc",
          limit: perPage,
          offset: (page - 1) * perPage
          // search: params.filter.q || "",
        };

        url = `${apiUrl}/${resource}/?${stringify(query)}`;

        break;
      }

      case GET_ONE:
        url = `${apiUrl}/${resource}/${params.id}/`;
        console.log("[DATAPROVIDER][GET_ONE] url =", url);

        break;

      case GET_MANY: {
        const query = {
          filter: JSON.stringify({
            id: params.ids
          })
        };
        url = `${apiUrl}/${resource}/?${stringify(query)}`;
        break;
      }

      case GET_MANY_REFERENCE: {
        const { page, perPage } = params.pagination;

        const { field, order } = params.sort;

        const query = {
          sort: JSON.stringify([field, order]),
          range: JSON.stringify([(page - 1) * perPage, page * perPage - 1]),
          filter: JSON.stringify({
            ...params.filter,
            [params.target]: params.id
          })
        };
        url = `${apiUrl}/${resource}/?${stringify(query)}`;

        break;
      }

      case UPDATE:
      case CREATE:
        if (resource === "products" && params.data.image_data != null) {
          const blob = await fetch(params.data.image_data.uri).then(r =>
            r.blob()
          );
          let data = await convertFileToBase64(blob);
          data = data.replace(new RegExp("data:image/[a-z]*;base64,"), "");
          params.data.image_data = data;
        }
        url = `${apiUrl}/${resource}/`;
        options.method = "POST";
        options.body = JSON.stringify(params.data);
        break;

      case DELETE:
        url = `${apiUrl}/${resource}/${params.id}/`;
        options.method = "DELETE";
        break;

      default:
        throw new Error(`Unsupported fetch action type ${type}`);
    }
    return {
      url,
      options
    };
  };

  /**
   * @param {Object} response HTTP response from fetch()
   * @param {String} type One of the constants appearing at the top if this file, e.g. 'UPDATE'
   * @param {String} resource Name of the resource to fetch, e.g. 'posts'
   * @param {Object} params The data request params, depending on the type
   * @returns {Object} Data response
   */
  const convertHTTPResponse = (response, type, resource, params) => {
    const { json } = response;
    switch (type) {
      case GET_ONE:
        if (resource === "products" && response.json.data.image_data != null) {
          response.json.data.image_data = {
            uri: "data:image/png;base64," + response.json.data.image_data
          };
        }
        return response.json;
      case GET_LIST:
        if (every(json.data, a => a.id != undefined)) {
          json.data = map(json.data, a => {
            a.id = a.id || a.phrase || a.code || a.internal_name;

            return a;
          });
        }
      case GET_MANY_REFERENCE:
        return {
          data: json.data,
          total: json.metadata.total
        };
      case CREATE: {
        return json;
      }
      case DELETE_MANY: {
        return json || [];
      }
      default:
        return json;
    }
  };

  /**
   * @param {string} type Request type, e.g GET_LIST
   * @param {string} resource Resource name, e.g. "posts"
   * @param {Object} payload Request parameters. Depends on the request type
   * @returns {Promise} the Promise for a data response
   */
  return async (type, resource, params) => {
    // simple-rest doesn't handle filters on UPDATE route, so we fallback to calling UPDATE n times instead
    if (type === UPDATE_MANY) {
      return Promise.all(
        params.ids.map(id =>
          httpClient(`${apiUrl}/${resource}/`, {
            method: "POST",
            body: JSON.stringify(params.data)
          })
        )
      ).then(responses => ({
        data: responses.map(response => response.json)
      }));
    }
    // simple-rest doesn't handle filters on DELETE route, so we fallback to calling DELETE n times instead
    if (type === DELETE_MANY) {
      return Promise.all(
        params.ids.map(id =>
          httpClient(`${apiUrl}/${resource}/${id}/`, {
            method: "DELETE"
          })
        )
      ).then(responses => ({
        data: responses.map(response => response.json)
      }));
    }

    const { url, options } = await convertDataRequestToHTTP(
      type,
      resource,
      params
    );
    return httpClient(url, options).then(response =>
      convertHTTPResponse(response, type, resource, params)
    );
  };
};
