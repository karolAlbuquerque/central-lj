import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

const apiProxy = {
  "/api": {
    target: "http://localhost:8080",
    changeOrigin: true,
    timeout: 0
  }
};

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { ...apiProxy }
  },
  /** `npm run preview` também precisa de proxy para /api, senão o login retorna 404 no dev local. */
  preview: {
    port: 4173,
    proxy: { ...apiProxy }
  }
});

