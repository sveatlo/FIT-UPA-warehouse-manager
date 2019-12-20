import React from "react";
import { Admin, Resource } from "react-admin";
import { fetchUtils } from "ra-core";

import config from "./config";
import restDataProvider from "./dataProvider";
import fakeAuthProvider from "./authProvider";
// resources
import { categories, products, product_units } from "./resources";

const httpClient = (url, options = {}) => {
  if (!options.headers) {
    options.headers = new Headers({ Accept: "application/json" });
  }

  return fetchUtils.fetchJson(url, options);
};
const dataProvider = restDataProvider(config.apiUrl, httpClient);

const App = () => (
  <Admin dataProvider={dataProvider} authProvider={fakeAuthProvider}>
    <Resource name="categories" {...categories} />
    <Resource name="products" {...products} />
    <Resource name="product_units" {...product_units} />
  </Admin>
);
export default App;
