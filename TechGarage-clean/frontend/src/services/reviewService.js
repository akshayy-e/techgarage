import api, { unwrap } from './api'

export const reviewService = {
  submit: (jobId, payload) => unwrap(api.post(`/jobs/${jobId}/review`, payload)),
  getForFreelancer: (freelancerId) => unwrap(api.get(`/freelancers/${freelancerId}/reviews`)),
}
