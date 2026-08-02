<template>
  <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center px-4">
    <div class="absolute inset-0 bg-black bg-opacity-40 backdrop-filter backdrop-blur-sm transition-opacity" @click="$emit('cancel')"></div>
    <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6 relative transform transition-all scale-100">
      <h3 class="text-lg font-medium text-gray-900 mb-4">{{ mode === 'create' ? '新建相册' : '编辑相册' }}</h3>
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">相册名称</label>
          <input
            v-model="form.name"
            type="text"
            maxlength="50"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition"
            placeholder="请输入相册名称"
            @keyup.enter="handleSubmit"
          />
          <p v-if="error" class="text-sm text-red-500 mt-1">{{ error }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">简介（可选）</label>
          <textarea
            v-model="form.description"
            maxlength="200"
            rows="3"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition resize-none"
            placeholder="请输入相册简介"
          ></textarea>
        </div>
      </div>
      <div class="mt-6 flex space-x-3">
        <button
          @click="$emit('cancel')"
          class="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium focus:outline-none"
        >
          取消
        </button>
        <button
          @click="handleSubmit"
          class="flex-1 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition font-medium shadow-md focus:outline-none"
        >
          {{ mode === 'create' ? '创建' : '保存' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  visible: Boolean,
  mode: { type: String, default: 'create' },
  album: { type: Object, default: null }
})

const emit = defineEmits(['submit', 'cancel'])

const form = ref({ name: '', description: '' })
const error = ref('')

watch(() => props.visible, (val) => {
  if (val) {
    error.value = ''
    if (props.mode === 'edit' && props.album) {
      form.value = {
        name: props.album.name || '',
        description: props.album.description || ''
      }
    } else {
      form.value = { name: '', description: '' }
    }
  }
})

const handleSubmit = () => {
  const name = form.value.name.trim()
  if (!name) {
    error.value = '相册名不能为空'
    return
  }
  if (name.length > 50) {
    error.value = '相册名不能超过50个字符'
    return
  }
  error.value = ''
  emit('submit', {
    name: name,
    description: form.value.description || ''
  })
}
</script>
