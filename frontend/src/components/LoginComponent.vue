<template>
  <div class="login-overlay" v-if="showLogin">
    <div class="login-modal">
      <div class="login-header">
        <h3 class="font-semibold text-lg">ログイン</h3>
        <button v-if="!forceLogin" @click="closeLogin" class="close-button">×</button>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="email">メールアドレス</label>
          <input
            id="email"
            v-model="loginForm.email"
            type="email"
            class="input w-full"
            placeholder="example@gridscale.com"
            required
            :disabled="isLoading"
          >
        </div>

        <div class="form-group">
          <label for="password">パスワード</label>
          <input
            id="password"
            v-model="loginForm.password"
            type="password"
            class="input w-full"
            placeholder="パスワードを入力"
            required
            :disabled="isLoading"
          >
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <div class="login-actions">
          <button
            v-if="!forceLogin"
            type="button"
            @click="closeLogin"
            class="btn-secondary"
            :disabled="isLoading"
          >
            キャンセル
          </button>
          <button
            type="submit"
            class="btn-primary"
            :disabled="isLoading || !loginForm.email || !loginForm.password"
            :class="forceLogin ? 'w-full' : ''"
          >
            <span v-if="isLoading">ログイン中...</span>
            <span v-else>ログイン</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'LoginComponent',
  props: {
    showLogin: {
      type: Boolean,
      default: false
    },
    forceLogin: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close', 'login-success'],
  data () {
    return {
      loginForm: {
        email: '',
        password: ''
      },
      isLoading: false,
      errorMessage: ''
    }
  },
  methods: {
    closeLogin () {
      // Prevent closing when login is forced
      if (this.forceLogin) {
        return
      }
      this.resetForm()
      this.$emit('close')
    },

    resetForm () {
      this.loginForm = {
        email: '',
        password: ''
      }
      this.errorMessage = ''
      this.isLoading = false
    },

    async handleLogin () {
      this.isLoading = true
      this.errorMessage = ''

      try {
        const response = await axios.post('/api/auth/login', {
          email: this.loginForm.email,
          password: this.loginForm.password
        })

        const { token, userId, userName, email, userType } = response.data

        // Store authentication data
        const authData = {
          token,
          userId,
          userName,
          email,
          userType,
          loginTime: new Date().toISOString()
        }

        localStorage.setItem('auth', JSON.stringify(authData))

        // Set default authorization header for future requests
        axios.defaults.headers.common.Authorization = `Bearer ${token}`

        // Emit success event with user data
        this.$emit('login-success', authData)

        // Reset and close form
        this.resetForm()

        alert('ログインに成功しました')
      } catch (error) {
        console.error('Login failed:', error)

        if (error.response) {
          const status = error.response.status
          const message = error.response.data?.message

          if (status === 401) {
            this.errorMessage = message || 'メールアドレスまたはパスワードが正しくありません'
          } else if (status === 400) {
            this.errorMessage = message || '入力内容を確認してください'
          } else {
            this.errorMessage = message || 'ログインに失敗しました'
          }
        } else {
          this.errorMessage = 'ネットワークエラーが発生しました'
        }
      } finally {
        this.isLoading = false
      }
    }
  }
}
</script>

<style scoped>
.login-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.login-modal {
  background: white;
  border-radius: 12px;
  padding: 0;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

.login-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6b7280;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}

.close-button:hover {
  background-color: #f3f4f6;
  color: #374151;
}

.login-form {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.error-message {
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.login-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

.btn-secondary {
  padding: 0.5rem 1rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background-color: white;
  color: #374151;
  cursor: pointer;
  font-size: 0.875rem;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #f9fafb;
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  padding: 0.5rem 1rem;
  background-image: linear-gradient(90deg, #6366f1, #06b6d4);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
}

.btn-primary:hover:not(:disabled) {
  filter: brightness(1.05);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
