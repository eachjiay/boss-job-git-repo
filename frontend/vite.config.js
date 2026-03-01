import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: [
            { find: '@', replacement: resolve(__dirname, 'src') },
            // Forcefully trap the ghost import that breaks Rollup
            { find: /.*use-window-config\.mjs$/, replacement: 'element-plus/es/hooks/use-id/index.mjs' },
            { find: /.*use-prevent-window\/index\.mjs$/, replacement: 'element-plus/es/hooks/use-id/index.mjs' }
        ]
    },
    server: {
        port: 5173,
        proxy: {
            '/api': {
                target: 'http://localhost:8081',
                changeOrigin: true
            }
        }
    },
    define: {
        global: 'window'
    }
})
