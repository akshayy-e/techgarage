import api, { unwrap } from './api'

export const messageService = {
  getForJob: (jobId) => unwrap(api.get(`/jobs/${jobId}/messages`)),
  send: (jobId, payload) => unwrap(api.post(`/jobs/${jobId}/messages`, payload)),
}
