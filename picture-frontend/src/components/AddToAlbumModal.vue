<template>
  <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center px-4">
    <div class="absolute inset-0 bg-black bg-opacity-40 backdrop-filter backdrop-blur-sm" @click="close(false)"></div>
    <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6 relative">
      <h3 class="text-lg font-medium text-gray-900 mb-1">加入相册</h3>
      <p class="text-sm text-gray-500 mb-4 truncate">
        <template v-if="isBatch">已选 {{ pictureIds.length }} 张图片</template>
        <template v-else>图片：{{ picture ? picture.name : '' }}</template>
      </p>

      <div v-if="loading" class="flex justify-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
      </div>

      <div v-else-if="albums.length === 0" class="text-center py-8 text-gray-400 text-sm">
        还没有相册，请先在"相册"页新建一个。
      </div>

      <div v-else class="max-h-72 overflow-y-auto space-y-2">
        <button
          v-for="album in albums"
          :key="album.id"
          @click="add(album)"
          :disabled="busyId === album.id"
          class="w-full flex items-center justify-between px-4 py-3 rounded-xl border border-gray-100 hover:border-purple-300 hover:bg-purple-50 transition text-left disabled:opacity-50"
        >
          <div class="min-w-0">
            <p class="text-sm font-medium text-gray-800 truncate">{{ album.name }}</p>
            <p class="text-xs text-gray-400">{{ album.pictureCount }} 张图片</p>
          </div>
          <svg class="h-5 w-5 text-purple-500 flex-shrink-0" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
        </button>
      </div>

      <div class="mt-6 flex justify-end">
        <button @click="close(false)" class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { listAlbums, addPictureToAlbum, batchAddToAlbum, unwrap, summarizeBatch } from '../api/album'
import { showToast } from '../composables/useToast'

const props = defineProps({
  visible: { type: Boolean, default: false },
  picture: { type: Object, default: null },
  pictureIds: { type: Array, default: () => [] }
})
const emit = defineEmits(['close'])

const albums = ref([])
const loading = ref(false)
const busyId = ref(null)

const isBatch = computed(() => props.pictureIds && props.pictureIds.length > 0)

const fetchAlbums = async () => {
  loading.value = true
  try {
    albums.value = unwrap(await listAlbums())
  } catch (e) {
    console.error(e)
    showToast(e.message || '加载相册失败', 'error')
  } finally {
    loading.value = false
  }
}

const add = async (album) => {
  busyId.value = album.id
  try {
    if (isBatch.value) {
      const result = unwrap(await batchAddToAlbum(album.id, props.pictureIds))
      showToast(`「${album.name}」：${summarizeBatch(result, '加入')}`)
      close(true)
    } else {
      if (!props.picture) return
      unwrap(await addPictureToAlbum(album.id, props.picture.id))
      showToast(`已加入相册「${album.name}」`)
      close(true)
    }
  } catch (e) {
    console.error(e)
    showToast(e.message || '加入失败', 'error')
  } finally {
    busyId.value = null
  }
}

const close = (done) => emit('close', done)

watch(() => props.visible, (v) => {
  if (v) fetchAlbums()
})
</script>
