<template>
  <div class="card p-4">
    <div class="flex items-center justify-between">
      <h2 class="font-semibold">ニュース一覧</h2>
      <div class="flex items-center gap-2">
        <input v-model="searchQuery" placeholder="検索：タイトル/本文" class="input w-56">
        <button v-if="canEdit" @click="$emit('create-news')" class="px-3 py-2 rounded-lg btn-primary">
          <i class="fa-solid fa-plus mr-1"></i> 新規作成
        </button>
      </div>
    </div>

    <div class="mt-4 overflow-auto">
      <table class="min-w-full text-sm">
        <thead class="border-b bg-gray-50">
        <tr>
          <th class="text-left p-2">日付</th>
          <th class="text-left p-2">タイトル</th>
          <th class="text-left p-2">本文</th>
          <th class="text-right p-2">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="n in filteredNews" :key="n.id" class="border-b">
          <td class="p-2 whitespace-nowrap">{{ n.date }}</td>
          <td class="p-2 font-medium">{{ n.title }}</td>
          <td class="p-2 text-gray-600 truncate max-w-[420px]">{{ n.content }}</td>
          <td class="p-2 text-right space-x-2 whitespace-nowrap">
            <button @click="$emit('view-news', n)" class="px-2 py-1 rounded border hover:bg-gray-50">
              <i class="fa-solid fa-eye"></i>
            </button>
            <button v-if="canEdit" @click="$emit('edit-news', n)" class="px-2 py-1 rounded border hover:bg-gray-50">
              <i class="fa-solid fa-pen"></i>
            </button>
            <button v-if="canEdit" @click="$emit('delete-news', n.id)" class="px-2 py-1 rounded border text-red-600 hover:bg-red-50">
              <i class="fa-solid fa-trash"></i>
            </button>
          </td>
        </tr>
        <tr v-if="loading">
          <td colspan="4" class="p-6 text-center text-gray-500">読み込み中...</td>
        </tr>
        <tr v-if="!loading && filteredNews.length===0">
          <td colspan="4" class="p-6 text-center text-gray-500">該当なし</td>
        </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div v-if="!loading && totalPages > 1" class="mt-4 flex items-center justify-center gap-2">
        <button
          @click="previousPage"
          :disabled="currentPage === 0"
          class="px-3 py-1 rounded border hover:bg-gray-50 disabled:opacity-50">
          前へ
        </button>
        <span class="px-3 py-1">{{ currentPage + 1 }} / {{ totalPages }}</span>
        <button
          @click="nextPage"
          :disabled="currentPage >= totalPages - 1"
          class="px-3 py-1 rounded border hover:bg-gray-50 disabled:opacity-50">
          次へ
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'NewsListComponent',
  props: {
    canEdit: {
      type: Boolean,
      default: false
    }
  },
  emits: ['create-news', 'edit-news', 'delete-news', 'view-news'],
  data () {
    return {
      searchQuery: '',
      loading: false,
      newsData: [],
      currentPage: 0,
      totalPages: 0,
      totalElements: 0
    }
  },
  computed: {
    filteredNews () {
      const q = this.searchQuery.trim().toLowerCase()
      return this.newsData.filter(n => !q || (n.title + (n.content || '')).toLowerCase().includes(q))
    }
  },
  watch: {
    // Note: Search filtering is handled client-side in filteredNews computed property
    // No need to call fetchNews() for search since we filter locally
  },
  async mounted () {
    await this.fetchNews()
  },
  methods: {
    async fetchNews () {
      this.loading = true
      try {
        const response = await axios.get(`/api/news?page=${this.currentPage}&size=5`)
        this.newsData = response.data.content || []
        this.totalPages = response.data.totalPages || 0
        this.totalElements = response.data.totalElements || 0
      } catch (error) {
        console.error('Failed to fetch news:', error)
        this.newsData = []
      } finally {
        this.loading = false
      }
    },
    async refreshNews () {
      // Reset to first page and fetch news
      this.currentPage = 0
      await this.fetchNews()
    },
    async nextPage () {
      if (this.currentPage < this.totalPages - 1) {
        this.currentPage++
        await this.fetchNews()
      }
    },
    async previousPage () {
      if (this.currentPage > 0) {
        this.currentPage--
        await this.fetchNews()
      }
    }
  }
}
</script>
