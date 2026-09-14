import api, { unwrap } from './api'

export const invitationService = {
  invite: (freelancerId, payload) => unwrap(api.post(`/invitations/freelancers/${freelancerId}`, payload)),
  getMine: () => unwrap(api.get('/invitations/mine')),
  getSent: () => unwrap(api.get('/invitations/sent')),
  respond: (id, accept) => unwrap(api.put(`/invitations/${id}/respond`, null, { params: { accept } })),
}
