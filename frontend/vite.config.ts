import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import dns from 'dns'

// https://vitejs.dev/config/

dns.setDefaultResultOrder('verbatim') // uses 'localhost' instead of ip

export default defineConfig({
    plugins: [
        vue({
            reactivityTransform: true
        })
    ],
    server: {
        strictPort: true,
        port: 3000,
        proxy: {
            "/api": "http://localhost:8080",
            "/auth": "http://localhost:8080",
        }
    }
})
