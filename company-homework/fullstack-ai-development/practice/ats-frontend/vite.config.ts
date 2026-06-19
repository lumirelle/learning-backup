import process from 'node:process'
import { fileURLToPath, URL } from 'node:url'
import vue from '@vitejs/plugin-vue'
import Unocss from 'unocss/vite'
import { defineConfig, loadEnv } from 'vite'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiBase = env.VITE_API_BASE_URL || '/api/v1'

  return {
    // UnoCSS 必须在 vue 之前，以保证 attributify 模板里的属性能被扫描
    plugins: [Unocss(), vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      host: true, // 0.0.0.0，局域网设备可通过本机 IP:5173 访问
      port: 5173,
      strictPort: false,
      proxy: {
        [apiBase]: {
          target: 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
    build: {
      target: 'es2022',
      sourcemap: false,
      chunkSizeWarningLimit: 1024,
    },
  }
})
