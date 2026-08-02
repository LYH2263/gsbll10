<template>
  <BaseModal :model-value="modelValue" :title="isEdit ? '编辑相册' : '新建相册'" @update:model-value="v => emit('update:modelValue', v)">
    <div class="space-y-4">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">相册名称</label>
        <input
          v-model="form.name"
          type="text"
          maxlength="50"
          placeholder="请输入相册名称（1~50字符）"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
        />
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">简介（可选）</label>
        <textarea
          v-model="form.description"
          maxlength="200"
          rows="3"
          placeholder="请输入相册简介（最多200字符）"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm resize-none"
        ></textarea>
      </div>
    </div>
    <div class="mt-6 flex space-x-3">
      <button
        @click="cancel"
        class="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium"
      >
        取消
      </button>
      <button
        @click="submit"
        class="flex-1 px-4 py-2 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg hover:shadow-lg transition font-medium"
      >
        {{ isEdit ? '保存' : '创建' }}
      </button>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref, watch } from 'vue'
import BaseModal from './BaseModal.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  album: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const form = ref({ name: '', description: '' })

const isEdit = () => !!props.album

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      form.value = {
        name: props.album?.name || '',
        description: props.album?.description || ''
      }
    }
  }
)

const cancel = () => emit('update:modelValue', false)

const submit = () => {
  const name = form.value.name.trim()
  if (!name) {
    emit('submit', { error: '相册名不能为空' })
    return
  }
  if (name.length > 50) {
    emit('submit', { error: '相册名长度不能超过50个字符' })
    return
  }
  if (form.value.description && form.value.description.length > 200) {
    emit('submit', { error: '简介长度不能超过200个字符' })
    return
  }
  emit('submit', {
    value: { name, description: form.value.description.trim() || null }
  })
}
</script>
