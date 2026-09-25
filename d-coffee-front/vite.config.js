import { defineConfig, loadEnv } from 'vite'
import uniModule from '@dcloudio/vite-plugin-uni'

const uni = uniModule.default ?? uniModule

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'VITE_')
  const apiContext = (env.VITE_API_CONTEXT || '/d_coffee_backend_war_exploded').replace(/\/$/, '')

  return {
    plugins: [uni()],
    server: {
      host: '0.0.0.0',
      port: 5173,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (path) => `${apiContext}${path}`,
        },
      },
    },
  }
})
