import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true }
    },
    // อนุญาตให้เข้าถึงผ่านโดเมนของ ngrok (สำหรับสาธิต/ทดสอบเท่านั้น)
    // true = อนุญาตทุกโดเมน — สะดวกตอน demo แต่ไม่ควรใช้แบบนี้กับระบบจริงที่ deploy ถาวร
    allowedHosts: true
  }
})
