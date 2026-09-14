import api, { unwrap } from './api'

export const notificationService = {
  getMine: () => unwrap(api.get('/notifications')),
  unreadCount: () => unwrap(api.get('/notifications/unread-count')),
  markAsRead: (id) => unwrap(api.put(`/notifications/${id}/read`)),
  clearAll: () => unwrap(api.delete('/notifications')),
}
