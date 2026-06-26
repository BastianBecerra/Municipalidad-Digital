import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // Carga variables de entorno del sistema y de archivos .env sin prefijo VITE_
  const env = loadEnv(mode, process.cwd(), '')
  const gatewayTarget = process.env.VITE_PROXY_GATEWAY || env.VITE_PROXY_GATEWAY || 'http://localhost:8080'

  return {
    plugins: [react()],
    server: {
      host: '0.0.0.0', // Obligatorio para desarrollo en contenedor Docker
      port: 5173,
      watch: {
        usePolling: true, // Asegura que HMR detecte cambios en volúmenes Docker bajo Windows/WSL
      },
      proxy: {
        '/auth': { target: gatewayTarget, changeOrigin: true },
        '/usuarios': { target: gatewayTarget, changeOrigin: true },
        '/territorios': { target: gatewayTarget, changeOrigin: true },
        '/api/blockchain': { target: gatewayTarget, changeOrigin: true },
        '/documentos': { target: gatewayTarget, changeOrigin: true },
        '/api/validacion': { target: gatewayTarget, changeOrigin: true },
      },
    },
    test: {
      globals: true,
      environment: 'jsdom',
      setupFiles: './src/setupTests.js',
    },
  }
})


