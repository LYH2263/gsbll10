import axios from 'axios'

// 统一的请求实例，所有后端请求都从这里发出
const request = axios.create({
  baseURL: '/api'
})

// 相册等接口使用 { code, message, data } 包裹结构：code !== 0 时抛出可读中文错误
request.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code !== 0) {
        return Promise.reject(new Error(body.message || '操作失败'))
      }
      return body.data
    }
    return body
  },
  (err) => Promise.reject(err)
)

export default request
