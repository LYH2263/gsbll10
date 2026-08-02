<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">我的相册</h2>
        <p class="text-sm text-gray-400 mt-1">共 {{ albums.length }} 个相册 · 删除相册不会删除原图</p>
      </div>
      <button
        @click="openCreate"
        class="px-5 py-2 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-full shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 transition duration-200 flex items-center space-x-2 text-sm font-medium"
      >
        <span>新建相册</span>
        <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
        </svg>
      </button>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
    </div>

    <div v-else-if="albums.length === 0" class="text-center py-20">
      <div class="text-6xl mb-4">📂</div>
      <h3 class="text-xl font-medium text-gray-500">还没有相册，点击右上角创建吧</h3>
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      <div
        v-for="album in albums"
        :key="album.id"
        class="group bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden cursor-pointer"
        @click="openDetail(album)"
      >
        <div class="h-48 bg-gray-100 relative overflow-hidden">
          <img
            v-if="album.cover"
            :src="album.cover.url"
            class="object-cover w-full h-full transform group-hover:scale-105 transition duration-500"
            loading="lazy"
          />
          <div v-else class="w-full h-full flex items-center justify-center text-gray-300">
            <svg class="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
          <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-10 transition duration-300"></div>

          <div class="absolute top-3 right-3 flex space-x-2 opacity-0 group-hover:opacity-100 transition duration-300" @click.stop>
            <button
              @click="openEdit(album)"
              class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-blue-600 shadow-lg transition transform hover:scale-110"
              title="重命名"
            >
              <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </button>
            <button
              @click="confirmDelete(album)"
              class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-red-600 shadow-lg transition transform hover:scale-110"
              title="删除相册"
            >
              <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>

        <div class="p-4">
          <h3 class="text-base font-semibold text-gray-800 truncate">{{ album.name }}</h3>
          <p v-if="album.description" class="text-xs text-gray-400 mt-1 truncate">{{ album.description }}</p>
          <div class="flex items-center justify-between mt-3 text-xs text-gray-400">
            <span>{{ album.pictureCount }} 张图片</span>
            <span>{{ formatTime(album.updateTime) }}</span>
          </div>
        </div>
      </div>
    </div>

    <AlbumFormModal
      v-model="showForm"
      :album="editingAlbum"
      @submit="handleSubmit"
    />

    <ConfirmModal
      v-model="showDeleteModal"
      title="删除相册?"
      :message="`确定要删除相册「${albumToDelete?.name}」吗？\n该相册中的成员关系会被解除，但图片文件和图库中的图片记录不会被删除。`"
      confirm-text="删除相册"
      :danger="true"
      @confirm="doDelete"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listAlbums, createAlbum, updateAlbum, deleteAlbum } from '../api/album'
import { showToast } from '../composables/useToast'
import { formatTime } from '../utils/format'
import AlbumFormModal from '../components/AlbumFormModal.vue'
import ConfirmModal from '../components/ConfirmModal.vue'

const emit = defineEmits(['open-detail'])

const albums = ref([])
const loading = ref(false)
const showForm = ref(false)
const editingAlbum = ref(null)
const showDeleteModal = ref(false)
const albumToDelete = ref(null)

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

const openCreate = () => {
  editingAlbum.value = null
  showForm.value = true
}

const openEdit = (album) => {
  editingAlbum.value = album
  showForm.value = true
}

const handleSubmit = async (payload) => {
  if (payload.error) {
    showToast(payload.error, 'error')
    return
  }
  try {
    if (editingAlbum.value) {
      await updateAlbum(editingAlbum.value.id, payload.value)
      showToast('相册已更新')
    } else {
      await createAlbum(payload.value)
      showToast('相册已创建')
    }
    showForm.value = false
    fetchAlbums()
  } catch (e) {
    showToast(e.message || '操作失败', 'error')
  }
}

const confirmDelete = (album) => {
  albumToDelete.value = album
  showDeleteModal.value = true
}

const doDelete = async () => {
  if (!albumToDelete.value) return
  try {
    await deleteAlbum(albumToDelete.value.id)
    showToast('相册已删除')
    albumToDelete.value = null
    fetchAlbums()
  } catch (e) {
    showToast(e.message || '删除失败', 'error')
  }
}

const openDetail = (album) => {
  emit('open-detail', album.id)
}

onMounted(fetchAlbums)
</script>
