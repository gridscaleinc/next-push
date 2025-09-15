<template>
  <div class="home">
    <div class="hero-section">
      <h1>Welcome to Next Push</h1>
      <p class="hero-subtitle">A modern web application built with Spring Boot and Vue.js</p>

      <div class="features-grid">
        <div class="feature-card">
          <h3>🚀 Spring Boot Backend</h3>
          <p>Powerful REST API with Spring Boot, JPA, and H2 database</p>
        </div>
        <div class="feature-card">
          <h3>💚 Vue.js Frontend</h3>
          <p>Modern, reactive user interface built with Vue 3 and Vue Router</p>
        </div>
        <div class="feature-card">
          <h3>🔧 Integrated Build</h3>
          <p>Maven handles both backend and frontend builds seamlessly</p>
        </div>
      </div>

      <div class="api-test">
        <button @click="testBackendConnection" class="test-button">
          Test Backend Connection
        </button>
        <div v-if="connectionStatus" class="status-message" :class="connectionStatus.type">
          {{ connectionStatus.message }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Home',
  data () {
    return {
      connectionStatus: null
    }
  },
  methods: {
    async testBackendConnection () {
      this.connectionStatus = null
      try {
        await axios.get('/api/health')
        this.connectionStatus = {
          type: 'success',
          message: 'Backend connection successful!'
        }
      } catch (error) {
        this.connectionStatus = {
          type: 'error',
          message: 'Backend connection failed. Make sure Spring Boot is running.'
        }
      }
    }
  }
}
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
}

.hero-section {
  text-align: center;
  padding: 2rem 0;
}

.hero-section h1 {
  font-size: 3rem;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.hero-subtitle {
  font-size: 1.2rem;
  color: #7f8c8d;
  margin-bottom: 3rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.feature-card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-card h3 {
  color: #2c3e50;
  margin-bottom: 1rem;
  font-size: 1.5rem;
}

.feature-card p {
  color: #7f8c8d;
  line-height: 1.6;
}

.api-test {
  margin-top: 2rem;
}

.test-button {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 1rem 2rem;
  font-size: 1rem;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.test-button:hover {
  background-color: #2980b9;
}

.status-message {
  margin-top: 1rem;
  padding: 1rem;
  border-radius: 5px;
  font-weight: bold;
}

.status-message.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.status-message.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}
</style>
