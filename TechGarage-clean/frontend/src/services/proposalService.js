import api, { unwrap } from './api'

export const proposalService = {
  submit: (problemId, payload) => unwrap(api.post(`/problems/${problemId}/proposals`, payload)),
  getForProblem: (problemId) => unwrap(api.get(`/problems/${problemId}/proposals`)),
  getMine: () => unwrap(api.get('/proposals/mine')),
  accept: (id) => unwrap(api.put(`/proposals/${id}/accept`)),
  reject: (id) => unwrap(api.put(`/proposals/${id}/reject`)),
}
