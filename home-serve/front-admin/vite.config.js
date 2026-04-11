import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

const portMap = {
  'front-user': 5173,
  'front-worker': 5174,
  'front-admin': 5175
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const projectName = path.basename(process.cwd())
  const port = portMap[projectName] || 5175
  
  return {
    plugins: [
      vue(),
      AutoImport({
        resolvers: [ElementPlusResolver()]
      }),
      Components({
        resolvers: [ElementPlusResolver()]
      })
    ],
    server: {
      port,
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true
        }
      }
    },
    build: {
      rollupOptions: {
        output: {
          manualChunks: {
            'element-plus': ['element-plus'],
            'vue-vendor': ['vue', 'vue-router', 'pinia', 'axios']
          }
        }
      }
    }
  }
})