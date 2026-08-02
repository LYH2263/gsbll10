import { reactive } from 'vue'

// 全局共享的 toast 状态，供各视图/组件复用现有 toast 风格。
const toast = reactive({
  show: false,
  message: '',
  type: 'success'
})

let timer = null

export function showToast(message, type = 'success') {
  toast.show = true
  toast.message = message
  toast.type = type
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    toast.show = false
  }, 3000)
}

export function useToast() {
  return { toast, showToast }
}
