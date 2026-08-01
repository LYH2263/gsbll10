<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-bold text-gray-800">我的相册</h2>
      <button
        @click="openCreateModal"
        class="px-4 py-2 bg-gradient-to-r from-purple-500 to-purple-600 text-white rounded-full shadow hover:shadow-lg transition text-sm font-medium"
      >
        新建相册
      </button>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500"></div>
    </div>

    <div v-else-if="albums.length === 0" class="text-center py-20">
      <div class="text-6xl mb-4">📁</div>
      <h3 class="text-xl font-medium text-gray-500">暂无相册，点击右上角新建吧！</h3>
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      <div
        v-for="album in albums"
        :key="album.id"
        class="group bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden cursor-pointer"
        @click="$emit('open', album.id)"
      >
        <div class="h-48 bg-gray-100 relative overflow-hidden">
          <img
            v-if="album.cover"
            :src="album.cover.url"
            class="object-cover w-full h-full transform group-hover:scale-105 transition duration-500"
            loading="lazy"
          />
          <div v-else class="w-full h-full flex items-center justify-center text-5xl text-gray-300">📷</div>
        </div>
        <div class="p-4">
          <div class="flex justify-between items-center">
            <h3 class="text-sm font-medium text-gray-800 truncate">{{ album.name }}</h3>
            <span class="text-xs text-gray-400 flex-shrink-0 ml-2">{{ album.pictureCount }} 张</span>
          </div>
          <p class="text-xs text-gray-400 mt-1">更新于 {{ formatTime(album.updateTime) }}</p>
          <div class="flex space-x-3 mt-3" @click.stop>
            <button @click="openEditModal(album)" class="text-xs text-blue-500 hover:text-blue-700">重命名</button>
            <button @click="openDeleteModal(album)" class="text-xs text-red-500 hover:text-red-700">删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建 / 重命名相册弹层 -->
    <div v-if="showEditModal" class="fixed inset-0 z-50 flex items-center justify-center px-4">
      <div class="absolute inset-0 bg-black bg-opacity-40 backdrop-filter backdrop-blur-sm" @click="closeEditModal"></div>
      <div class="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6 relative">
        <h3 class="text-lg font-medium text-gray-900 mb-4">{{ editingAlbum ? '重命名相册' : '新建相册' }}</h3>
        <input
          v-model="form.name"
          maxlength="50"
          placeholder="相册名（必填，50 字以内）"
          class="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-purple-400"
        />
        <p v-if="nameConflict" class="text-xs text-amber-500 mt-1">提示：已存在同名相册，仍可{{ editingAlbum ? '保存' : '创建' }}。</p>
        <textarea
          v-model="form.description"
          maxlength="200"
          rows="3"
          placeholder="简介（可选，200 字以内）"
          class="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm mt-3 focus:outline-none focus:ring-2 focus:ring-purple-400"
        ></textarea>
        <div class="mt-5 flex space-x-3">
          <button @click="closeEditModal" class="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium">取消</button>
          <button @click="submitEdit" class="flex-1 px-4 py-2 bg-purple-500 text-white rounded-lg hover:bg-purple-600 transition font-medium shadow-md">确定</button>
        </div>
      </div>
    </div>

    <!-- 删除相册确认弹层（自定义，不用 window.confirm） -->
    <div v-if="showDeleteModal" class="fixed inset-0 z-50 flex items-center justify-center px-4">
      <div class="absolute inset-0 bg-black bg-opacity-40 backdrop-filter backdrop-blur-sm" @click="showDeleteModal = false"></div>
      <div class="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6 relative">
        <div class="text-center">
          <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4">
            <svg class="h-6 w-6 text-red-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h3 class="text-lg leading-6 font-medium text-gray-900">删除相册「{{ deletingAlbum && deletingAlbum.name }}」?</h3>
          <p class="text-sm text-gray-500 mt-2">仅删除相册本身，相册中的图片仍会保留在图库中。</p>
        </div>
        <div class="mt-6 flex space-x-3">
          <button @click="showDeleteModal = false" class="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium">取消</button>
          <button @click="doDelete" class="flex-1 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition font-medium shadow-md">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { listAlbums, createAlbum, updateAlbum, deleteAlbum } from '../api/album'

const emit = defineEmits(['open'])
const showToast = inject('showToast')

const albums = ref([])
const loading = ref(false)

const showEditModal = ref(false)
const editingAlbum = ref(null)
const form = ref({ name: '', description: '' })

const showDeleteModal = ref(false)
const deletingAlbum = ref(null)

const nameConflict = computed(() => {
  const name = form.value.name.trim()
  if (!name) return false
  return albums.value.some(a => a.name === name && (!editingAlbum.value || a.id !== editingAlbum.value.id))
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
  })
}

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

const openCreateModal = () => {
  editingAlbum.value = null
  form.value = { name: '', description: '' }
  showEditModal.value = true
}

const openEditModal = (album) => {
  editingAlbum.value = album
  form.value = { name: album.name, description: album.description || '' }
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
  editingAlbum.value = null
}

const submitEdit = async () => {
  try {
    if (editingAlbum.value) {
      await updateAlbum(editingAlbum.value.id, form.value)
      showToast('相册已更新')
    } else {
      await createAlbum(form.value)
      showToast('相册创建成功')
    }
    closeEditModal()
    fetchAlbums()
  } catch (e) {
    showToast(e.message || '操作失败', 'error')
  }
}

const openDeleteModal = (album) => {
  deletingAlbum.value = album
  showDeleteModal.value = true
}

const doDelete = async () => {
  try {
    await deleteAlbum(deletingAlbum.value.id)
    showToast('相册已删除，图片仍保留在图库')
    showDeleteModal.value = false
    deletingAlbum.value = null
    fetchAlbums()
  } catch (e) {
    showToast(e.message || '删除失败', 'error')
  }
}

onMounted(fetchAlbums)
defineExpose({ fetchAlbums })
</script>
