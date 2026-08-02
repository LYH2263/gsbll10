import request from './request'

export const listAlbums = () => request.get('/album/list')

export const getAlbum = (id) => request.get(`/album/${id}`)

export const createAlbum = (data) => request.post('/album', data)

export const updateAlbum = (id, data) => request.put(`/album/${id}`, data)

export const deleteAlbum = (id) => request.delete(`/album/${id}`)

export const addPictureToAlbum = (id, pictureId) =>
  request.post(`/album/${id}/pictures`, { pictureId })

export const removePictureFromAlbum = (id, pictureId) =>
  request.delete(`/album/${id}/pictures/${pictureId}`)

export const batchAddPictures = (id, pictureIds) =>
  request.post(`/album/${id}/pictures/batch`, { pictureIds })

export const batchRemovePictures = (id, pictureIds) =>
  request.delete(`/album/${id}/pictures/batch`, { data: { pictureIds } })

export const setAlbumCover = (id, pictureId) =>
  request.put(`/album/${id}/cover`, { pictureId })

export const clearAlbumCover = (id) =>
  request.put(`/album/${id}/cover`, { pictureId: null })

export const updateAlbumOrder = (id, pictureIds) =>
  request.put(`/album/${id}/order`, { pictureIds })
