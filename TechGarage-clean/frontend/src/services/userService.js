import api, { unwrap } from './api'

export const userService = {
  getMine: () => unwrap(api.get('/users/me')),
  update: (payload) => unwrap(api.put('/users/me', payload)),
}
