import api, { unwrap } from './api'

export const jobService = {
  getMine: () => unwrap(api.get('/jobs')),
  getById: (id) => unwrap(api.get(`/jobs/${id}`)),
  updateStatus: (id, payload) => unwrap(api.put(`/jobs/${id}/status`, payload)),
  submitSolution: (id, payload) => unwrap(api.post(`/jobs/${id}/submit-solution`, payload)),
  requestRevision: (id, payload) => unwrap(api.post(`/jobs/${id}/revision`, payload)),
  complete: (id) => unwrap(api.post(`/jobs/${id}/complete`)),
  cancel: (id) => unwrap(api.post(`/jobs/${id}/cancel`)),
  raiseDispute: (id, payload) => unwrap(api.post(`/jobs/${id}/disputes`, payload)),
}
