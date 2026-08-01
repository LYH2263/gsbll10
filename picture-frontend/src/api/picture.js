import request from './request'

// 现有图片接口（保持原有形态，不做包裹）
export function listPictures() {
  return request.get('/list')
}

export function uploadPicture(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deletePicture(id) {
  return request.delete(`/delete/${id}`)
}
