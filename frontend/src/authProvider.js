import {
  AUTH_CHECK,
  AUTH_ERROR,
  AUTH_GET_PERMISSIONS,
  AUTH_LOGIN,
  AUTH_LOGOUT
} from "react-admin";

export default (type, params) => {
  // called when the user attempts to log in
  if (type === AUTH_LOGIN) {
    const { username, password } = params;

    if (username != "admin" || password != "admin") {
      console.log("[auth] rejecting login");
      return Promise.reject();
    }

    sessionStorage.setItem("user", "admin");
    return Promise.resolve();
  }

  // called when the user clicks on the logout button
  if (type === AUTH_LOGOUT) {
    localStorage.removeItem("user");
    return Promise.resolve();
  }

  // called when the API returns an error
  if (type === AUTH_ERROR) {
    const { status } = params;

    if (status === 401) {
      console.log("[auth] rejecting viac auth error");
      localStorage.removeItem("user");
      return Promise.reject();
    }

    return Promise.resolve();
  }

  // called when the user navigates to a new location
  if (type === AUTH_CHECK) {
    let user = sessionStorage.getItem("user");
    console.log("[auth] auth check: ", !!user);

    return !!user ? Promise.resolve(true) : Promise.reject();
  }

  if (type === AUTH_GET_PERMISSIONS) {
    return Promise.resolve();
  }

  return Promise.reject("Unknown method");
};
