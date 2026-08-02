<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center space-x-4 min-w-0">
        <button
          @click="$emit('back')"
          class="p-2 rounded-full bg-white shadow-sm hover:shadow-md text-gray-600 hover:text-blue-600 transition flex-shrink-0"
        >
          <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <div class="min-w-0">
          <h2 class="text-2xl font-bold text-gray-800 truncate">{{ album?.name || '相册详情' }}</h2>
          <p class="text-sm text-gray-400 mt-1">
            {{ album?.pictureCount || 0 }} 张图片
            <span v-if="album?.description"> · {{ album.description }}</span>
          </p>
        </div>
      </div>
      <div class="flex items-center space-x-3">
        <button
          v-if="pictures.length && !sortMode"
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
          v-if="pictures.length && !selectMode"
          @click="toggleSortMode"
          class="px-4 py-2 rounded-full text-sm font-medium transition flex items-center space-x-2 shadow-sm"
          :class="sortMode ? 'bg-blue-500 text-white hover:bg-blue-600' : 'bg-white text-gray-600 hover:text-blue-600'"
        >
          <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
          <span>{{ sortMode ? '完成排序' : '排序' }}</span>
        </button>
        <button
          v-if="sortMode"
          @click="saveOrder"
          :disabled="savingOrder"
          class="px-4 py-2 bg-green-500 text-white rounded-full shadow-lg hover:bg-green-600 transition text-sm font-medium disabled:opacity-50"
        >
          {{ savingOrder ? '保存中...' : '保存顺序' }}
        </button>
        <button
          v-if="!selectMode"
          @click="openAdd"
          class="px-5 py-2 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-full shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 transition text-sm font-medium flex items-center space-x-2"
        >
          <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
          <span>添加图片</span>
        </button>
      </div>
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
        @click="confirmBatchRemove"
        :disabled="selectedIds.size === 0"
        class="px-4 py-1.5 bg-red-500 text-white text-sm rounded-full shadow hover:bg-red-600 transition disabled:opacity-40"
      >
        批量移出
      </button>
    </div>

    <div
      v-if="album && album.cover && !selectMode"
      class="relative h-48 sm:h-56 rounded-2xl overflow-hidden mb-6 shadow-md"
    >
      <img :src="album.cover.url" class="w-full h-full object-cover" />
      <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-black/10 to-transparent"></div>
      <div class="absolute bottom-0 left-0 right-0 p-5 flex items-end justify-between">
        <div class="text-white">
          <p class="text-xs uppercase tracking-wider opacity-80">封面</p>
          <p class="text-lg font-semibold truncate">{{ album.cover.name }}</p>
        </div>
        <button
          v-if="album.coverPictureId && !sortMode"
          @click="doClearCover"
          class="px-3 py-1.5 bg-white/90 text-gray-700 hover:text-red-600 rounded-full text-xs font-medium shadow transition"
        >
          移除封面
        </button>
      </div>
    </div>

    <div v-if="sortMode && pictures.length" class="mb-4 px-4 py-2 bg-blue-50 text-blue-600 text-sm rounded-lg flex items-center space-x-2">
      <svg class="h-4 w-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <span>拖拽图片卡片或使用上下按钮调整顺序，完成后点击「保存顺序」。</span>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
    </div>

    <div v-else-if="!pictures.length" class="text-center py-20">
      <div class="text-6xl mb-4">📭</div>
      <h3 class="text-xl font-medium text-gray-500">这个相册还是空的</h3>
      <p class="text-sm text-gray-400 mt-2">点击右上角「添加图片」从图库加入图片</p>
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      <div
        v-for="(pic, index) in pictures"
        :key="pic.id"
        :draggable="sortMode"
        class="group relative bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden"
        :class="[
          sortMode ? 'cursor-move ring-2 ring-transparent hover:ring-blue-300' : '',
          selectMode ? 'cursor-pointer ring-2 ' + (selectedIds.has(pic.id) ? 'ring-blue-500' : 'ring-transparent') : ''
        ]"
        @click="selectMode && toggleSelect(pic.id)"
        @dragstart="sortMode && onDragStart(index)"
        @dragover.prevent="sortMode && onDragOver(index)"
        @dragend="sortMode && onDragEnd"
      >
        <div class="h-64 bg-gray-100 relative overflow-hidden">
          <img
            :src="pic.url"
            class="object-cover w-full h-full transform duration-500"
            :class="sortMode || selectMode ? '' : 'group-hover:scale-105'"
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

          <div
            v-if="album && album.coverPictureId === pic.id && !selectMode"
            class="absolute top-3 left-3 px-2 py-1 bg-yellow-400 text-white text-xs font-semibold rounded-full shadow flex items-center space-x-1"
          >
            <svg class="h-3 w-3" fill="currentColor" viewBox="0 0 20 20">
              <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.539 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
            </svg>
            <span>封面</span>
          </div>

          <div v-if="sortMode" class="absolute top-3 right-3 flex flex-col space-y-1">
            <button
              @click="move(index, -1)"
              :disabled="index === 0"
              class="p-1.5 bg-white/90 rounded-full text-gray-700 shadow hover:text-blue-600 disabled:opacity-30 transition"
              title="上移"
            >
              <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7" />
              </svg>
            </button>
            <button
              @click="move(index, 1)"
              :disabled="index === pictures.length - 1"
              class="p-1.5 bg-white/90 rounded-full text-gray-700 shadow hover:text-blue-600 disabled:opacity-30 transition"
              title="下移"
            >
              <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </button>
          </div>

          <div
            v-if="!sortMode && !selectMode"
            class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-20 transition duration-300"
          ></div>

          <div
            v-if="!sortMode && !selectMode"
            class="absolute inset-0 flex items-center justify-center space-x-3 opacity-0 group-hover:opacity-100 transition duration-300"
          >
            <button
              @click="doSetCover(pic)"
              class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-yellow-500 shadow-lg transition transform hover:scale-110"
              title="设为封面"
            >
              <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
              </svg>
            </button>
            <a
              :href="pic.url"
              download
              target="_blank"
              class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-blue-600 shadow-lg transition transform hover:scale-110"
              title="下载"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
            </a>
            <button
              @click="confirmRemove(pic)"
              class="p-2 bg-white bg-opacity-90 rounded-full text-gray-700 hover:text-red-600 shadow-lg transition transform hover:scale-110"
              title="移出相册"
            >
              <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
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

    <AlbumDetailPicker
      v-if="showPicker"
      v-model="showPicker"
      :album-id="albumId"
      :existing-ids="existingIds"
      @added="fetchDetail"
    />

    <ConfirmModal
      v-model="showRemoveModal"
      title="移出相册?"
      :message="`确定要将「${pictureToRemove?.name}」从该相册移出吗？\n这只会解除图片与相册的关系，不会删除图片文件或图库记录。`"
      confirm-text="移出"
      :danger="true"
      @confirm="doRemove"
    />

    <ConfirmModal
      v-model="showBatchRemoveModal"
      title="批量移出?"
      :message="`确定要将选中的 ${selectedIds.size} 张图片从该相册移出吗？\n这只会解除图片与相册的关系，不会删除图片文件或图库记录。`"
      :confirm-text="`移出 ${selectedIds.size} 张`"
      :danger="true"
      @confirm="doBatchRemove"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import {
  getAlbum,
  removePictureFromAlbum,
  setAlbumCover,
  clearAlbumCover,
  updateAlbumOrder,
  batchRemovePictures
} from '../api/album'
import { showToast } from '../composables/useToast'
import { formatTime } from '../utils/format'
import { summarizeBatchResult } from '../utils/batch'
import ConfirmModal from '../components/ConfirmModal.vue'
import AlbumDetailPicker from '../components/AlbumDetailPicker.vue'

