<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center px-4">
    <div class="absolute inset-0 bg-black bg-opacity-40 backdrop-filter backdrop-blur-sm" @click="$emit('close')"></div>
    <div class="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6 relative">
      <h3 class="text-lg font-medium text-gray-900 mb-1">加入相册</h3>
      <p class="text-xs text-gray-400 mb-4 truncate">
        {{ pictures.length > 1 ? `已选中 ${pictures.length} 张图片` : (pictures[0] ? pictures[0].name : '') }}
      </p>

      <div v-if="loading" class="flex justify-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-500"></div>
      </div>
      <div v-else-if="albums.length === 0" class="text-center py-8 text-sm text-gray-400">
        暂无相册，请先在「相册」页新建。
      </div>
      <div v-else class="max-h-64 overflow-y-auto space-y-2">
        <button
          v-for="album in albums"
          :key="album.id"
          @click="addTo(album)"
          :disabled="addingId === album.id"
          class="w-full flex items-center space-x-3 p-2 rounded-xl hover:bg-purple-50 transition text-left disabled:opacity-50"
        >
          <div class="w-12 h-12 rounded-lg bg-gray-100 overflow-hidden flex-shrink-0">
            <img v-if="album.cover" :src="album.cover.url" class="object-cover w-full h-full" loading="lazy" />
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-gray-800 truncate">{{ album.name }}</p>
            <p class="text-xs text-gray-400">{{ album.pictureCount }} 张</p>
          </div>
        </button>
      </div>

      <button @click="$emit('close')" class="mt-5 w-full px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium">关闭</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { listAlbums, batchAddPictures } from '../api/album'

const props = defineProps({ pictures: { type: Array, required: true } })
const emit = defineEmits(['close'])
const showToast = inject('showToast')

const albums = ref([])
const loading = ref(false)
const addingId = ref(null)

const fetchAlbums = async () => {
  loading.value = true
  try {
    albums.value = await listAlbums()
  } catch (e) {
    showToast(e.message || '加载相册失败', 'error')
  } finally {
    loading.value = false
  }
}

const addTo = async (album) => {
  addingId.value = album.id
  try {
    const result = await batchAddPictures(album.id, props.pictures.map(p => p.id))
    const okCount = result.succeeded.length
    const ignoredCount = result.ignored.length
    const failedCount = result.failed.length
    if (failedCount > 0) {
      showToast(`加入相册「${album.name}」：成功 ${okCount} 张，忽略 ${ignoredCount} 张，失败 ${failedCount} 张`, 'error')
    } else if (okCount === 0 && ignoredCount > 0) {
      showToast(`所选图片已在相册「${album.name}」中`)
    } else {
      showToast(`已加入相册「${album.name}」${okCount} 张` + (ignoredCount > 0 ? `，${ignoredCount} 张已存在被忽略` : ''))
    }
    emit('close')
  } catch (e) {
    showToast(e.message || '加入失败', 'error')
  } finally {
    addingId.value = null
  }
}

onMounted(fetchAlbums)
</script>
