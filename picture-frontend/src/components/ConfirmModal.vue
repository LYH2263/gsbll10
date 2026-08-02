<template>
  <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center px-4">
    <div
      class="absolute inset-0 bg-black bg-opacity-40 backdrop-filter backdrop-blur-sm transition-opacity"
      @click="cancel"
    ></div>
    <div class="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6 relative transform transition-all">
      <div class="text-center">
        <div
          class="mx-auto flex items-center justify-center h-12 w-12 rounded-full mb-4"
          :class="danger ? 'bg-red-100' : 'bg-blue-100'"
        >
          <svg
            v-if="danger"
            class="h-6 w-6 text-red-600"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <svg
            v-else
            class="h-6 w-6 text-blue-600"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <h3 class="text-lg leading-6 font-medium text-gray-900">{{ title }}</h3>
        <p class="text-sm text-gray-500 mt-2 whitespace-pre-line">{{ message }}</p>
      </div>
      <div class="mt-6 flex space-x-3">
        <button
          @click="cancel"
          class="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium focus:outline-none"
        >
          {{ cancelText }}
        </button>
        <button
          @click="confirm"
          class="flex-1 px-4 py-2 text-white rounded-lg transition font-medium shadow-md focus:outline-none"
          :class="danger ? 'bg-red-500 hover:bg-red-600' : 'bg-blue-500 hover:bg-blue-600'"
        >
          {{ confirmText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '请确认' },
  message: { type: String, default: '' },
  confirmText: { type: String, default: '确定' },
  cancelText: { type: String, default: '取消' },
  danger: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const cancel = () => {
  emit('update:modelValue', false)
  emit('cancel')
}

const confirm = () => {
  emit('update:modelValue', false)
  emit('confirm')
}
</script>
