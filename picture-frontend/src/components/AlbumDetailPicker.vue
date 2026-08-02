<template>
  <BaseModal :model-value="modelValue" title="从图库添加图片" @update:model-value="v => emit('update:modelValue', v)">
    <div class="mb-3 flex items-center justify-between">
      <p class="text-xs text-gray-400">点击图片即可加入相册（已在相册中的图片会被忽略）</p>
      <button
        v-if="libraryPictures.length"
        @click="addAllRemaining"
        class="text-xs text-blue-600 hover:text-blue-700 font-medium"
      >
        一键添加剩余 ({{ remainingCount }})
      </button>
    </div>

    <div v-if="loading" class="flex justify-center py-10">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
    </div>

    <div v-else-if="libraryPictures.length === 0" class="text-center py-10 text-gray-400 text-sm">
      图库中暂无图片，请先上传
    </div>

    <div v-else class="grid grid-cols-3 gap-3 max-h-96 overflow-y-auto">
      <div
        v-for="pic in libraryPictures"
        :key="pic.id"
        class="relative aspect-square rounded-lg overflow-hidden bg-gray-100 group cursor-pointer"
        @click="addOne(pic)"
      >
        <img :src="pic.url" class="w-full h-full object-cover" loading="lazy" />
        <div
          class="absolute inset-0 flex items-center justify-center transition"
          :class="addedIds.has(pic.id) ? 'bg-black bg-opacity-50' : 'bg-black bg-opacity-0 group-hover:bg-opacity-30'"
        >
          <span
            v-if="addedIds.has(pic.id)"
            class="px-2 py-1 bg-white bg-opacity-90 text-green-600 text-xs rounded-full font-medium"
          >
            已加入
          </span>
          <span
            v-else
            class="px-2 py-1 bg-white bg-opacity-0 group-hover:bg-opacity-90 text-gray-700 text-xs rounded-full font-medium opacity-0 group-hover:opacity-100 transition"
          >
            加入
          </span>
        </div>
      </div>
    </div>

    <div class="mt-6">
      <button
        @click="close"
        class="w-full px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium"
      >
        完成
      </button>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import BaseModal from './BaseModal.vue'
import { listPictures } from '../api/picture'
import { addPictureToAlbum } from '../api/album'
import { showToast } from '../composables/useToast'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  albumId: { type: Number, required: true },
  existingIds: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue', 'added'])

const libraryPictures = ref([])
const addedIds = ref(new Set())
const loading = ref(false)

const remainingCount = computed(
  () => libraryPictures.value.filter((p) => !addedIds.value.has(p.id)).length
)

watch(
  () => props.modelValue,
  async (val) => {
    if (val) {
      addedIds.value = new Set(props.existingIds)
      loading.value = true
      try {
        const data = await listPictures()
        libraryPictures.value = (data || []).slice().reverse()
      } catch (e) {
        showToast(e.message || '加载图库失败', 'error')
      } finally {
        loading.value = false
      }
    }
  }
)

const addOne = async (pic) => {
  if (addedIds.value.has(pic.id)) return
  try {
    await addPictureToAlbum(props.albumId, pic.id)
    addedIds.value = new Set([...addedIds.value, pic.id])
    emit('added')
  } catch (e) {
    showToast(e.message || '加入失败', 'error')
  }
}

const addAllRemaining = async () => {
  const remaining = libraryPictures.value.filter((p) => !addedIds.value.has(p.id))
  let success = 0
  for (const pic of remaining) {
    try {
      await addPictureToAlbum(props.albumId, pic.id)
      addedIds.value = new Set([...addedIds.value, pic.id])
      success++
    } catch (e) {
      // ignore per-item, continue
    }
  }
  if (success > 0) {
    showToast(`已加入 ${success} 张图片`)
    emit('added')
  }
}

const close = () => emit('update:modelValue', false)
</script>
