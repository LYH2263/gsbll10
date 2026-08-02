<template>
  <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center px-4">
    <div class="absolute inset-0 bg-black bg-opacity-40 backdrop-filter backdrop-blur-sm transition-opacity" @click="$emit('cancel')"></div>
    <div class="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6 relative transform transition-all scale-100">
      <div class="text-center">
        <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full" :class="type === 'danger' ? 'bg-red-100' : 'bg-blue-100'">
          <svg class="h-6 w-6" :class="type === 'danger' ? 'text-red-600' : 'text-blue-600'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <h3 class="text-lg leading-6 font-medium text-gray-900 mt-4">{{ title }}</h3>
        <p class="text-sm text-gray-500 mt-2">{{ message }}</p>
      </div>
      <div class="mt-6 flex space-x-3">
        <button
          @click="$emit('cancel')"
          class="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium focus:outline-none"
        >
          {{ cancelText }}
        </button>
        <button
          @click="$emit('confirm')"
          class="flex-1 px-4 py-2 text-white rounded-lg transition font-medium shadow-md focus:outline-none"
          :class="type === 'danger' ? 'bg-red-500 hover:bg-red-600' : 'bg-blue-500 hover:bg-blue-600'"
        >
          {{ confirmText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  visible: Boolean,
  title: { type: String, default: '确认操作' },
  message: { type: String, default: '' },
  confirmText: { type: String, default: '确认' },
  cancelText: { type: String, default: '取消' },
  type: { type: String, default: 'danger' }
})

defineEmits(['confirm', 'cancel'])
</script>
