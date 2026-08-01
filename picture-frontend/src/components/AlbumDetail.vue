<template>
  <div>
    <div class="flex items-center space-x-4 mb-6">
      <button @click="$emit('back')" class="px-3 py-1.5 bg-white border border-gray-200 rounded-full text-sm text-gray-600 hover:bg-gray-100 transition">
        ← 返回相册
      </button>
      <h2 class="text-lg font-bold text-gray-800 truncate">{{ album ? album.name : '' }}</h2>
      <span v-if="album" class="text-sm text-gray-400 flex-shrink-0">{{ album.pictureCount }} 张</span>
      <div class="flex-1"></div>
      <template v-if="pictures.length > 0">
        <template v-if="selectMode">
          <span class="text-sm text-gray-500">已选 {{ selectedIds.length }} 张</span>
          <button @click="toggleSelectAll" class="px-3 py-1.5 bg-white border border-gray-200 rounded-full text-sm text-gray-600 hover:bg-gray-100 transition">
            {{ selectedIds.length === pictures.length ? '取消全选' : '全选' }}
          </button>
          <button
            @click="showBatchRemoveModal = true"
            :disabled="selectedIds.length === 0"
            class="px-4 py-1.5 bg-red-500 text-white rounded-full text-sm font-medium shadow hover:bg-red-600 transition disabled:opacity-40"
          >
            批量移出（{{ selectedIds.length }}）
          </button>
          <button @click="exitSelectMode" class="px-3 py-1.5 bg-white border border-gray-200 rounded-full text-sm text-gray-600 hover:bg-gray-100 transition">
            完成
          </button>
        </template>
        <button v-else @click="selectMode = true" class="px-4 py-1.5 bg-white border border-gray-200 rounded-full text-sm text-gray-600 hover:bg-gray-100 transition">
          批量管理
        </button>
      </template>
    </div>
    <p v-if="album && album.description" class="text-sm text-gray-500 mb-6">{{ album.description }}</p>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500"></div>
    </div>

    <div v-else-if="pictures.length === 0" class="text-center py-20">
      <div class="text-6xl mb-4">🖼️</div>
      <h3 class="text-xl font-medium text-gray-500">相册还是空的，去图库把图片加进来吧！</h3>
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      <div
        v-for="(pic, index) in pictures"
        :key="pic.id"
        class="group relative bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden"
        :class="{ 'cursor-pointer ring-2': selectMode, 'ring-red-400': selectMode && selectedIds.includes(pic.id), 'ring-transparent': selectMode && !selectedIds.includes(pic.id) }"
        @click="selectMode && toggleSelect(pic.id)"
      >
        <div class="h-64 bg-gray-100 relative overflow-hidden">
          <img :src="pic.url" class="object-cover w-full h-full transform group-hover:scale-105 transition duration-500" loading="lazy" />

          <!-- 批量选择模式的勾选标记 -->
          <div
            v-if="selectMode"
            class="absolute top-2 right-2 w-6 h-6 rounded-full border-2 flex items-center justify-center transition z-10"
            :class="selectedIds.includes(pic.id) ? 'bg-red-500 border-red-500 text-white' : 'bg-white bg-opacity-70 border-gray-300'"
          >
            <svg v-if="selectedIds.includes(pic.id)" xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
            </svg>
          </div>

          <!-- 封面角标：手动封面 / 自动封面（最近加入） -->
          <div
            v-if="isCover(pic)"
            class="absolute top-2 left-2 px-2 py-0.5 rounded-full text-xs text-white shadow"
            :class="isManualCover(pic) ? 'bg-amber-500' : 'bg-gray-500 bg-opacity-70'"
          >
            {{ isManualCover(pic) ? '封面' : '封面·自动' }}
          </div>

          <div v-if="!selectMode" class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-20 transition duration-300"></div>
          <div v-if="!selectMode" class="absolute inset-0 flex items-center justify-center space-x-2 opacity-0 group-hover:opacity-100 transition duration-300">
            <!-- 前移 -->
            <button
              v-if="index > 0"
              @click="movePicture(index, -1)"
              :disabled="orderSaving"
              class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-indigo-600 shadow-lg hover:bg-white transition transform hover:scale-110 disabled:opacity-40"
              title="前移"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <!-- 后移 -->
            <button
              v-if="index < pictures.length - 1"
              @click="movePicture(index, 1)"
              :disabled="orderSaving"
              class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-indigo-600 shadow-lg hover:bg-white transition transform hover:scale-110 disabled:opacity-40"
              title="后移"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </button>
            <!-- 设为封面 / 取消封面 -->
            <button
              @click="toggleCover(pic)"
              class="p-2 bg-white bg-opacity-90 rounded-full shadow-lg hover:bg-white transition transform hover:scale-110"
              :class="isManualCover(pic) ? 'text-amber-500 hover:text-gray-500' : 'text-gray-700 hover:text-amber-500'"
              :title="isManualCover(pic) ? '取消封面（回退自动）' : '设为封面'"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" :fill="isManualCover(pic) ? 'currentColor' : 'none'" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.196-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
              </svg>
            </button>
            <!-- 下载 -->
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
            <!-- 移出相册 -->
            <button
              @click="removeFromAlbum(pic)"
              class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-red-600 shadow-lg hover:bg-white transition transform hover:scale-110"
              title="移出相册"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z" />
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

    <!-- 批量移出确认弹层（自定义，文案含选中数量） -->
    <div v-if="showBatchRemoveModal" class="fixed inset-0 z-50 flex items-center justify-center px-4">
      <div class="absolute inset-0 bg-black bg-opacity-40 backdrop-filter backdrop-blur-sm" @click="showBatchRemoveModal = false"></div>
      <div class="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6 relative">
        <div class="text-center">
          <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4">
            <svg class="h-6 w-6 text-red-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h3 class="text-lg leading-6 font-medium text-gray-900">批量移出 {{ selectedIds.length }} 张图片?</h3>
          <p class="text-sm text-gray-500 mt-2">
            将把选中的 {{ selectedIds.length }} 张图片移出相册「{{ album ? album.name : '' }}」，仅解除相册关系，图片仍保留在图库中。
          </p>
        </div>
        <div class="mt-6 flex space-x-3">
          <button @click="showBatchRemoveModal = false" class="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium">取消</button>
          <button
            @click="doBatchRemove"
            :disabled="batchRemoving"
            class="flex-1 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition font-medium shadow-md disabled:opacity-40"
          >
            {{ batchRemoving ? '移出中...' : `移出 ${selectedIds.length} 张` }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { getAlbumDetail, removePictureFromAlbum, setAlbumCover, updateAlbumOrder, batchRemovePictures } from '../api/album'

const props = defineProps({ albumId: { type: Number, required: true } })
const emit = defineEmits(['back'])
const showToast = inject('showToast')

const album = ref(null)
const pictures = ref([])
const loading = ref(false)
const orderSaving = ref(false)
const selectMode = ref(false)
const selectedIds = ref([])
const showBatchRemoveModal = ref(false)
const batchRemoving = ref(false)

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
  })
}

