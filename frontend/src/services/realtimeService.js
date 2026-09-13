import { API_ORIGIN } from './api'

// Uses fetch instead of EventSource so the JWT stays in the Authorization header
// rather than appearing in the URL, access logs, browser history, or referrers.
export async function subscribeToNotifications(onNotification, signal) {
  const token = localStorage.getItem('tg_token')
  if (!token) return
  const response = await fetch(`${API_ORIGIN}/api/notifications/stream`, {
    headers: { Authorization: `Bearer ${token}`, Accept: 'text/event-stream' },
    signal,
  })
  if (!response.ok || !response.body) throw new Error('Notification stream unavailable')
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  while (!signal?.aborted) {
    const { value, done } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const events = buffer.split('\n\n')
    buffer = events.pop() || ''
    for (const block of events) {
      const data = block.split('\n').filter(line => line.startsWith('data:')).map(line => line.slice(5).trim()).join('\n')
      const event = block.split('\n').find(line => line.startsWith('event:'))?.slice(6).trim()
      if (event === 'notification' && data) {
        try { onNotification(JSON.parse(data)) } catch { /* ignore malformed server events */ }
      }
    }
  }
}
