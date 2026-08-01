<template>
  <div>
    <!-- 头部：返回 + 相册信息 -->
    <div class="mb-6">
      <button @click="$emit('back')" class="text-sm text-gray-500 hover:text-purple-600 flex items-center space-x-1 mb-3">
        <svg class="h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
        <span>返回相册列表</span>
      </button>

      <div class="flex items-center justify-between flex-wrap gap-2">
        <div v-if="album">
          <div class="flex items-baseline space-x-3">
            <h2 class="text-2xl font-bold text-gray-800">{{ album.name }}</h2>
            <span class="text-sm text-gray-400">{{ album.pictureCount }} 张图片 · 更新于 {{ formatTime(album.updateTime) }}</span>
          </div>
          <p class="text-xs mt-1" :class="album.coverPictureId ? 'text-purple-500' : 'text-gray-400'">
            {{ album.coverPictureId ? '封面：手动指定' : '封面：默认使用最近加入的图片' }}
          </p>
          <p v-if="album && album.description" class="text-sm text-gray-500 mt-1">{{ album.description }}</p>
        </div>

        <!-- 排序 / 批量移除 切换 -->
        <div v-if="pictures.length > 0" class="flex items-center space-x-2">
          <template v-if="selectMode">
            <span class="text-sm text-gray-500">已选 {{ selectedIds.length }} 张</span>
            <button @click="askBatchRemove" :disabled="selectedIds.length === 0" class="px-3 py-1.5 rounded-full text-sm font-medium bg-red-500 text-white hover:bg-red-600 transition disabled:opacity-40">批量移除</button>
            <button @click="exitSelect" class="px-3 py-1.5 rounded-full text-sm font-medium bg-gray-100 text-gray-600 hover:bg-gray-200 transition">取消</button>
          </template>
          <template v-else-if="sortMode">
            <button @click="saveOrder" :disabled="savingOrder" class="px-3 py-1.5 rounded-full text-sm font-medium bg-purple-500 text-white hover:bg-purple-600 transition disabled:opacity-50">保存顺序</button>
            <button @click="cancelSort" class="px-3 py-1.5 rounded-full text-sm font-medium bg-gray-100 text-gray-600 hover:bg-gray-200 transition">取消</button>
          </template>
          <template v-else>
            <button v-if="pictures.length > 1" @click="enterSortMode" class="px-3 py-1.5 rounded-full text-sm font-medium bg-gray-100 text-gray-600 hover:bg-gray-200 transition">调整顺序</button>
            <button @click="enterSelectMode" class="px-3 py-1.5 rounded-full text-sm font-medium bg-gray-100 text-gray-600 hover:bg-gray-200 transition">批量移除</button>
          </template>
        </div>
      </div>
      <p v-if="sortMode" class="text-xs text-purple-500 mt-2">用每张图片上的 ↑ / ↓ 调整位置，最靠前者排在第一位；完成后点"保存顺序"。</p>
      <p v-if="selectMode" class="text-xs text-red-500 mt-2">点选要移除的图片，然后点"批量移除"。移除只解除关系，不会删除图片本身。</p>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500"></div>
    </div>

    <div v-else-if="pictures.length === 0" class="text-center py-20">
      <div class="text-6xl mb-4">🖼️</div>
      <h3 class="text-xl font-medium text-gray-500">相册里还没有图片</h3>
      <p class="text-sm text-gray-400 mt-2">回到"图库"，在图片上点击"加入相册"即可添加。</p>
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      <div
        v-for="(pic, index) in pictures"
        :key="pic.id"
        class="group relative bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden"
        :class="{ 'ring-2 ring-purple-400': isCover(pic) && !selectMode, 'ring-2 ring-red-400': selectMode && isSelected(pic) }"
        @click="selectMode ? toggleSelect(pic) : null"
      >
        <div class="h-64 bg-gray-100 relative overflow-hidden" :class="{ 'cursor-pointer': selectMode }">
          <img :src="pic.url" class="object-cover w-full h-full transform group-hover:scale-105 transition duration-500" loading="lazy" />

          <!-- 当前封面角标 -->
          <span v-if="isCover(pic) && !selectMode" class="absolute top-2 left-2 px-2 py-0.5 rounded-full bg-purple-500 text-white text-xs shadow flex items-center space-x-1">
            <svg class="h-3 w-3" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
              <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.968a1 1 0 00.95.69h4.17c.969 0 1.371 1.24.588 1.81l-3.376 2.455a1 1 0 00-.363 1.118l1.287 3.968c.3.922-.755 1.688-1.54 1.118l-3.376-2.454a1 1 0 00-1.175 0l-3.376 2.454c-.784.57-1.838-.196-1.539-1.118l1.287-3.968a1 1 0 00-.363-1.118L2.98 9.395c-.783-.57-.38-1.81.588-1.81h4.17a1 1 0 00.951-.69l1.286-3.968z" />
            </svg>
            <span>封面</span>
          </span>

          <!-- 选择态勾选标记 -->
          <div v-if="selectMode" class="absolute top-2 left-2">
            <span
              class="flex items-center justify-center h-6 w-6 rounded-full border-2"
              :class="isSelected(pic) ? 'bg-red-500 border-red-500 text-white' : 'bg-white bg-opacity-80 border-gray-300'"
            >
              <svg v-if="isSelected(pic)" class="h-4 w-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
            </span>
          </div>

          <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-20 transition duration-300"></div>

          <!-- 排序模式：上移 / 下移 -->
          <div v-if="sortMode" class="absolute inset-0 flex items-center justify-center space-x-3">
            <button @click="move(index, -1)" :disabled="index === 0" class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-purple-600 shadow-lg disabled:opacity-30" title="上移">
              <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7" />
              </svg>
            </button>
            <span class="px-2 py-1 bg-white bg-opacity-90 rounded-full text-xs font-medium text-gray-600 shadow">第 {{ index + 1 }} 位</span>
            <button @click="move(index, 1)" :disabled="index === pictures.length - 1" class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-purple-600 shadow-lg disabled:opacity-30" title="下移">
              <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </button>
          </div>

          <!-- 常规操作 -->
          <div v-else-if="!selectMode" class="absolute inset-0 flex items-center justify-center space-x-3 opacity-0 group-hover:opacity-100 transition duration-300">
            <!-- 设为封面 / 取消封面 -->
            <button
              v-if="!isManualCover(pic)"
              @click="setCover(pic)"
              class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-purple-600 shadow-lg hover:bg-white transition transform hover:scale-110"
              title="设为封面"
            >
              <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.968a1 1 0 00.95.69h4.17c.969 0 1.371 1.24.588 1.81l-3.376 2.455a1 1 0 00-.363 1.118l1.287 3.968c.3.922-.755 1.688-1.54 1.118l-3.376-2.454a1 1 0 00-1.175 0l-3.376 2.454c-.784.57-1.838-.196-1.539-1.118l1.287-3.968a1 1 0 00-.363-1.118L2.98 9.395c-.783-.57-.38-1.81.588-1.81h4.17a1 1 0 00.951-.69l1.286-3.968z" />
              </svg>
            </button>
            <button
              v-else
              @click="clearCover"
              class="p-2 bg-purple-500 bg-opacity-90 rounded-full text-white shadow-lg hover:bg-purple-600 transition transform hover:scale-110"
              title="取消手动封面"
            >
              <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.968a1 1 0 00.95.69h4.17c.969 0 1.371 1.24.588 1.81l-3.376 2.455a1 1 0 00-.363 1.118l1.287 3.968c.3.922-.755 1.688-1.54 1.118l-3.376-2.454a1 1 0 00-1.175 0l-3.376 2.454c-.784.57-1.838-.196-1.539-1.118l1.287-3.968a1 1 0 00-.363-1.118L2.98 9.395c-.783-.57-.38-1.81.588-1.81h4.17a1 1 0 00.951-.69l1.286-3.968z" />
              </svg>
            </button>
            <a :href="pic.url" download target="_blank" class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-blue-600 shadow-lg hover:bg-white transition transform hover:scale-110" title="下载">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
            </a>
            <!-- 移出相册（仅解关系，不删图片） -->
            <button @click="askRemove(pic)" class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-red-600 shadow-lg hover:bg-white transition transform hover:scale-110" title="移出相册">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
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

    <!-- 单张移出确认 -->
    <ConfirmDialog
      :visible="showRemoveModal"
      title="移出相册?"
      message="确定将这张图片移出相册吗？这只会解除关系，不会删除图片本身。"
      confirm-text="移出"
      @confirm="doRemove"
      @cancel="showRemoveModal = false"
    />

    <!-- 批量移出确认（文案含选中数量） -->
    <ConfirmDialog
      :visible="showBatchRemoveModal"
      title="批量移出?"
      :message="`确定将选中的 ${selectedIds.length} 张图片移出相册吗？这只会解除关系，不会删除图片本身。`"
      confirm-text="移出所选"
      @confirm="doBatchRemove"
      @cancel="showBatchRemoveModal = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  getAlbumDetail,
  removePictureFromAlbum,
  setAlbumCover,
  clearAlbumCover,
  updateAlbumOrder,
  batchRemoveFromAlbum,
  unwrap,
  summarizeBatch
} from '../api/album'
import { showToast } from '../composables/useToast'
import { formatTime } from '../utils/format'
import ConfirmDialog from '../components/ConfirmDialog.vue'

