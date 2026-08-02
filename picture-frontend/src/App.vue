<template>
  <div class="min-h-screen bg-gray-50 font-sans text-gray-700">
    <header class="bg-white shadow-sm sticky top-0 z-10">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex justify-between items-center">
        <div class="flex items-center space-x-8">
          <div class="flex items-center space-x-3">
            <div class="w-8 h-8 bg-gradient-to-tr from-blue-500 to-purple-500 rounded-lg flex items-center justify-center text-white font-bold">P</div>
            <h1 class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-purple-600">
              云端图库
            </h1>
          </div>
          <nav class="flex space-x-1">
            <button
              @click="switchView('library')"
              class="px-4 py-2 rounded-lg text-sm font-medium transition"
              :class="currentView === 'library' ? 'bg-blue-50 text-blue-600' : 'text-gray-600 hover:bg-gray-100'"
            >
              图库
            </button>
            <button
              @click="switchView('albums')"
              class="px-4 py-2 rounded-lg text-sm font-medium transition"
              :class="currentView === 'albums' || currentView === 'albumDetail' ? 'bg-blue-50 text-blue-600' : 'text-gray-600 hover:bg-gray-100'"
            >
              相册
            </button>
          </nav>
        </div>
        <button
          v-if="currentView === 'library'"
          @click="triggerUpload"
          class="px-5 py-2 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-full shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 transition duration-200 flex items-center space-x-2 text-sm font-medium"
        >
          <span>上传图片</span>
          <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
          </svg>
        </button>
        <input type="file" ref="fileInput" class="hidden" @change="handleUpload" accept="image/*" />
      </div>
    </header>

    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div v-if="currentView === 'library'">
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
          >
            <div class="h-64 bg-gray-100 relative overflow-hidden">
              <img
                :src="pic.url"
                class="object-cover w-full h-full transform group-hover:scale-105 transition duration-500"
                loading="lazy"
              />
              <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-20 transition duration-300"></div>

              <div class="absolute inset-0 flex items-center justify-center space-x-3 opacity-0 group-hover:opacity-100 transition duration-300">
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
                  @click="openAddToAlbum(pic)"
                  class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-purple-600 shadow-lg hover:bg-white transition transform hover:scale-110"
                  title="加入相册"
                >
                  <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                  </svg>
                </button>
                <button
                  @click="confirmDeletePicture(pic)"
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
      </div>

      <div v-else-if="currentView === 'albums'">
        <AlbumList
          :albums="albums"
          :loading="albumsLoading"
          @create="openCreateAlbum"
          @open="openAlbumDetail"
          @edit="openEditAlbum"
          @delete="confirmDeleteAlbum"
        />
      </div>

      <div v-else-if="currentView === 'albumDetail'">
        <AlbumDetail
          :album="currentAlbum"
          :loading="albumDetailLoading"
          @back="backToAlbumList"
          @addPictures="openAddPicturesToAlbum"
          @remove="confirmRemoveFromAlbum"
          @setCover="handleSetCover"
          @clearCover="handleClearCover"
          @batchRemove="confirmBatchRemove"
          @reordered="handleReordered"
        />
      </div>
    </main>

    <ConfirmModal
      :visible="confirmModal.visible"
      :title="confirmModal.title"
      :message="confirmModal.message"
      :confirm-text="confirmModal.confirmText"
      :type="confirmModal.type"
      @confirm="confirmModal.onConfirm"
      @cancel="closeConfirmModal"
    />

    <AlbumFormModal
      :visible="albumFormVisible"
      :mode="albumFormMode"
      :album="editingAlbum"
      @submit="handleAlbumFormSubmit"
      @cancel="albumFormVisible = false"
    />

    <SelectAlbumModal
      :visible="addToAlbumVisible"
      :albums="albums"
      @confirm="handleAddToAlbum"
      @cancel="addToAlbumVisible = false"
    />

    <AddToAlbumModal
      :visible="addPicturesVisible"
      :pictures="pictures"
      :existing-picture-ids="currentAlbum ? currentAlbum.pictures.map(p => p.id) : []"
      @confirm="handleAddPicturesToAlbum"
      @cancel="addPicturesVisible = false"
    />

    <transition
      enter-active-class="transform ease-out duration-300 transition"
      enter-from-class="translate-y-2 opacity-0 sm:translate-y-0 sm:translate-x-2"
      enter-to-class="translate-y-0 opacity-100 sm:translate-x-0"
      leave-active-class="transition ease-in duration-100"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div v-if="toast.show" class="fixed top-20 right-5 z-50 max-w-sm w-full bg-white shadow-lg rounded-lg pointer-events-auto ring-1 ring-black ring-opacity-5 overflow-hidden">
        <div class="p-4">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <svg v-if="toast.type === 'success'" class="h-6 w-6 text-green-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <svg v-if="toast.type === 'error'" class="h-6 w-6 text-red-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div class="ml-3 w-0 flex-1 pt-0.5">
              <p class="text-sm font-medium text-gray-900">{{ toast.message }}</p>
            </div>
            <div class="ml-4 flex-shrink-0 flex">
              <button @click="toast.show = false" class="bg-white rounded-md inline-flex text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
                <span class="sr-only">Close</span>
                <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as pictureApi from './api/picture'
