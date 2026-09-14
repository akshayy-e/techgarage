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
  getChangeRequests: (id) => unwrap(api.get(`/jobs/${id}/change-requests`)),
  createChangeRequest: (id, payload) => unwrap(api.post(`/jobs/${id}/change-requests`, payload)),
  respondToChangeRequest: (changeRequestId, accept) => unwrap(api.put(`/jobs/change-requests/${changeRequestId}/respond`, null, { params: { accept } })),
}
