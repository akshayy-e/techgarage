import axios from 'axios'

const configuredApiBase = (import.meta.env.VITE_API_BASE_URL || '').trim()
const API_BASE_URL = (configuredApiBase || (import.meta.env.DEV ? 'http://localhost:8080/api' : '/api')).replace(/\/+$/, '')

export const API_ORIGIN = API_BASE_URL.replace(/\/api\/?$/, '')

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
  timeout: 8000,
  // Keep API calls snappy; GET requests are deduplicated and briefly cached in-memory.
  transitional: { clarifyTimeoutError: true },
})



const GET_CACHE_TTL = 5000
const getCache = new Map()
const getPending = new Map()

const originalGet = api.get.bind(api)
api.get = (url, config = {}) => {
  const params = config.params || {}
  const key = `${url}?${new URLSearchParams(Object.entries(params).filter(([, v]) => v !== undefined && v !== null)).toString()}`
  const now = Date.now()
  const cached = getCache.get(key)
  if (cached && now - cached.time < GET_CACHE_TTL) return Promise.resolve(cached.response)
  if (getPending.has(key)) return getPending.get(key)

  const request = originalGet(url, config).then((response) => {
    getCache.set(key, { time: Date.now(), response })
    getPending.delete(key)
    return response
  }).catch((error) => {
    getPending.delete(key)
    throw error
  })
  getPending.set(key, request)
  return request
}

export const clearApiCache = () => {
  getCache.clear()
}

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('tg_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('tg_token')
      localStorage.removeItem('tg_user')
      clearApiCache()
      if (!window.location.pathname.startsWith('/login')) window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const unwrap = (promise) =>
  promise.then((res) => res.data.data).catch((err) => {
    const message = err.response?.data?.message || err.message || 'Something went wrong'
    throw new Error(message)
  })

export const resolveFileUrl = (path) => {
  if (!path) return null
  if (/^https?:\/\//i.test(path)) return path
  return `${API_ORIGIN}${path.startsWith('/') ? path : `/${path}`}`
}

export default api
