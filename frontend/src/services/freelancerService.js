import api, { unwrap } from './api'

export const freelancerService = {
  getAll: () => unwrap(api.get('/freelancers')),
  getMine: () => unwrap(api.get('/freelancers/me')),
  getByUserId: (userId) => unwrap(api.get(`/freelancers/${userId}`)),
  updateProfile: (payload) => unwrap(api.put('/freelancers/me', payload)),
}
