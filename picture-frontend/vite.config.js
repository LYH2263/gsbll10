import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import WindiCSS from 'vite-plugin-windicss'

export default defineConfig({
  plugins: [
    vue(),
    WindiCSS(),
  ],
  server: {
    port: 4140,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:8140',
        changeOrigin: true
      },
      '/images': {
        target: 'http://localhost:8140',
        changeOrigin: true
      }
    }
  }
})
