import request from './request'

export const listPictures = () => request.get('/list')

export const uploadPicture = (formData) =>
  request.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

export const deletePicture = (id) => request.delete(`/delete/${id}`)
