import axios from 'axios'

const api = axios.create({
  baseURL: '/api'
})

export const fetchAlbumList = () =>
  api.get('/album/list').then(res => res.data)

export const fetchAlbumDetail = (id) =>
  api.get(`/album/${id}`).then(res => res.data)

export const createAlbum = (data) =>
  api.post('/album', data).then(res => res.data)

export const updateAlbum = (id, data) =>
  api.put(`/album/${id}`, data).then(res => res.data)

export const deleteAlbum = (id) =>
  api.delete(`/album/${id}`).then(res => res.data)

export const addPictureToAlbum = (albumId, pictureId) =>
  api.post(`/album/${albumId}/pictures`, { pictureId }).then(res => res.data)

export const removePictureFromAlbum = (albumId, pictureId) =>
  api.delete(`/album/${albumId}/pictures/${pictureId}`).then(res => res.data)

export const setAlbumCover = (albumId, pictureId) =>
  api.put(`/album/${albumId}/cover`, { pictureId }).then(res => res.data)

export const updateAlbumOrder = (albumId, pictureIds) =>
  api.put(`/album/${albumId}/order`, { pictureIds }).then(res => res.data)

export const batchAddPictures = (albumId, pictureIds) =>
  api.post(`/album/${albumId}/pictures/batch`, { pictureIds }).then(res => res.data)

export const batchRemovePictures = (albumId, pictureIds) =>
  api.delete(`/album/${albumId}/pictures/batch`, { data: { pictureIds } }).then(res => res.data)
