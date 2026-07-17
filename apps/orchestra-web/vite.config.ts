import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    host: '127.0.0.1',
    proxy: {
      '/api': {
        target: process.env.VITE_API_TARGET || 'http://localhost:8085',
        changeOrigin: true,
      },
      '/plantuml': {
        target: process.env.VITE_PLANTUML_TARGET || 'http://plantuml:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/plantuml/, ''),
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/setupTests.ts',
  },
});
