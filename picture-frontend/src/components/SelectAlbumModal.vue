<template>
  <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center px-4">
    <div class="absolute inset-0 bg-black bg-opacity-40 backdrop-filter backdrop-blur-sm transition-opacity" @click="$emit('cancel')"></div>
    <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full max-h-[80vh] flex flex-col relative">
      <div class="p-6 border-b border-gray-100">
        <h3 class="text-lg font-medium text-gray-900">加入相册</h3>
        <p class="text-sm text-gray-500 mt-1">选择要将图片加入的相册（可多选）</p>
      </div>
      <div class="flex-1 overflow-y-auto p-6">
        <div v-if="albums.length === 0" class="text-center py-10 text-gray-400">
          <div class="text-4xl mb-2">📁</div>
          <p>暂无相册，请先创建相册</p>
        </div>
        <div v-else class="space-y-2">
          <label
            v-for="album in albums"
            :key="album.id"
            class="flex items-center p-3 rounded-lg cursor-pointer hover:bg-gray-50 transition border"
            :class="selectedIds.includes(album.id) ? 'border-blue-500 bg-blue-50' : 'border-gray-200'"
          >
            <input
              type="checkbox"
              :checked="selectedIds.includes(album.id)"
              @change="toggleSelect(album.id)"
              class="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
            />
            <div class="ml-3 flex-1 min-w-0">
              <div class="flex items-center space-x-2">
                <span class="text-sm font-medium text-gray-800 truncate">{{ album.name }}</span>
                <span class="text-xs text-gray-400">{{ album.pictureCount }} 张</span>
              </div>
              <p v-if="album.description" class="text-xs text-gray-400 truncate mt-0.5">{{ album.description }}</p>
            </div>
          </label>
        </div>
      </div>
      <div class="p-6 border-t border-gray-100 flex space-x-3">
        <button
          @click="$emit('cancel')"
          class="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium"
        >
          取消
        </button>
        <button
          @click="handleConfirm"
          :disabled="selectedIds.length === 0"
          class="flex-1 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition font-medium shadow-md disabled:opacity-50 disabled:cursor-not-allowed"
        >
          确认（{{ selectedIds.length }}）
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  visible: Boolean,
  albums: { type: Array, default: () => [] }
})

const emit = defineEmits(['confirm', 'cancel'])

const selectedIds = ref([])

watch(() => props.visible, (val) => {
  if (val) {
    selectedIds.value = []
  }
})

const toggleSelect = (id) => {
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const handleConfirm = () => {
  emit('confirm', [...selectedIds.value])
}
</script>
