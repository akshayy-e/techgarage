import api, { unwrap } from './api'

export const adminService = {
  getStats: () => unwrap(api.get('/admin/stats')),
  getUsers: () => unwrap(api.get('/admin/users')),
  getClients: () => unwrap(api.get('/admin/clients')),
  getFreelancers: () => unwrap(api.get('/admin/freelancers')),
  getProblems: () => unwrap(api.get('/admin/problems')),
  getJobs: () => unwrap(api.get('/admin/jobs')),
  getDisputes: () => unwrap(api.get('/admin/disputes')),
  resolveDispute: (id, payload) => unwrap(api.put(`/admin/disputes/${id}/resolve`, payload)),
  verifyFreelancer: (id) => unwrap(api.put(`/admin/freelancers/${id}/verify`)),
  suspendUser: (id) => unwrap(api.put(`/admin/users/${id}/suspend`)),
  reactivateUser: (id) => unwrap(api.put(`/admin/users/${id}/reactivate`)),
}
