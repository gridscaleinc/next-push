<template>
  <div class="mt-2">
    <h4 class="text-sm font-medium mb-2">ユーザー登録（管理者のみ）</h4>
    <label class="block text-sm">氏名
      <input v-model="userForm.name" class="mt-1 w-full input" placeholder="山田 太郎">
    </label>
    <label class="block text-sm mt-2">メール
      <input v-model="userForm.email" type="email" class="mt-1 w-full input" placeholder="taro@example.com">
    </label>
    <div class="flex gap-2 mt-2">
      <label class="block text-sm flex-1">権限
        <select v-model="userForm.role" class="mt-1 w-full input">
          <option value="admin">管理者</option>
          <option value="editor">編集者</option>
          <option value="viewer">閲覧のみ</option>
        </select>
      </label>
      <label class="block text-sm w-24">状態
        <select v-model="userForm.active" class="mt-1 w-full input">
          <option :value="true">有効</option>
          <option :value="false">無効</option>
        </select>
      </label>
    </div>
    <label class="block text-sm mt-2">初期パスワード
      <div class="flex gap-2 mt-1">
        <input v-model="userForm.tempPass" type="text" class="flex-1 input" placeholder="英数8文字以上">
        <button @click="generateTempPass" class="px-2 rounded border">生成</button>
      </div>
    </label>
    <label class="block text-sm mt-2">メモ
      <textarea v-model="userForm.note" rows="2" class="mt-1 w-full input" placeholder="部署や備考など"></textarea>
    </label>
    <button @click="createUser" class="mt-3 w-full px-3 py-2 rounded-lg btn-primary">
      <i class="fa-solid fa-user-plus mr-1"></i> 登録
    </button>

    <div class="mt-3 text-xs text-gray-600">
      登録数: {{ users.length }}
    </div>
    <div class="mt-2">
      <div class="text-xs text-gray-500 mb-1">最近登録</div>
      <ul class="space-y-1 max-h-28 overflow-auto">
        <li v-for="u in recentUsers" :key="u.id" class="text-sm flex justify-between gap-2">
          <span class="truncate">{{ u.name }} <span class="text-gray-500">・{{ roleLabel(u.role) }}</span></span>
          <span class="text-xs text-gray-400 whitespace-nowrap">{{ (u.createdAt||'').replace('T',' ').slice(5,16) }}</span>
        </li>
      </ul>
    </div>
    <button @click="relockAdmin" class="mt-3 w-full px-3 py-2 rounded border">
      ロック
    </button>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'UserRegistrationComponent',
  props: {
    users: {
      type: Array,
      required: true
    },
    security: {
      type: Object,
      required: true
    },
    adminUnlocked: {
      type: Boolean,
      required: true
    }
  },
  emits: ['user-created', 'admin-relocked', 'password-generated'],
  data () {
    return {
      userForm: { name: '', email: '', role: 'editor', active: true, tempPass: '', note: '' }
    }
  },
  computed: {
    recentUsers () {
      return [...this.users].sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || '')).slice(0, 5)
    }
  },
  methods: {
    roleLabel (r) {
      return r === 'admin' ? '管理者' : (r === 'editor' ? '編集者' : '閲覧のみ')
    },
    generateTempPass () {
      const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%^&*'
      let s = ''
      for (let i = 0; i < 12; i++) {
        s += chars[Math.floor(Math.random() * chars.length)]
      }
      this.userForm.tempPass = s
      this.$emit('password-generated', s)
    },
    async createUser () {
      if (this.security.adminEnabled && !this.adminUnlocked) {
        alert('管理者ロックを解除してください')
        return
      }
      const f = this.userForm
      if (!f.name.trim()) return alert('氏名は必須です')
      if (!f.email.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.email)) return alert('メールの形式が不正です')
      if (!f.tempPass || f.tempPass.length < 8) return alert('初期パスワードは8文字以上にしてください')

      try {
        // Create user object for backend API
        const userData = {
          userName: f.name.trim(),
          email: f.email.trim(),
          userType: f.role,
          activeFlag: !!f.active,
          password: f.tempPass
        }

        // Send POST request to backend
        const response = await axios.post('/api/users', userData)

        // Create user object for frontend display (with local structure)
        const user = {
          id: response.data.userId,
          name: response.data.userName,
          email: response.data.email,
          role: response.data.userType,
          active: response.data.activeFlag,
          note: f.note || '',
          createdAt: new Date().toISOString(),
          auth: { salt: '', hash: '' }
        }

        this.$emit('user-created', user)
        this.userForm = { name: '', email: '', role: 'editor', active: true, tempPass: '', note: '' }
        alert('ユーザーを登録しました（初期パスワードは本人へ安全に共有してください）')
      } catch (error) {
        console.error('Failed to create user:', error)
        if (error.response && error.response.status === 409) {
          alert('このメールアドレスは既に使用されています')
        } else if (error.response && error.response.status === 400) {
          alert('入力内容に不備があります')
        } else {
          alert('ユーザーの登録に失敗しました')
        }
      }
    },
    relockAdmin () {
      this.$emit('admin-relocked')
    }
  }
}
</script>
