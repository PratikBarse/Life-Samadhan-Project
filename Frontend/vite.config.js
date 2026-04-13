import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd());
  const isDotnet = env.VITE_BACKEND_TYPE === 'DOTNET';
  const target = isDotnet ? 'http://localhost:5055' : 'http://localhost:8080';

  return {
    plugins: [react()],
    define: {
      global: 'window',
    },
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
        },
        '/ws': {
          target: 'http://localhost:8080',
          ws: true,
          changeOrigin: true,
          secure: false,
        },
        '/hubs': {
          target: 'http://localhost:8080',
          ws: true,
          changeOrigin: true,
          secure: false,
        }
      },
    },
  }
})
