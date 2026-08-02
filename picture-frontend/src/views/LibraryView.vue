<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">全部图片</h2>
        <p class="text-sm text-gray-400 mt-1">共 {{ pictures.length }} 张图片</p>
      </div>
      <div class="flex items-center space-x-3">
        <button
          v-if="pictures.length"
          @click="toggleSelectMode"
          class="px-4 py-2 rounded-full text-sm font-medium transition flex items-center space-x-2 shadow-sm"
          :class="selectMode ? 'bg-blue-500 text-white hover:bg-blue-600' : 'bg-white text-gray-600 hover:text-blue-600'"
        >
          <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
          </svg>
          <span>{{ selectMode ? '退出选择' : '批量选择' }}</span>
        </button>
        <button
          @click="triggerUpload"
          class="px-5 py-2 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-full shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 transition duration-200 flex items-center space-x-2 text-sm font-medium"
        >
          <span>上传图片</span>
          <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
          </svg>
        </button>
      </div>
      <input type="file" ref="fileInput" class="hidden" @change="handleUpload" accept="image/*" />
    </div>

    <div
      v-if="selectMode && pictures.length"
      class="mb-4 flex items-center justify-between bg-white rounded-xl shadow-sm px-4 py-3"
    >
      <div class="flex items-center space-x-3">
        <button
          @click="toggleSelectAll"
          class="px-3 py-1.5 text-xs font-medium rounded-full border transition"
          :class="allSelected ? 'bg-blue-500 text-white border-blue-500' : 'text-blue-600 border-blue-200 hover:bg-blue-50'"
        >
          {{ allSelected ? '取消全选' : '全选' }}
        </button>
        <span class="text-sm text-gray-500">已选 <b class="text-blue-600">{{ selectedIds.size }}</b> 张</span>
      </div>
      <button
        @click="openBatchPicker"
        :disabled="selectedIds.size === 0"
        class="px-4 py-1.5 bg-gradient-to-r from-blue-500 to-blue-600 text-white text-sm rounded-full shadow hover:shadow-md transition disabled:opacity-40"
      >
        加入相册
      </button>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
    </div>

    <div v-else-if="pictures.length === 0" class="text-center py-20">
      <div class="text-6xl mb-4">🖼️</div>
      <h3 class="text-xl font-medium text-gray-500">暂无图片，快来上传吧！</h3>
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      <div
        v-for="pic in pictures"
        :key="pic.id"
        class="group relative bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden break-inside-avoid"
        :class="[selectMode ? 'cursor-pointer ring-2' : '', selectedIds.has(pic.id) ? 'ring-blue-500' : 'ring-transparent']"
        @click="selectMode && toggleSelect(pic.id)"
      >
        <div class="h-64 bg-gray-100 relative overflow-hidden">
          <img
            :src="pic.url"
            class="object-cover w-full h-full transform group-hover:scale-105 transition duration-500"
            loading="lazy"
          />

          <div
            v-if="selectMode"
            class="absolute top-3 left-3 w-6 h-6 rounded-full border-2 flex items-center justify-center transition"
            :class="selectedIds.has(pic.id) ? 'bg-blue-500 border-blue-500' : 'bg-white/80 border-white'"
          >
            <svg v-if="selectedIds.has(pic.id)" class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
            </svg>
          </div>

          <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-20 transition duration-300"></div>

          <div v-if="!selectMode" class="absolute top-3 right-3 opacity-0 group-hover:opacity-100 transition duration-300">
            <button
              @click="openPicker(pic)"
              class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-blue-600 shadow-lg hover:bg-white transition transform hover:scale-110"
              title="加入相册"
            >
              <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
            </button>
          </div>

          <div
            v-if="!selectMode"
            class="absolute inset-0 flex items-center justify-center space-x-3 opacity-0 group-hover:opacity-100 transition duration-300"
          >
            <a
              :href="pic.url"
              download
              target="_blank"
              class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-blue-600 shadow-lg hover:bg-white transition transform hover:scale-110"
              title="下载"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
            </a>
            <button
              @click="confirmDelete(pic)"
              class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-red-600 shadow-lg hover:bg-white transition transform hover:scale-110"
              title="删除"
            >
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

    <ConfirmModal
      v-model="showDeleteModal"
      title="确认删除图片?"
      message="您确定要从图库删除这张图片吗？图片文件和图片记录将被永久删除，且该图片也会从所有相册中移出。"
      confirm-text="删除"
      :danger="true"
      @confirm="doDelete"
    />

    <AlbumPickerModal
      v-model="showPicker"
      :picture-id="pickerPictureId"
      @joined="fetchPictures"
    />

    <BatchAlbumPickerModal
      v-model="showBatchPicker"
      :picture-ids="Array.from(selectedIds)"
      @added="onBatchAdded"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listPictures, uploadPicture, deletePicture } from '../api/picture'
import { showToast } from '../composables/useToast'
import { formatTime } from '../utils/format'
import ConfirmModal from '../components/ConfirmModal.vue'
import AlbumPickerModal from '../components/AlbumPickerModal.vue'
import BatchAlbumPickerModal from '../components/BatchAlbumPickerModal.vue'

const pictures = ref([])
const loading = ref(false)
const fileInput = ref(null)
const showDeleteModal = ref(false)
const pictureToDelete = ref(null)
const showPicker = ref(false)
const pickerPictureId = ref(null)
const showBatchPicker = ref(false)
const selectMode = ref(false)
const selectedIds = ref(new Set())

const allSelected = computed(
  () => pictures.value.length > 0 && selectedIds.value.size === pictures.value.length
)

const fetchPictures = async () => {
  loading.value = true
  try {
    const data = await listPictures()
    pictures.value = (data || []).slice().reverse()
  } catch (e) {
    showToast(e.message || '加载图片失败', 'error')
  } finally {
    loading.value = false
  }
}

const triggerUpload = () => fileInput.value.click()

const handleUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    await uploadPicture(formData)
    showToast('上传成功！')
    fetchPictures()
  } catch (e) {
    showToast(e.message || '上传失败', 'error')
  } finally {
    e.target.value = ''
  }
}

const confirmDelete = (pic) => {
  pictureToDelete.value = pic
  showDeleteModal.value = true
}

const doDelete = async () => {
  if (!pictureToDelete.value) return
  try {
    await deletePicture(pictureToDelete.value.id)
    pictures.value = pictures.value.filter((p) => p.id !== pictureToDelete.value.id)
    selectedIds.value.delete(pictureToDelete.value.id)
    showToast('删除成功')
  } catch (e) {
    showToast(e.message || '删除失败', 'error')
  } finally {
    pictureToDelete.value = null
  }
}

const openPicker = (pic) => {
  pickerPictureId.value = pic.id
  showPicker.value = true
}

const toggleSelectMode = () => {
  selectMode.value = !selectMode.value
  selectedIds.value = new Set()
}

const toggleSelect = (id) => {
  const next = new Set(selectedIds.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  selectedIds.value = next
}

const toggleSelectAll = () => {
  if (allSelected.value) {
    selectedIds.value = new Set()
  } else {
    selectedIds.value = new Set(pictures.value.map((p) => p.id))
  }
}

const openBatchPicker = () => {
  if (selectedIds.value.size === 0) {
    showToast('请先选择图片', 'error')
    return
  }
  showBatchPicker.value = true
}

const onBatchAdded = () => {
  selectedIds.value = new Set()
}

onMounted(fetchPictures)
</script>
