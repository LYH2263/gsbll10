<template>
  <div class="min-h-screen bg-gray-50 font-sans text-gray-700">
    <!-- Header -->
    <header class="bg-white shadow-sm sticky top-0 z-10">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex justify-between items-center">
        <div class="flex items-center space-x-3">
          <div class="w-8 h-8 bg-gradient-to-tr from-blue-500 to-purple-500 rounded-lg flex items-center justify-center text-white font-bold">P</div>
          <h1 class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-purple-600">
            云端图库
          </h1>
        </div>
        <button 
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

    <!-- Main Content -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
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
        >
          <!-- Image Container with Fixed Height -->
          <div class="h-64 bg-gray-100 relative overflow-hidden">
            <img 
              :src="pic.url" 
              class="object-cover w-full h-full transform group-hover:scale-105 transition duration-500" 
              loading="lazy"
            />
            <!-- Overlay -->
            <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-20 transition duration-300"></div>
            
            <!-- Actions Overlay -->
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
          
          <!-- Info -->
          <div class="p-4 h-20 flex flex-col justify-center">
            <h3 class="text-sm font-medium text-gray-800 truncate">{{ pic.name }}</h3>
            <p class="text-xs text-gray-400 mt-1">{{ formatTime(pic.createTime) }}</p>
          </div>
        </div>
      </div>
    </main>

    <!-- Delete Modal -->
    <div v-if="showDeleteModal" class="fixed inset-0 z-50 flex items-center justify-center px-4">
      <div class="absolute inset-0 bg-black bg-opacity-40 backdrop-filter backdrop-blur-sm transition-opacity" @click="cancelDelete"></div>
      <div class="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6 relative transform transition-all scale-100">
        <div class="text-center">
          <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4">
            <svg class="h-6 w-6 text-red-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h3 class="text-lg leading-6 font-medium text-gray-900">确认删除?</h3>
          <p class="text-sm text-gray-500 mt-2">
            您确定要删除这张图片吗？此操作无法撤销。
          </p>
        </div>
        <div class="mt-6 flex space-x-3">
          <button 
            @click="cancelDelete" 
            class="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium focus:outline-none"
          >
            取消
          </button>
          <button 
            @click="doDelete" 
            class="flex-1 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition font-medium shadow-md focus:outline-none"
          >
            删除
          </button>
        </div>
      </div>
    </div>
    <!-- Toast Notification -->
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
              <!-- Success Icon -->
              <svg v-if="toast.type === 'success'" class="h-6 w-6 text-green-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <!-- Error Icon -->
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
import axios from 'axios'

// Configure Axios
const api = axios.create({
  baseURL: '/api'
})

const pictures = ref([])
const loading = ref(false)
const fileInput = ref(null)
const showDeleteModal = ref(false)
const pictureToDelete = ref(null)

// Toast State
const toast = ref({
  show: false,
  message: '',
  type: 'success'
})

const showToast = (message, type = 'success') => {
  toast.value = { show: true, message, type }
  setTimeout(() => {
    toast.value.show = false
  }, 3000)
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

const fetchPictures = async () => {
  loading.value = true
  try {
    const res = await api.get('/list')
    pictures.value = res.data.reverse() // Newest first
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
  
  // Optimistic UI update or wait? Let's wait for simplicity but show loading if needed
  // ideally show a separate uploading state, but simple toast is OK for now
  
  try {
    await api.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    showToast('上传成功！')
    fetchPictures() 
  } catch (e) {
    console.error(e)
    showToast('上传失败，请重试', 'error')
  } finally {
    e.target.value = '' // Reset input
  }
}

const confirmDelete = (pic) => {
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
    await api.delete(`/delete/${pictureToDelete.value.id}`)
    pictures.value = pictures.value.filter(p => p.id !== pictureToDelete.value.id)
    showToast('删除成功')
    showDeleteModal.value = false
  } catch (e) {
    console.error(e)
    showToast('删除失败', 'error')
  }
}

onMounted(() => {
  fetchPictures()
})
</script>

<style>
/* Custom Scrollbar for better aesthetics */
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