import * as albumApi from './api/album'
import AlbumList from './components/AlbumList.vue'
import AlbumDetail from './components/AlbumDetail.vue'
import AlbumFormModal from './components/AlbumFormModal.vue'
import ConfirmModal from './components/ConfirmModal.vue'
import AddToAlbumModal from './components/AddToAlbumModal.vue'
import SelectAlbumModal from './components/SelectAlbumModal.vue'

const currentView = ref('library')
const pictures = ref([])
const loading = ref(false)
const fileInput = ref(null)

const albums = ref([])
const albumsLoading = ref(false)
const currentAlbum = ref(null)
const albumDetailLoading = ref(false)

const albumFormVisible = ref(false)
const albumFormMode = ref('create')
const editingAlbum = ref(null)

const addToAlbumVisible = ref(false)
const addPicturesVisible = ref(false)
const targetPicture = ref(null)

const toast = ref({ show: false, message: '', type: 'success' })

const confirmModal = ref({
  visible: false,
  title: '',
  message: '',
  confirmText: '确认',
  type: 'danger',
  onConfirm: () => {}
})

const showToast = (message, type = 'success') => {
  toast.value = { show: true, message, type }
  setTimeout(() => { toast.value.show = false }, 3000)
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

const switchView = (view) => {
  currentView.value = view
  if (view === 'library') {
    fetchPictures()
  } else if (view === 'albums') {
    currentAlbum.value = null
    fetchAlbums()
  }
}

const fetchPictures = async () => {
  loading.value = true
  try {
    const data = await pictureApi.fetchPictures()
    pictures.value = data.reverse()
  } catch (e) {
    console.error(e)
    showToast('加载图片失败，请检查网络', 'error')
  } finally {
    loading.value = false
  }
}

const triggerUpload = () => {
  fileInput.value.click()
}

const handleUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    await pictureApi.uploadPicture(formData)
    showToast('上传成功！')
    fetchPictures()
  } catch (e) {
    console.error(e)
    showToast('上传失败，请重试', 'error')
  } finally {
    e.target.value = ''
  }
}

const confirmDeletePicture = (pic) => {
  confirmModal.value = {
    visible: true,
    title: '确认删除图片?',
    message: '您确定要删除这张图片吗？此操作将同时从所有相册中移除，无法撤销。',
    confirmText: '删除',
    type: 'danger',
    onConfirm: () => doDeletePicture(pic)
  }
}

const doDeletePicture = async (pic) => {
  try {
    await pictureApi.deletePicture(pic.id)
    pictures.value = pictures.value.filter(p => p.id !== pic.id)
    if (currentAlbum.value) {
      currentAlbum.value.pictures = currentAlbum.value.pictures.filter(p => p.id !== pic.id)
      currentAlbum.value.pictureCount = currentAlbum.value.pictures.length
    }
    showToast('删除成功')
    closeConfirmModal()
  } catch (e) {
    console.error(e)
    showToast('删除失败', 'error')
  }
}

const fetchAlbums = async () => {
  albumsLoading.value = true
  try {
    const res = await albumApi.fetchAlbumList()
    if (res.code === 0) {
      albums.value = res.data
    } else {
      showToast(res.message || '加载相册失败', 'error')
    }
  } catch (e) {
    console.error(e)
    showToast('加载相册失败', 'error')
  } finally {
    albumsLoading.value = false
  }
}