// 当前展示封面（后端已按规则计算：手动封面优先，否则最近加入的成员）
const isCover = (pic) => album.value && album.value.cover && album.value.cover.id === pic.id
// 是否为手动指定的封面
const isManualCover = (pic) => album.value && album.value.coverPictureId === pic.id

const fetchDetail = async () => {
  loading.value = true
  try {
    const data = await getAlbumDetail(props.albumId)
    album.value = data
    pictures.value = data.pictures || []
  } catch (e) {
    showToast(e.message || '加载相册详情失败', 'error')
    emit('back')
  } finally {
    loading.value = false
  }
}

const toggleCover = async (pic) => {
  const clearing = isManualCover(pic)
  try {
    await setAlbumCover(props.albumId, clearing ? null : pic.id)
    showToast(clearing ? '已取消手动封面，回退为最近加入的图片' : '封面设置成功')
    fetchDetail()
  } catch (e) {
    showToast(e.message || '封面设置失败', 'error')
  }
}

const movePicture = async (index, direction) => {
  const target = index + direction
  if (target < 0 || target >= pictures.value.length || orderSaving.value) return
  // 本地先交换，给出即时反馈，再持久化
  const list = pictures.value.slice()
  const temp = list[index]
  list[index] = list[target]
  list[target] = temp
  pictures.value = list

  orderSaving.value = true
  try {
    await updateAlbumOrder(props.albumId, list.map(p => p.id))
    showToast('顺序已保存')
  } catch (e) {
    showToast(e.message || '顺序保存失败', 'error')
    fetchDetail() // 失败回滚到服务端顺序
  } finally {
    orderSaving.value = false
  }
}

const removeFromAlbum = async (pic) => {
  const wasCover = isCover(pic)
  try {
    await removePictureFromAlbum(props.albumId, pic.id)
    showToast(wasCover ? '已移出相册，封面已自动回退' : '已移出相册，图片仍保留在图库')
    fetchDetail()
  } catch (e) {
    showToast(e.message || '移出失败', 'error')
  }
}

const toggleSelect = (id) => {
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const toggleSelectAll = () => {
  if (selectedIds.value.length === pictures.value.length) {
    selectedIds.value = []
  } else {
    selectedIds.value = pictures.value.map(p => p.id)
  }
}

const exitSelectMode = () => {
  selectMode.value = false
  selectedIds.value = []
}

const doBatchRemove = async () => {
  batchRemoving.value = true
  try {
    const result = await batchRemovePictures(props.albumId, selectedIds.value.slice())
    const okCount = result.succeeded.length
    const ignoredCount = result.ignored.length
    const failedCount = result.failed.length
    if (failedCount > 0) {
      showToast(`批量移出：成功 ${okCount} 张，忽略 ${ignoredCount} 张，失败 ${failedCount} 张`, 'error')
    } else {
      showToast(`已移出 ${okCount} 张图片` + (ignoredCount > 0 ? `，${ignoredCount} 张本就不在相册中` : ''))
    }
    showBatchRemoveModal.value = false
    exitSelectMode()
    fetchDetail()
  } catch (e) {
    showToast(e.message || '批量移出失败', 'error')
  } finally {
    batchRemoving.value = false
  }
}

onMounted(fetchDetail)
</script>
