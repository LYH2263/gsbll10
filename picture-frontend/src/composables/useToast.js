import { reactive } from 'vue'

export const toastState = reactive({
  show: false,
  message: '',
  type: 'success'
})

let timer = null

export const showToast = (message, type = 'success') => {
  toastState.message = message
  toastState.type = type
  toastState.show = true
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    toastState.show = false
  }, 2500)
}
