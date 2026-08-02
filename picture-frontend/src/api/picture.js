import axios from 'axios'

const api = axios.create({
  baseURL: '/api'
})

export const fetchPictures = () => api.get('/list').then(res => res.data)

export const uploadPicture = (formData) =>
  api.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(res => res.data)

export const deletePicture = (id) =>
  api.delete(`/delete/${id}`).then(res => res.data)
