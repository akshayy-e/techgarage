import api, { unwrap } from './api'

export const authService = {
  register: (payload) => unwrap(api.post('/auth/register', payload)),
  login: (payload) => unwrap(api.post('/auth/login', payload)),
  forgotPassword: (payload) => unwrap(api.post('/auth/forgot-password', payload)),
  resetPassword: (payload) => unwrap(api.post('/auth/reset-password', payload)),
}