const openCreateAlbum = () => {
  albumFormMode.value = 'create'
  editingAlbum.value = null
  albumFormVisible.value = true
}

const openEditAlbum = (album) => {
  albumFormMode.value = 'edit'
  editingAlbum.value = album
  albumFormVisible.value = true
}

const handleAlbumFormSubmit = async (form) => {
  try {
    if (albumFormMode.value === 'create') {
      const res = await albumApi.createAlbum(form)
      if (res.code === 0) {
        showToast('相册创建成功')
        albumFormVisible.value = false
        fetchAlbums()
      } else {
        showToast(res.message || '创建失败', 'error')
      }
    } else {
      const res = await albumApi.updateAlbum(editingAlbum.value.id, form)
      if (res.code === 0) {
        showToast('更新成功')
        albumFormVisible.value = false
        fetchAlbums()
        if (currentAlbum.value && currentAlbum.value.id === editingAlbum.value.id) {
          currentAlbum.value.name = form.name
          currentAlbum.value.description = form.description
        }
      } else {
        showToast(res.message || '更新失败', 'error')
      }
    }
  } catch (e) {
    console.error(e)
    showToast('操作失败', 'error')
  }
}

const confirmDeleteAlbum = (album) => {
  confirmModal.value = {
    visible: true,
    title: '确认删除相册?',
    message: `确定要删除相册"${album.name}"吗？相册中的图片不会被删除，仅解除相册与图片的关联。`,
    confirmText: '删除相册',
    type: 'danger',
    onConfirm: () => doDeleteAlbum(album.id)
  }
}

const doDeleteAlbum = async (id) => {
  try {
    const res = await albumApi.deleteAlbum(id)
    if (res.code === 0) {
      showToast('相册已删除')
      closeConfirmModal()
      fetchAlbums()
    } else {
      showToast(res.message || '删除失败', 'error')
    }
  } catch (e) {
    console.error(e)
    showToast('删除失败', 'error')
  }
}

const openAlbumDetail = async (album) => {
  currentView.value = 'albumDetail'
  albumDetailLoading.value = true
  currentAlbum.value = null
  try {
    const res = await albumApi.fetchAlbumDetail(album.id)
    if (res.code === 0) {
      currentAlbum.value = res.data
    } else {
      showToast(res.message || '加载失败', 'error')
    }
  } catch (e) {
    console.error(e)
    showToast('加载相册详情失败', 'error')
  } finally {
    albumDetailLoading.value = false
  }
}

const backToAlbumList = () => {
  currentView.value = 'albums'
  currentAlbum.value = null
  fetchAlbums()
}

const openAddToAlbum = (pic) => {
  targetPicture.value = pic
  if (albums.value.length === 0) {
    fetchAlbums()
  }
  addToAlbumVisible.value = true
}

const handleAddToAlbum = async (albumIds) => {
  if (albumIds.length === 0) return
  try {
    let successCount = 0
    let failMsg = ''
    for (const aid of albumIds) {
      const res = await albumApi.addPictureToAlbum(aid, targetPicture.value.id)
      if (res.code === 0) {
        successCount++
      } else {
        failMsg = res.message
      }
    }
    if (successCount === albumIds.length) {
      showToast(`已加入${successCount}个相册`)
    } else if (successCount > 0) {
      showToast(`成功加入${successCount}个相册，部分失败：${failMsg}`, 'error')
    } else {
      showToast(failMsg || '操作失败', 'error')
    }
    addToAlbumVisible.value = false
    fetchAlbums()
  } catch (e) {
    console.error(e)
    showToast('操作失败', 'error')
  }
}

const openAddPicturesToAlbum = () => {
  if (pictures.value.length === 0) {
    fetchPictures()
  }
  addPicturesVisible.value = true
}

const buildBatchMessage = (data, actionLabel) => {
  const succeeded = data.succeeded ? data.succeeded.length : 0
  const ignored = data.ignored ? data.ignored.length : 0
  const failed = data.failed ? data.failed.length : 0
  let msg = `${actionLabel}${succeeded}张`
  if (ignored > 0) {
    msg += `，${ignored}张已存在已忽略`
  }
  if (failed > 0) {
    msg += `，${failed}张失败`
    const reasons = data.failed.map(f => f.reason).filter((v, i, a) => a.indexOf(v) === i)
    if (reasons.length > 0) {
      msg += `（${reasons.join('、')}）`
    }
  }
  return { message: msg, hasError: failed > 0 }
}

