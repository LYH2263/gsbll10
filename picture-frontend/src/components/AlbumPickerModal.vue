<template>
  <BaseModal :model-value="modelValue" title="加入相册" @update:model-value="v => emit('update:modelValue', v)">
    <div v-if="loading" class="flex justify-center py-10">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
    </div>
    <div v-else-if="albums.length === 0" class="text-center py-10 text-gray-400 text-sm">
      暂无相册，请先在「相册」页面创建相册
    </div>
    <div v-else class="max-h-80 overflow-y-auto space-y-2">
      <div
        v-for="album in albums"
        :key="album.id"
        class="flex items-center justify-between p-3 rounded-lg border border-gray-100 hover:bg-gray-50"
      >
        <div class="flex items-center space-x-3 min-w-0">
          <div class="w-10 h-10 rounded-lg bg-gray-100 overflow-hidden flex-shrink-0">
            <img v-if="album.cover" :src="album.cover.url" class="w-full h-full object-cover" />
            <div v-else class="w-full h-full flex items-center justify-center text-gray-300">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
          </div>
          <div class="min-w-0">
            <p class="text-sm font-medium text-gray-800 truncate">{{ album.name }}</p>
            <p class="text-xs text-gray-400">{{ album.pictureCount }} 张图片</p>
          </div>
        </div>
        <button
          @click="select(album)"
          class="px-3 py-1 text-xs font-medium text-blue-600 bg-blue-50 rounded-full hover:bg-blue-100 transition flex-shrink-0"
        >
          加入
        </button>
      </div>
    </div>
    <div class="mt-6">
      <button
        @click="close"
        class="w-full px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium"
      >
        关闭
      </button>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref, watch } from 'vue'
import BaseModal from './BaseModal.vue'
import { listAlbums, addPictureToAlbum } from '../api/album'
import { showToast } from '../composables/useToast'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  pictureId: { type: Number, default: null }
})

const emit = defineEmits(['update:modelValue', 'joined'])

const albums = ref([])
const loading = ref(false)

watch(
  () => props.modelValue,
  async (val) => {
    if (val) {
      loading.value = true
      try {
        albums.value = await listAlbums()
      } catch (e) {
        showToast(e.message || '加载相册失败', 'error')
      } finally {
        loading.value = false
      }
    }
  }
)

const select = async (album) => {
  try {
    await addPictureToAlbum(album.id, props.pictureId)
    showToast(`已加入「${album.name}」`)
    emit('joined', album)
    close()
  } catch (e) {
    showToast(e.message || '加入失败', 'error')
  }
}

const close = () => emit('update:modelValue', false)
</script>
