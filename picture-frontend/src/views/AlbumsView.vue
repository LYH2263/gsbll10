<template>
  <div>
    <!-- 详情视图 -->
    <AlbumDetailView
      v-if="selectedAlbumId"
      :album-id="selectedAlbumId"
      @back="onBackFromDetail"
    />

    <!-- 列表视图 -->
    <template v-else>
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-lg font-bold text-gray-700">我的相册</h2>
        <button
          @click="openCreate"
          class="px-4 py-2 bg-gradient-to-r from-purple-500 to-purple-600 text-white rounded-full shadow hover:shadow-lg transition text-sm font-medium flex items-center space-x-2"
        >
          <svg class="h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
          <span>新建相册</span>
        </button>
      </div>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500"></div>
      </div>

      <div v-else-if="albums.length === 0" class="text-center py-20">
        <div class="text-6xl mb-4">📚</div>
        <h3 class="text-xl font-medium text-gray-500">还没有相册，点击右上角新建一个吧！</h3>
      </div>

      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        <div
          v-for="album in albums"
          :key="album.id"
          class="group relative bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden cursor-pointer"
          @click="openDetail(album)"
        >
          <div class="h-48 bg-gray-100 relative overflow-hidden flex items-center justify-center">
            <img v-if="album.cover" :src="album.cover.url" class="object-cover w-full h-full transform group-hover:scale-105 transition duration-500" loading="lazy" />
            <div v-else class="text-5xl text-gray-300">🖼️</div>
            <span class="absolute bottom-2 right-2 px-2 py-0.5 rounded-full bg-black bg-opacity-50 text-white text-xs">{{ album.pictureCount }} 张</span>
          </div>
          <div class="p-4">
            <h3 class="text-sm font-semibold text-gray-800 truncate">{{ album.name }}</h3>
            <p v-if="album.description" class="text-xs text-gray-400 mt-1 truncate">{{ album.description }}</p>
            <p class="text-xs text-gray-400 mt-1">更新于 {{ formatTime(album.updateTime) }}</p>
          </div>
          <!-- 相册操作 -->
          <div class="absolute top-2 right-2 flex space-x-1 opacity-0 group-hover:opacity-100 transition" @click.stop>
            <button @click="openRename(album)" class="p-1.5 bg-white bg-opacity-90 rounded-full text-gray-600 hover:text-blue-600 shadow" title="重命名">
              <svg class="h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </button>
            <button @click="askDelete(album)" class="p-1.5 bg-white bg-opacity-90 rounded-full text-gray-600 hover:text-red-600 shadow" title="删除相册">
              <svg class="h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </template>

    <!-- 新建 / 重命名弹层 -->
    <div v-if="showEditModal" class="fixed inset-0 z-50 flex items-center justify-center px-4">
      <div class="absolute inset-0 bg-black bg-opacity-40 backdrop-filter backdrop-blur-sm" @click="closeEdit"></div>
      <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6 relative">
        <h3 class="text-lg font-medium text-gray-900 mb-4">{{ editingAlbum ? '重命名相册' : '新建相册' }}</h3>
        <label class="block text-sm text-gray-600 mb-1">相册名 <span class="text-red-500">*</span></label>
        <input
          v-model="form.name"
          type="text"
          maxlength="50"
          placeholder="请输入相册名（1~50 字）"
          class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-300 mb-1"
        />
        <p v-if="nameConflict" class="text-xs text-amber-500 mb-2">已存在同名相册，可继续创建（以 id 区分）。</p>
        <label class="block text-sm text-gray-600 mb-1 mt-2">简介（可选）</label>
        <textarea
          v-model="form.description"
          maxlength="200"
          rows="3"
          placeholder="最多 200 字"
          class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-300 mb-4 resize-none"
        ></textarea>
        <div class="flex space-x-3">
          <button @click="closeEdit" class="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium">取消</button>
          <button @click="saveAlbum" :disabled="saving" class="flex-1 px-4 py-2 bg-purple-500 text-white rounded-lg hover:bg-purple-600 transition font-medium shadow-md disabled:opacity-50">
            {{ editingAlbum ? '保存' : '创建' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 删除相册确认 -->
    <ConfirmDialog
      :visible="showDeleteModal"
      title="确认删除相册?"
      :message="deleteMessage"
      confirm-text="删除相册"
      @confirm="doDelete"
      @cancel="showDeleteModal = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { listAlbums, createAlbum, updateAlbum, deleteAlbum, unwrap } from '../api/album'
import { showToast } from '../composables/useToast'
import { formatTime } from '../utils/format'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import AlbumDetailView from './AlbumDetailView.vue'

const albums = ref([])
const loading = ref(false)
const selectedAlbumId = ref(null)

const showEditModal = ref(false)
const editingAlbum = ref(null)
const saving = ref(false)
const form = reactive({ name: '', description: '' })

const showDeleteModal = ref(false)
const albumToDelete = ref(null)

const nameConflict = computed(() => {
  const n = form.name.trim()
  if (!n) return false
  return albums.value.some(a => a.name === n && (!editingAlbum.value || a.id !== editingAlbum.value.id))
})

const deleteMessage = computed(() => {
  if (!albumToDelete.value) return ''
  return `确定删除相册「${albumToDelete.value.name}」吗？\n删除相册只会解除图片与相册的关系，不会删除任何图片。`
})

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

const openCreate = () => {
  editingAlbum.value = null
  form.name = ''
  form.description = ''
  showEditModal.value = true
}

const openRename = (album) => {
  editingAlbum.value = album
  form.name = album.name
  form.description = album.description || ''
  showEditModal.value = true
}

const closeEdit = () => {
  showEditModal.value = false
}

const saveAlbum = async () => {
  const name = form.name.trim()
  if (!name) {
    showToast('相册名不能为空', 'error')
    return
  }
  saving.value = true
  try {
    const payload = { name, description: form.description.trim() }
    if (editingAlbum.value) {
      unwrap(await updateAlbum(editingAlbum.value.id, payload))
      showToast('已保存')
    } else {
      unwrap(await createAlbum(payload))
      showToast('相册创建成功')
    }
    showEditModal.value = false
    await fetchAlbums()
  } catch (e) {
    console.error(e)
    showToast(e.message || '保存失败', 'error')
  } finally {
    saving.value = false
  }
}

const askDelete = (album) => {
  albumToDelete.value = album
  showDeleteModal.value = true
}

const doDelete = async () => {
  if (!albumToDelete.value) return
  try {
    unwrap(await deleteAlbum(albumToDelete.value.id))
    showToast('相册已删除（图片未受影响）')
    await fetchAlbums()
  } catch (e) {
    console.error(e)
    showToast(e.message || '删除失败', 'error')
  } finally {
    showDeleteModal.value = false
    albumToDelete.value = null
  }
}

const openDetail = (album) => {
  selectedAlbumId.value = album.id
}

const onBackFromDetail = () => {
  selectedAlbumId.value = null
  fetchAlbums()
}

onMounted(() => {
  fetchAlbums()
})
</script>
