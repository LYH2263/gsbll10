import request from './request'

// 相册相关接口，统一返回 { code, message, data }，拦截器已解包 data
export function createAlbum(payload) {
  return request.post('/album', payload)
}

export function listAlbums() {
  return request.get('/album/list')
}

export function getAlbumDetail(id) {
  return request.get(`/album/${id}`)
}

export function updateAlbum(id, payload) {
  return request.put(`/album/${id}`, payload)
}

export function deleteAlbum(id) {
  return request.delete(`/album/${id}`)
}

export function addPictureToAlbum(albumId, pictureId) {
  return request.post(`/album/${albumId}/pictures`, { pictureId })
}

export function removePictureFromAlbum(albumId, pictureId) {
  return request.delete(`/album/${albumId}/pictures/${pictureId}`)
}

// pictureId 传 null 表示取消手动封面，回退为"最近加入的成员"
export function setAlbumCover(albumId, pictureId) {
  return request.put(`/album/${albumId}/cover`, { pictureId })
}

export function updateAlbumOrder(albumId, pictureIds) {
  return request.put(`/album/${albumId}/order`, { pictureIds })
}

// 批量加入：返回 { requested, succeeded, ignored, failed } 部分成功结构
export function batchAddPictures(albumId, pictureIds) {
  return request.post(`/album/${albumId}/pictures/batch`, { pictureIds })
}

// 批量移出：DELETE 携带 body
export function batchRemovePictures(albumId, pictureIds) {
  return request.delete(`/album/${albumId}/pictures/batch`, { data: { pictureIds } })
}
