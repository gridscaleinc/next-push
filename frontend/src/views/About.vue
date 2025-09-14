<template>
  <div class="about">
    <div class="about-content">
      <h1>About Next Push</h1>
      <p class="intro">
        Next Push is a full-stack web application template that combines the power of 
        Spring Boot for the backend with the modern Vue.js framework for the frontend.
      </p>

      <div class="tech-stack">
        <h2>Technology Stack</h2>
        
        <div class="stack-section">
          <h3>🏗️ Backend Technologies</h3>
          <ul>
            <li><strong>Spring Boot 3.2.0</strong> - Enterprise-grade Java framework</li>
            <li><strong>Spring Data JPA</strong> - Database abstraction layer</li>
            <li><strong>H2 Database</strong> - In-memory database for development</li>
            <li><strong>Maven</strong> - Build automation and dependency management</li>
          </ul>
        </div>

        <div class="stack-section">
          <h3>🎨 Frontend Technologies</h3>
          <ul>
            <li><strong>Vue.js 3</strong> - Progressive JavaScript framework</li>
            <li><strong>Vue Router 4</strong> - Client-side routing</li>
            <li><strong>Pinia</strong> - State management library</li>
            <li><strong>Axios</strong> - HTTP client for API calls</li>
          </ul>
        </div>

        <div class="stack-section">
          <h3>🔧 Development Tools</h3>
          <ul>
            <li><strong>Spring Boot DevTools</strong> - Hot reload for backend</li>
            <li><strong>Vue CLI Service</strong> - Frontend build tools</li>
            <li><strong>ESLint</strong> - Code quality and consistency</li>
            <li><strong>Frontend Maven Plugin</strong> - Integrated build process</li>
          </ul>
        </div>
      </div>

      <div class="project-info">
        <h2>Project Structure</h2>
        <p>
          This project follows a standard Maven structure with the backend code in 
          <code>src/main/java</code> and the frontend code in the <code>frontend/</code> 
          directory. The build process automatically compiles the Vue.js application and 
          serves it through Spring Boot's static resource handling.
        </p>
      </div>

      <div class="getting-started">
        <h2>Getting Started</h2>
        <ol>
          <li>Run <code>mvn spring-boot:run</code> to start the backend server</li>
          <li>Navigate to <code>http://localhost:8080</code> to view the application</li>
          <li>Access the H2 database console at <code>http://localhost:8080/h2-console</code></li>
          <li>For frontend development, use <code>npm run serve</code> in the frontend directory</li>
        </ol>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'About'
}
</script>

<style scoped>
.about {
  max-width: 800px;
  margin: 0 auto;
}

.about-content {
  line-height: 1.6;
}

.about h1 {
  color: #2c3e50;
  margin-bottom: 1.5rem;
  font-size: 2.5rem;
}

.intro {
  font-size: 1.2rem;
  color: #7f8c8d;
  margin-bottom: 3rem;
}

.tech-stack h2,
.project-info h2,
.getting-started h2 {
  color: #2c3e50;
  margin-bottom: 1.5rem;
  margin-top: 2rem;
}

.stack-section {
  background: #f8f9fa;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

.stack-section h3 {
  color: #2c3e50;
  margin-bottom: 1rem;
}

.stack-section ul {
  list-style: none;
  padding-left: 0;
}

.stack-section li {
  margin-bottom: 0.5rem;
  padding-left: 1rem;
}

.stack-section li:before {
  content: "▶";
  color: #3498db;
  margin-right: 0.5rem;
  margin-left: -1rem;
}

code {
  background-color: #f1f2f6;
  padding: 0.2rem 0.4rem;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  color: #e74c3c;
}

.getting-started ol {
  padding-left: 1.5rem;
}

.getting-started li {
  margin-bottom: 0.5rem;
}

.project-info p {
  color: #7f8c8d;
  margin-bottom: 1rem;
}
</style>