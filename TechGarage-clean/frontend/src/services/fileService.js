import api, { unwrap } from './api'

export const fileService = {
  upload: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return unwrap(api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }))
  },
}
