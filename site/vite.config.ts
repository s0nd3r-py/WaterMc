import { resolve } from "node:path"
import tailwindcss from "@tailwindcss/vite"
import react from "@vitejs/plugin-react"
import { defineConfig } from "vite"

// GitHub Pages serves the site from https://s0nd3r-py.github.io/WaterMc/
export default defineConfig({
  base: "/WaterMc/",
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": resolve(import.meta.dirname, "./src"),
    },
  },
  build: {
    rollupOptions: {
      input: {
        main: resolve(import.meta.dirname, "index.html"),
        config: resolve(import.meta.dirname, "config/index.html"),
      },
    },
  },
})
