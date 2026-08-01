import axios from 'axios'

// 统一的请求实例：所有对后端的 HTTP 请求都收敛到 src/api 下（工程规范 §3）。
// baseURL 沿用现有 /api 前缀，经 vite 代理 / nginx 转发到后端 8140。
const http = axios.create({
  baseURL: '/api'
})

export default http
