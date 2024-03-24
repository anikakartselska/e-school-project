import axios from 'axios'
import {setupApiInterceptorsTo} from "../interceptors/ApiInterceptors";
import {QVueGlobals} from "quasar";

const api = axios.create({baseURL: '/api'})
const auth = axios.create({baseURL: '/auth'})

export const setUpAxios = (quasar: QVueGlobals) => setupApiInterceptorsTo([api, auth], quasar);

export {axios, api, auth}

// export const setUpAxios = (quasar: QVueGlobals) => setupApiInterceptorsTo([api, auth], quasar);
//
// export {axios, api}