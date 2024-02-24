import axios from 'axios'

export const api = axios.create({baseURL: '/api'})
export const auth = axios.create({baseURL: '/auth'})