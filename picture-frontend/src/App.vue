<template>
  <div class="min-h-screen bg-gray-50 font-sans text-gray-700">
    <header class="bg-white shadow-sm sticky top-0 z-10">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex justify-between items-center">
        <div class="flex items-center space-x-3">
          <div class="w-8 h-8 bg-gradient-to-tr from-blue-500 to-purple-500 rounded-lg flex items-center justify-center text-white font-bold">P</div>
          <h1 class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-purple-600">
            云端图库
          </h1>
        </div>
        <nav class="flex bg-gray-100 rounded-full p-1">
          <button
            @click="currentView = 'library'; detailAlbumId = null"
            class="px-4 py-1.5 rounded-full text-sm font-medium transition"
            :class="currentView === 'library' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
          >
            图库
          </button>
          <button
            @click="currentView = 'albums'; detailAlbumId = null"
            class="px-4 py-1.5 rounded-full text-sm font-medium transition"
            :class="currentView === 'albums' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
          >
            相册
          </button>
        </nav>
        <div class="w-20"></div>
      </div>
    </header>

    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <LibraryView v-if="currentView === 'library'" />
      <AlbumListView v-else-if="currentView === 'albums' && !detailAlbumId" @open-detail="openAlbumDetail" />
      <AlbumDetailView
        v-else-if="currentView === 'albums' && detailAlbumId"
        :key="detailAlbumId"
        :album-id="detailAlbumId"
        @back="detailAlbumId = null"
      />
    </main>

    <Toast />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Toast from './components/Toast.vue'
import LibraryView from './views/LibraryView.vue'
import AlbumListView from './views/AlbumListView.vue'
import AlbumDetailView from './views/AlbumDetailView.vue'

const currentView = ref('library')
const detailAlbumId = ref(null)

const openAlbumDetail = (id) => {
  detailAlbumId.value = id
}
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
