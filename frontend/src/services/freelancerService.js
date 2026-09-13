import api, { unwrap } from './api'

export const freelancerService = {
  getAll: (params = {}) => unwrap(api.get('/freelancers', { params })),
  getMine: () => unwrap(api.get('/freelancers/me')),
  getByUserId: (userId) => unwrap(api.get(`/freelancers/${userId}`)),
  updateProfile: (payload) => unwrap(api.put('/freelancers/me', payload)),
}
