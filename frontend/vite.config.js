import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/auth': {
        target: 'http://localhost:8086',
        changeOrigin: true,
      },
      '/usuarios': {
        target: 'http://localhost:8086',
        changeOrigin: true,
      },
      '/api/blockchain': {
        target: 'http://localhost:8087',
        changeOrigin: true,
      },
    },
  },
})