const props = defineProps({
  albumId: { type: [Number, String], required: true }
})
defineEmits(['back'])

const album = ref(null)
const pictures = ref([])
const loading = ref(false)

const showRemoveModal = ref(false)
const pictureToRemove = ref(null)

// 批量移除选择
const selectMode = ref(false)
const selectedIds = ref([])
const showBatchRemoveModal = ref(false)

// 排序模式
const sortMode = ref(false)
const savingOrder = ref(false)
let backupOrder = []

const fetchDetail = async () => {
  loading.value = true
  try {
    const data = unwrap(await getAlbumDetail(props.albumId))
    album.value = data.album
    pictures.value = data.pictures || []
  } catch (e) {
    console.error(e)
    showToast(e.message || '加载相册详情失败', 'error')
  } finally {
    loading.value = false
  }
}

// 当前展示封面（后端计算，可能是手动或最近加入回退结果）
const isCover = (pic) => album.value && album.value.cover && album.value.cover.id === pic.id
// 是否为"手动指定"的封面（用于展示取消按钮）
const isManualCover = (pic) => album.value && album.value.coverPictureId === pic.id

const setCover = async (pic) => {
  try {
    album.value = unwrap(await setAlbumCover(props.albumId, pic.id))
    showToast('已设为封面')
  } catch (e) {
    console.error(e)
    showToast(e.message || '设置封面失败', 'error')
  }
}

