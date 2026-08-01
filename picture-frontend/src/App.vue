<template>
  <div class="min-h-screen bg-gray-50 font-sans text-gray-700">
    <!-- Header -->
    <header class="bg-white shadow-sm sticky top-0 z-10">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex justify-between items-center">
        <div class="flex items-center space-x-6">
          <div class="flex items-center space-x-3">
            <div class="w-8 h-8 bg-gradient-to-tr from-blue-500 to-purple-500 rounded-lg flex items-center justify-center text-white font-bold">P</div>
            <h1 class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-purple-600">
              云端图库
            </h1>
          </div>
          <!-- 顶部标签导航：图库 / 相册 -->
          <nav class="flex items-center space-x-1">
            <button
              @click="switchTab('gallery')"
              :class="tab === 'gallery' ? 'bg-blue-50 text-blue-600' : 'text-gray-500 hover:text-gray-700'"
              class="px-3 py-1.5 rounded-full text-sm font-medium transition"
            >
              图库
            </button>
            <button
              @click="switchTab('albums')"
              :class="tab === 'albums' ? 'bg-purple-50 text-purple-600' : 'text-gray-500 hover:text-gray-700'"
              class="px-3 py-1.5 rounded-full text-sm font-medium transition"
            >
              相册
            </button>
          </nav>
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
      <GalleryView v-show="tab === 'gallery'" ref="galleryRef" />
      <AlbumsView v-if="tab === 'albums'" />
    </main>

    <!-- 全局 Toast -->
    <ToastHost />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { uploadPicture } from './api/picture'
import { showToast } from './composables/useToast'
import GalleryView from './views/GalleryView.vue'
import AlbumsView from './views/AlbumsView.vue'
import ToastHost from './components/ToastHost.vue'

const tab = ref('gallery')
const fileInput = ref(null)
const galleryRef = ref(null)

const switchTab = (t) => {
  tab.value = t
}

const triggerUpload = () => {
  fileInput.value.click()
}

const handleUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  try {
    await uploadPicture(file)
    showToast('上传成功！')
    // 上传后回到图库并刷新
    tab.value = 'gallery'
    if (galleryRef.value) {
      galleryRef.value.fetchPictures()
    }
  } catch (err) {
    console.error(err)
    showToast('上传失败，请重试', 'error')
  } finally {
    e.target.value = ''
  }
}
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