const props = defineProps({
  albumId: { type: Number, required: true }
})

defineEmits(['back'])

const album = ref(null)
const pictures = ref([])
const loading = ref(false)
const showRemoveModal = ref(false)
const pictureToRemove = ref(null)
const showPicker = ref(false)
const sortMode = ref(false)
const savingOrder = ref(false)
const dragIndex = ref(null)
const selectMode = ref(false)
const selectedIds = ref(new Set())
const showBatchRemoveModal = ref(false)

const existingIds = computed(() => pictures.value.map((p) => p.id))
const allSelected = computed(
  () => pictures.value.length > 0 && selectedIds.value.size === pictures.value.length
)

const applyDetail = (data) => {
  album.value = data
  pictures.value = data.pictures || []
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const data = await getAlbum(props.albumId)
    applyDetail(data)
  } catch (e) {
    showToast(e.message || '加载相册失败', 'error')
  } finally {
    loading.value = false
  }
}

watch(
  () => props.albumId,
  () => fetchDetail(),
  { immediate: true }
)

const openAdd = () => {
  showPicker.value = true
}

const toggleSortMode = () => {
  sortMode.value = !sortMode.value
  if (sortMode.value) selectMode.value = false
}

const toggleSelectMode = () => {
  selectMode.value = !selectMode.value
  selectedIds.value = new Set()
  if (selectMode.value) sortMode.value = false
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

const confirmBatchRemove = () => {
  if (selectedIds.value.size === 0) {
    showToast('请先选择图片', 'error')
    return
  }
  showBatchRemoveModal.value = true
}

const doBatchRemove = async () => {
  const ids = Array.from(selectedIds.value)
  try {
    const res = await batchRemovePictures(props.albumId, ids)
    if (res?.detail) applyDetail(res.detail)
    else if (res?.album) album.value = res.album
    selectedIds.value = new Set()
    selectMode.value = false
    showToast(summarizeBatchResult('批量移出', res?.result))
  } catch (e) {
    showToast(e.message || '批量移出失败', 'error')
  }
}

const move = (index, delta) => {
  const target = index + delta
  if (target < 0 || target >= pictures.value.length) return
  const list = pictures.value.slice()
  const item = list.splice(index, 1)[0]
  list.splice(target, 0, item)
  pictures.value = list
}

const onDragStart = (index) => {
  dragIndex.value = index
}

const onDragOver = (index) => {
  if (dragIndex.value === null || dragIndex.value === index) return
  const list = pictures.value.slice()
  const item = list.splice(dragIndex.value, 1)[0]
  list.splice(index, 0, item)
  pictures.value = list
  dragIndex.value = index
}

const onDragEnd = () => {
  dragIndex.value = null
}

const saveOrder = async () => {
  savingOrder.value = true
  try {
    const ids = pictures.value.map((p) => p.id)
    const updated = await updateAlbumOrder(props.albumId, ids)
    applyDetail(updated)
    showToast('顺序已保存')
    sortMode.value = false
  } catch (e) {
    showToast(e.message || '保存顺序失败', 'error')
  } finally {
    savingOrder.value = false
  }
}

const doSetCover = async (pic) => {
  try {
    await setAlbumCover(props.albumId, pic.id)
    showToast('封面已设置')
    fetchDetail()
  } catch (e) {
    showToast(e.message || '设置封面失败', 'error')
  }
}

const doClearCover = async () => {
  try {
    await clearAlbumCover(props.albumId)
    showToast('已移除封面，将自动使用最近加入的图片')
    fetchDetail()
  } catch (e) {
    showToast(e.message || '移除封面失败', 'error')
  }
}

const confirmRemove = (pic) => {
  pictureToRemove.value = pic
  showRemoveModal.value = true
}

const doRemove = async () => {
  if (!pictureToRemove.value) return
  const targetId = pictureToRemove.value.id
  try {
    await removePictureFromAlbum(props.albumId, targetId)
    showToast('已移出相册')
    await fetchDetail()
  } catch (e) {
    showToast(e.message || '移出失败', 'error')
  } finally {
    pictureToRemove.value = null
  }
}
</script>