const handleAddPicturesToAlbum = async (pictureIds) => {
  if (!currentAlbum.value || pictureIds.length === 0) return
  try {
    const res = await albumApi.batchAddPictures(currentAlbum.value.id, pictureIds)
    if (res.code === 0) {
      const { message, hasError } = buildBatchMessage(res.data, '成功加入')
      if (hasError) {
        showToast(message, 'error')
      } else {
        showToast(message, 'success')
      }
      addPicturesVisible.value = false
      await openAlbumDetail({ id: currentAlbum.value.id })
      fetchAlbums()
    } else {
      showToast(res.message || '操作失败', 'error')
    }
  } catch (e) {
    console.error(e)
    showToast('操作失败', 'error')
  }
}

const confirmRemoveFromAlbum = (pictureId) => {
  confirmModal.value = {
    visible: true,
    title: '移出图片?',
    message: '确定将这张图片移出相册吗？图片本身不会被删除。',
    confirmText: '移出',
    type: 'danger',
    onConfirm: () => doRemoveFromAlbum(pictureId)
  }
}

const doRemoveFromAlbum = async (pictureId) => {
  try {
    const res = await albumApi.removePictureFromAlbum(currentAlbum.value.id, pictureId)
    if (res.code === 0) {
      showToast('已移出相册')
      closeConfirmModal()
      openAlbumDetail({ id: currentAlbum.value.id })
      fetchAlbums()
    } else {
      showToast(res.message || '操作失败', 'error')
    }
  } catch (e) {
    console.error(e)
    showToast('操作失败', 'error')
  }
}

const confirmBatchRemove = (pictureIds) => {
  confirmModal.value = {
    visible: true,
    title: '批量移出图片?',
    message: `确定将选中的 ${pictureIds.length} 张图片移出相册吗？图片本身不会被删除。`,
    confirmText: `移出${pictureIds.length}张`,
    type: 'danger',
    onConfirm: () => doBatchRemove(pictureIds)
  }
}

const doBatchRemove = async (pictureIds) => {
  try {
    const res = await albumApi.batchRemovePictures(currentAlbum.value.id, pictureIds)
    if (res.code === 0) {
      const { message, hasError } = buildBatchMessage(res.data, '已移出')
      if (hasError) {
        showToast(message, 'error')
      } else {
        showToast(message, 'success')
      }
      closeConfirmModal()
      await openAlbumDetail({ id: currentAlbum.value.id })
      fetchAlbums()
    } else {
      showToast(res.message || '操作失败', 'error')
    }
  } catch (e) {
    console.error(e)
    showToast('操作失败', 'error')
  }
}

const handleSetCover = async (pictureId) => {
  try {
    const res = await albumApi.setAlbumCover(currentAlbum.value.id, pictureId)
    if (res.code === 0) {
      showToast('封面已设置')
      currentAlbum.value.coverPictureId = pictureId
      const coverPic = currentAlbum.value.pictures.find(p => p.id === pictureId)
      if (coverPic) {
        currentAlbum.value.cover = { id: coverPic.id, name: coverPic.name, url: coverPic.url }
      }
      fetchAlbums()
    } else {
      showToast(res.message || '设置失败', 'error')
    }
  } catch (e) {
    console.error(e)
    showToast('设置失败', 'error')
  }
}

const handleClearCover = async () => {
  try {
    const res = await albumApi.setAlbumCover(currentAlbum.value.id, null)
    if (res.code === 0) {
      showToast('已恢复自动封面')
      const pictures = currentAlbum.value.pictures
      if (pictures.length > 0) {
        const fallback = pictures[0]
        currentAlbum.value.coverPictureId = null
        currentAlbum.value.cover = { id: fallback.id, name: fallback.name, url: fallback.url }
      } else {
        currentAlbum.value.coverPictureId = null
        currentAlbum.value.cover = null
      }
      fetchAlbums()
    } else {
      showToast(res.message || '操作失败', 'error')
    }
  } catch (e) {
    console.error(e)
    showToast('操作失败', 'error')
  }
}

const handleReordered = () => {
  fetchAlbums()
}

const closeConfirmModal = () => {
  confirmModal.value.visible = false
}

onMounted(() => {
  fetchPictures()
})
</script>

<style>
::-webkit-scrollbar {
  width: 8px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1;
}
::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
