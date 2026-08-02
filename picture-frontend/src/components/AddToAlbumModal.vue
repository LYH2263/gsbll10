<template>
  <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center px-4">
    <div class="absolute inset-0 bg-black bg-opacity-40 backdrop-filter backdrop-blur-sm transition-opacity" @click="$emit('cancel')"></div>
    <div class="bg-white rounded-2xl shadow-2xl max-w-3xl w-full max-h-[80vh] flex flex-col relative">
      <div class="p-6 border-b border-gray-100">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-medium text-gray-900">选择图片加入相册</h3>
            <p class="text-sm text-gray-500 mt-1">
              已选 <span class="text-blue-600 font-medium">{{ selectedIds.length }}</span> 张
              <span v-if="existingCount > 0" class="text-gray-400"> · {{ existingCount }} 张已在相册中（将被忽略）</span>
            </p>
          </div>
          <button
            v-if="selectableCount > 0"
            @click="toggleSelectAll"
            class="text-sm text-blue-600 hover:text-blue-700 font-medium"
          >
            {{ allSelected ? '取消全选' : '全选' }}
          </button>
        </div>
      </div>
      <div class="flex-1 overflow-y-auto p-6">
        <div v-if="pictures.length === 0" class="text-center py-10 text-gray-400">
          图库中暂无图片
        </div>
        <div v-else class="grid grid-cols-3 sm:grid-cols-4 gap-3">
          <div
            v-for="pic in pictures"
            :key="pic.id"
            @click="toggleSelect(pic.id)"
            class="relative cursor-pointer rounded-lg overflow-hidden border-2 transition-all"
            :class="cardClass(pic.id)"
          >
            <div class="aspect-square bg-gray-100 relative">
              <img
                :src="pic.url"
                class="w-full h-full object-cover transition"
                :class="{ 'opacity-40': isExisting(pic.id) }"
                loading="lazy"
              />
              <div
                v-if="isExisting(pic.id)"
                class="absolute inset-0 flex items-center justify-center"
              >
                <span class="px-2 py-1 bg-gray-800 bg-opacity-60 text-white text-xs rounded-full">已在相册</span>
              </div>
            </div>
            <div
              v-if="selectedIds.includes(pic.id)"
              class="absolute top-1 right-1 w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center"
            >
              <svg class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
            </div>
          </div>
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
          加入相册（{{ selectedIds.length }}）
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  visible: Boolean,
  pictures: { type: Array, default: () => [] },
  existingPictureIds: { type: Array, default: () => [] }
})

const emit = defineEmits(['confirm', 'cancel'])

const selectedIds = ref([])

const existingSet = computed(() => new Set(props.existingPictureIds))

const existingCount = computed(() => {
  return props.pictures.filter(p => existingSet.value.has(p.id)).length
})

const selectablePictures = computed(() => {
  return props.pictures.filter(p => !existingSet.value.has(p.id))
})

const selectableCount = computed(() => selectablePictures.value.length)

const allSelected = computed(() => {
  if (selectableCount.value === 0) return false
  return selectablePictures.value.every(p => selectedIds.value.includes(p.id))
})

watch(() => props.visible, (val) => {
  if (val) {
    selectedIds.value = []
  }
})

const isExisting = (id) => existingSet.value.has(id)

const cardClass = (id) => {
  if (isExisting(id)) {
    return 'border-gray-200 opacity-60 cursor-not-allowed'
  }
  return selectedIds.value.includes(id)
    ? 'border-blue-500 ring-2 ring-blue-200'
    : 'border-transparent hover:border-gray-300'
}

const toggleSelect = (id) => {
  if (isExisting(id)) return
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const toggleSelectAll = () => {
  if (allSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = selectablePictures.value.map(p => p.id)
  }
}

const handleConfirm = () => {
  emit('confirm', [...selectedIds.value])
}
</script>
