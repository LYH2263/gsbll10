import http from './http'

// 相册相关请求，统一收敛于此（工程规范 §3）。
// 相册接口返回 { code, message, data } 包裹结构；此处返回原始 axios response，
// 由调用方通过 unwrap 处理 code。

// 新建相册
export function createAlbum(payload) {
  return http.post('/album', payload)
}

// 相册列表（含计算封面、图片数量、更新时间）
export function listAlbums() {
  return http.get('/album/list')
}

// 相册详情（相册信息 + 该相册内图片，按 sortOrder）
export function getAlbumDetail(id) {
  return http.get(`/album/${id}`)
}

// 重命名 / 改简介
export function updateAlbum(id, payload) {
  return http.put(`/album/${id}`, payload)
}

// 删除相册（仅删相册与成员关系，不删图片）
export function deleteAlbum(id) {
  return http.delete(`/album/${id}`)
}

// 加入单张图片
export function addPictureToAlbum(id, pictureId) {
  return http.post(`/album/${id}/pictures`, { pictureId })
}

// 移出单张图片（仅解关系）
export function removePictureFromAlbum(id, pictureId) {
  return http.delete(`/album/${id}/pictures/${pictureId}`)
}

// 手动设置封面（pictureId 必须是该相册成员）
export function setAlbumCover(id, pictureId) {
  return http.put(`/album/${id}/cover`, { pictureId })
}

// 取消手动封面（回退为最近加入）
export function clearAlbumCover(id) {
  return http.delete(`/album/${id}/cover`)
}

// 更新相册内顺序（pictureIds 为期望顺序）
export function updateAlbumOrder(id, pictureIds) {
  return http.put(`/album/${id}/order`, { pictureIds })
}

// 批量加入图片（部分成功语义）
export function batchAddToAlbum(id, pictureIds) {
  return http.post(`/album/${id}/pictures/batch`, { pictureIds })
}

// 批量移出图片（部分成功语义，仅解关系）
export function batchRemoveFromAlbum(id, pictureIds) {
  return http.delete(`/album/${id}/pictures/batch`, { data: { pictureIds } })
}

// 解包 { code, message, data }：成功返回 data，失败抛出可读中文错误
export function unwrap(res) {
  const body = res.data
  if (body && body.code === 0) {
    return body.data
  }
  const message = (body && body.message) ? body.message : '操作失败'
  throw new Error(message)
}

// 把批处理结果汇总成可读中文提示（部分成功语义）
export function summarizeBatch(result, verb) {
  const ok = result.succeeded ? result.succeeded.length : 0
  const ignored = result.ignored ? result.ignored.length : 0
  const failed = result.failed ? result.failed.length : 0
  const parts = []
  parts.push(`${verb}成功 ${ok} 张`)
  if (ignored > 0) parts.push(`忽略 ${ignored} 张`)
  if (failed > 0) parts.push(`失败 ${failed} 张`)
  return parts.join('，')
}