const clearCover = async () => {
  try {
    album.value = unwrap(await clearAlbumCover(props.albumId))
    showToast('已取消手动封面，将使用最近加入的图片')
  } catch (e) {
    console.error(e)
    showToast(e.message || '取消封面失败', 'error')
  }
}

// ---- 排序 ----
const enterSortMode = () => {
  backupOrder = pictures.value.slice()
  sortMode.value = true
}

const cancelSort = () => {
  pictures.value = backupOrder.slice()
  sortMode.value = false
}

const move = (index, delta) => {
  const target = index + delta
  if (target < 0 || target >= pictures.value.length) return
  const arr = pictures.value.slice()
  const tmp = arr[index]
  arr[index] = arr[target]
  arr[target] = tmp
  pictures.value = arr
}

const saveOrder = async () => {
  savingOrder.value = true
  try {
    const ids = pictures.value.map(p => p.id)
    album.value = unwrap(await updateAlbumOrder(props.albumId, ids))
    showToast('顺序已保存')
    sortMode.value = false
    await fetchDetail()
  } catch (e) {
    console.error(e)
    showToast(e.message || '保存顺序失败', 'error')
  } finally {
    savingOrder.value = false
  }
}

// ---- 移出 ----
const askRemove = (pic) => {
  pictureToRemove.value = pic
  showRemoveModal.value = true
}

const doRemove = async () => {
  if (!pictureToRemove.value) return
  try {
    unwrap(await removePictureFromAlbum(props.albumId, pictureToRemove.value.id))
    showToast('已移出相册')
    await fetchDetail()
  } catch (e) {
    console.error(e)
    showToast(e.message || '移出失败', 'error')
  } finally {
    showRemoveModal.value = false
    pictureToRemove.value = null
  }
}

// ---- 批量移除 ----
const enterSelectMode = () => {
  selectMode.value = true
  selectedIds.value = []
}
const exitSelect = () => {
  selectMode.value = false
  selectedIds.value = []
}
const isSelected = (pic) => selectedIds.value.indexOf(pic.id) !== -1
const toggleSelect = (pic) => {
  const idx = selectedIds.value.indexOf(pic.id)
  if (idx === -1) {
    selectedIds.value = selectedIds.value.concat(pic.id)
  } else {
    selectedIds.value = selectedIds.value.filter(id => id !== pic.id)
  }
}
const askBatchRemove = () => {
  if (selectedIds.value.length === 0) return
  showBatchRemoveModal.value = true
}
const doBatchRemove = async () => {
  try {
    const result = unwrap(await batchRemoveFromAlbum(props.albumId, selectedIds.value.slice()))
    showToast(summarizeBatch(result, '移出'))
    exitSelect()
    await fetchDetail()
  } catch (e) {
    console.error(e)
    showToast(e.message || '批量移出失败', 'error')
  } finally {
    showBatchRemoveModal.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>
