<template>
  <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center px-4">
    <div class="absolute inset-0 bg-black bg-opacity-40 backdrop-blur-sm" @click="close"></div>
    <div class="relative bg-white rounded-2xl shadow-2xl max-w-md w-full p-6 transform transition-all">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-medium text-gray-900">{{ title }}</h3>
        <button @click="close" class="text-gray-400 hover:text-gray-600 focus:outline-none">
          <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
          </svg>
        </button>
      </div>
      <slot></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const close = () => emit('update:modelValue', false)
</script>
