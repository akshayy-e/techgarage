import api, { unwrap } from './api'

export const problemService = {
  getEmergency: () => unwrap(api.get('/problems/emergency')),
  create: (payload) => unwrap(api.post('/problems', payload)),
  aiSuggest: (payload) => unwrap(api.post('/problems/ai-suggest', payload)),
  getAllOpen: () => unwrap(api.get('/problems')),
  getMine: () => unwrap(api.get('/problems/mine')),
  getById: (id) => unwrap(api.get(`/problems/${id}`)),
  update: (id, payload) => unwrap(api.put(`/problems/${id}`, payload)),
  remove: (id) => unwrap(api.delete(`/problems/${id}`)),
}
