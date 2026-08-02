<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center space-x-4">
        <button
          @click="$emit('back')"
          class="p-2 hover:bg-gray-100 rounded-full transition"
        >
          <svg class="w-5 h-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <div>
          <h2 class="text-2xl font-bold text-gray-800">{{ album?.name }}</h2>
          <p v-if="album?.description" class="text-sm text-gray-500 mt-1">{{ album.description }}</p>
        </div>
      </div>
      <div class="flex items-center space-x-3">
        <button
          v-if="localPictures.length > 0"
          @click="toggleSelectAll"
          class="px-3 py-2 text-gray-600 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition text-sm font-medium"
        >
          {{ allSelected ? '取消全选' : '全选' }}
        </button>
        <button
          @click="$emit('addPictures')"
          class="px-4 py-2 bg-blue-500 text-white rounded-full shadow hover:shadow-lg hover:bg-blue-600 transition text-sm font-medium flex items-center space-x-1"
        >
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          <span>加入图片</span>
        </button>
        <button
          v-if="selectedIds.length > 0"
          @click="clearSelection"
          class="px-3 py-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition text-sm font-medium"
        >
          取消选择
        </button>
        <button
          v-if="selectedIds.length > 0"
          @click="$emit('batchRemove', [...selectedIds])"
          class="px-4 py-2 bg-red-500 text-white rounded-full shadow hover:shadow-lg hover:bg-red-600 transition text-sm font-medium"
        >
          移出选中（{{ selectedIds.length }}）
        </button>
      </div>
    </div>

    <transition
      enter-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-200"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div v-if="saving" class="mb-4 flex items-center text-sm text-blue-500">
        <svg class="animate-spin h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
        </svg>
        正在保存顺序...
      </div>
    </transition>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
    </div>

    <div v-else-if="!album || album.pictures.length === 0" class="text-center py-20">
      <div class="text-6xl mb-4">🏞️</div>
      <h3 class="text-xl font-medium text-gray-500">该相册暂无图片</h3>
      <p class="text-gray-400 mt-2">点击右上角"加入图片"添加图片到相册</p>
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      <div
        v-for="(pic, index) in localPictures"
        :key="pic.id"
        class="group relative bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden"
        :class="{
          'ring-2 ring-yellow-400 shadow-yellow-100': album.coverPictureId === pic.id,
          'ring-2 ring-blue-500': selectedIds.includes(pic.id) && album.coverPictureId !== pic.id
        }"
      >
        <div class="h-64 bg-gray-100 relative overflow-hidden" @click="toggleSelect(pic.id)">
          <img
            :src="pic.url"
            class="object-cover w-full h-full transform group-hover:scale-105 transition duration-500"
            loading="lazy"
          />
          <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-20 transition duration-300"></div>

          <div class="absolute top-2 left-2 flex items-center space-x-1">
            <div
              class="w-6 h-6 rounded-full border-2 border-white flex items-center justify-center transition cursor-pointer"
              :class="selectedIds.includes(pic.id) ? 'bg-blue-500' : 'bg-white bg-opacity-50'"
            >
              <svg v-if="selectedIds.includes(pic.id)" class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
            </div>
          </div>

          <div v-if="album.coverPictureId === pic.id" class="absolute top-2 right-2">
            <span class="px-2 py-1 bg-yellow-400 text-yellow-900 text-xs rounded-full font-medium flex items-center space-x-1 shadow">
              <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L12 2z"/>
              </svg>
              <span>封面</span>
            </span>
          </div>

          <div class="absolute right-2 top-1/2 -translate-y-1/2 flex flex-col space-y-1 opacity-0 group-hover:opacity-100 transition duration-200" @click.stop>
            <button
              @click="moveUp(index)"
              :disabled="index === 0 || saving"
              class="p-1.5 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-blue-600 shadow-lg transition disabled:opacity-30 disabled:cursor-not-allowed"
              title="上移"
            >
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7" />
              </svg>
            </button>
            <button
              @click="moveDown(index)"
              :disabled="index === localPictures.length - 1 || saving"
              class="p-1.5 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-blue-600 shadow-lg transition disabled:opacity-30 disabled:cursor-not-allowed"
              title="下移"
            >
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </button>
          </div>

          <div class="absolute bottom-2 left-2 right-2 flex items-center justify-center space-x-2 opacity-0 group-hover:opacity-100 transition duration-300" @click.stop>
            <a
              :href="pic.url"
              download
              target="_blank"
              class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-blue-600 shadow-lg transition transform hover:scale-110"
              title="下载"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
            </a>
            <button
              v-if="album.coverPictureId !== pic.id"
              @click="$emit('setCover', pic.id)"
              class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-yellow-600 shadow-lg transition transform hover:scale-110"
              title="设为封面"
            >
              <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
              </svg>
            </button>
            <button
              v-else
              @click="$emit('clearCover')"
              class="p-2 bg-yellow-400 bg-opacity-90 rounded-full text-yellow-900 hover:bg-yellow-300 shadow-lg transition transform hover:scale-110"
              title="取消手动封面（恢复自动）"
            >
              <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
              </svg>
            </button>
            <button
              @click="$emit('remove', pic.id)"
              class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-red-600 shadow-lg transition transform hover:scale-110"
              title="移出相册"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <div class="p-4 h-20 flex flex-col justify-center">
          <h3 class="text-sm font-medium text-gray-800 truncate">{{ pic.name }}</h3>
          <p class="text-xs text-gray-400 mt-1">{{ formatTime(pic.createTime) }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { updateAlbumOrder } from '../api/album'

const props = defineProps({
  album: { type: Object, default: null },
  loading: Boolean
})

const emit = defineEmits(['back', 'addPictures', 'remove', 'setCover', 'clearCover', 'batchRemove', 'reordered'])

const selectedIds = ref([])
const localPictures = ref([])
const saving = ref(false)
let lastPictureCount = 0

const allSelected = computed(() => {
  if (localPictures.value.length === 0) return false
  return localPictures.value.every(p => selectedIds.value.includes(p.id))
})

watch(() => props.album, (newAlbum) => {
  if (newAlbum && newAlbum.pictures) {
    localPictures.value = [...newAlbum.pictures]
    if (newAlbum.pictures.length < lastPictureCount) {
      selectedIds.value = []
    }
    lastPictureCount = newAlbum.pictures.length
  }
}, { immediate: true, deep: true })

watch(() => props.album?.id, () => {
  selectedIds.value = []
})

const toggleSelectAll = () => {
  if (allSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = localPictures.value.map(p => p.id)
  }
}

const clearSelection = () => {
  selectedIds.value = []
}

const toggleSelect = (id) => {
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const saveOrder = async () => {
  if (!props.album) return
  saving.value = true
  const ids = localPictures.value.map(p => p.id)
  try {
    const res = await updateAlbumOrder(props.album.id, ids)
    if (res.code === 0) {
      emit('reordered')
    }
  } catch (e) {
    console.error(e)
  } finally {
    setTimeout(() => { saving.value = false }, 300)
  }
}

const moveUp = (index) => {
  if (index === 0 || saving.value) return
  const arr = [...localPictures.value]
  const temp = arr[index - 1]
  arr[index - 1] = arr[index]
  arr[index] = temp
  localPictures.value = arr
  saveOrder()
}

const moveDown = (index) => {
  if (index === localPictures.value.length - 1 || saving.value) return
  const arr = [...localPictures.value]
  const temp = arr[index + 1]
  arr[index + 1] = arr[index]
  arr[index] = temp
  localPictures.value = arr
  saveOrder()
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>
