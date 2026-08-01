import http from './http'

// 图库（图片）相关请求。沿用现有 /upload、/list、/delete 形态（未改造为包裹格式）。

export function listPictures() {
  return http.get('/list')
}

export function uploadPicture(file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deletePicture(id) {
  return http.delete(`/delete/${id}`)
}
