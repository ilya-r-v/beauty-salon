import { defineConfig } from "vite";
export default defineConfig({
  server: {
    proxy: {
      "/api": "http://localhost:8080",
      "/v3": "http://localhost:8080",
      "/swagger-ui": "http://localhost:8080",
    },
  },
});
