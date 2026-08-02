<template>
  <div>
    <!-- 批量工具条 -->
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-lg font-bold text-gray-700">图库</h2>
      <div class="flex items-center space-x-2">
        <template v-if="selectMode">
          <span class="text-sm text-gray-500">已选 {{ selectedIds.length }} 张</span>
          <button @click="openBatchAdd" :disabled="selectedIds.length === 0" class="px-3 py-1.5 rounded-full text-sm font-medium bg-purple-500 text-white hover:bg-purple-600 transition disabled:opacity-40">
            加入相册
          </button>
          <button @click="exitSelect" class="px-3 py-1.5 rounded-full text-sm font-medium bg-gray-100 text-gray-600 hover:bg-gray-200 transition">取消</button>
        </template>
        <button v-else-if="pictures.length > 0" @click="selectMode = true" class="px-3 py-1.5 rounded-full text-sm font-medium bg-gray-100 text-gray-600 hover:bg-gray-200 transition">
          批量选择
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
    </div>

    <!-- Empty State -->
    <div v-else-if="pictures.length === 0" class="text-center py-20">
      <div class="text-6xl mb-4">🖼️</div>
      <h3 class="text-xl font-medium text-gray-500">暂无图片，快来上传吧！</h3>
    </div>

    <!-- Image Grid -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      <div
        v-for="pic in pictures"
        :key="pic.id"
        class="group relative bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden break-inside-avoid"
        :class="{ 'ring-2 ring-purple-400': selectMode && isSelected(pic) }"
        @click="selectMode ? toggleSelect(pic) : null"
      >
        <div class="h-64 bg-gray-100 relative overflow-hidden" :class="{ 'cursor-pointer': selectMode }">
          <img :src="pic.url" class="object-cover w-full h-full transform group-hover:scale-105 transition duration-500" loading="lazy" />
          <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-20 transition duration-300"></div>

          <!-- 选择态勾选标记 -->
          <div v-if="selectMode" class="absolute top-2 left-2">
            <span
              class="flex items-center justify-center h-6 w-6 rounded-full border-2"
              :class="isSelected(pic) ? 'bg-purple-500 border-purple-500 text-white' : 'bg-white bg-opacity-80 border-gray-300'"
            >
              <svg v-if="isSelected(pic)" class="h-4 w-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
            </span>
          </div>

          <!-- 常规操作（选择态下隐藏） -->
          <div v-if="!selectMode" class="absolute inset-0 flex items-center justify-center space-x-3 opacity-0 group-hover:opacity-100 transition duration-300">
            <button @click.stop="openAddToAlbum(pic)" class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-purple-600 shadow-lg hover:bg-white transition transform hover:scale-110" title="加入相册">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 7v10a2 2 0 002 2h12a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H6a2 2 0 00-2 2z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 11v4m-2-2h4" />
              </svg>
            </button>
            <a :href="pic.url" download target="_blank" class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-blue-600 shadow-lg hover:bg-white transition transform hover:scale-110" title="下载">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
            </a>
            <button @click.stop="askDelete(pic)" class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-red-600 shadow-lg hover:bg-white transition transform hover:scale-110" title="删除">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
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

    <!-- 删除确认弹层 -->
    <ConfirmDialog
      :visible="showDeleteModal"
      title="确认删除?"
      message="您确定要删除这张图片吗？此操作无法撤销。"
      confirm-text="删除"
      @confirm="doDelete"
      @cancel="cancelDelete"
    />

    <!-- 加入相册弹层（单张 or 批量） -->
    <AddToAlbumModal
      :visible="showAddModal"
      :picture="pictureToAdd"
      :picture-ids="batchIds"
      @close="onAddModalClose"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listPictures, deletePicture } from '../api/picture'
import { showToast } from '../composables/useToast'
import { formatTime } from '../utils/format'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import AddToAlbumModal from '../components/AddToAlbumModal.vue'

const pictures = ref([])
const loading = ref(false)

const showDeleteModal = ref(false)
const pictureToDelete = ref(null)

const showAddModal = ref(false)
const pictureToAdd = ref(null)
const batchIds = ref([])

// 批量选择
const selectMode = ref(false)
const selectedIds = ref([])

const fetchPictures = async () => {
  loading.value = true
  try {
    const res = await listPictures()
    pictures.value = res.data.reverse() // Newest first
  } catch (e) {
    console.error(e)
    showToast('加载图片失败，请检查网络', 'error')
  } finally {
    loading.value = false
  }
}

// ---- 单图删除 ----
const askDelete = (pic) => {
  pictureToDelete.value = pic
  showDeleteModal.value = true
}
const cancelDelete = () => {
  showDeleteModal.value = false
  pictureToDelete.value = null
}
const doDelete = async () => {
  if (!pictureToDelete.value) return
  try {
    await deletePicture(pictureToDelete.value.id)
    pictures.value = pictures.value.filter(p => p.id !== pictureToDelete.value.id)
    showToast('删除成功')
  } catch (e) {
    console.error(e)
    showToast('删除失败', 'error')
  } finally {
    showDeleteModal.value = false
    pictureToDelete.value = null
  }
}

// ---- 加入相册（单张）----
const openAddToAlbum = (pic) => {
  pictureToAdd.value = pic
  batchIds.value = []
  showAddModal.value = true
}

// ---- 批量选择 & 批量加入 ----
const isSelected = (pic) => selectedIds.value.indexOf(pic.id) !== -1
const toggleSelect = (pic) => {
  const idx = selectedIds.value.indexOf(pic.id)
  if (idx === -1) {
    selectedIds.value = selectedIds.value.concat(pic.id)
  } else {
    selectedIds.value = selectedIds.value.filter(id => id !== pic.id)
  }
}
const exitSelect = () => {
  selectMode.value = false
  selectedIds.value = []
}
const openBatchAdd = () => {
  if (selectedIds.value.length === 0) return
  pictureToAdd.value = null
  batchIds.value = selectedIds.value.slice()
  showAddModal.value = true
}

const onAddModalClose = (done) => {
  showAddModal.value = false
  // 批量加入成功后退出选择态
  if (done && batchIds.value.length > 0) {
    exitSelect()
  }
  batchIds.value = []
}

defineExpose({ fetchPictures })

onMounted(() => {
  fetchPictures()
})
</script>
