import api, { unwrap } from './api'

export const paymentService = {
  createOrder: (jobId) => unwrap(api.post(`/payments/jobs/${jobId}/order`)),
  verify: (payload) => unwrap(api.post('/payments/verify', payload)),
  mine: () => unwrap(api.get('/payments/mine')),
}

export async function loadRazorpay() {
  if (window.Razorpay) return true
  await new Promise((resolve, reject) => {
    const existing = document.querySelector('script[data-razorpay="checkout"]')
    if (existing) { existing.addEventListener('load', resolve, { once: true }); existing.addEventListener('error', reject, { once: true }); return }
    const script = document.createElement('script')
    script.src = 'https://checkout.razorpay.com/v1/checkout.js'
    script.async = true
    script.dataset.razorpay = 'checkout'
    script.onload = resolve
    script.onerror = () => reject(new Error('Unable to load the payment gateway'))
    document.body.appendChild(script)
  })
  return Boolean(window.Razorpay)
}
