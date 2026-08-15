import { defineConfig } from 'vite';
import path from 'path';

export default defineConfig({
  root: '.',
  build: {
    outDir: path.resolve(__dirname, '../out'),
    emptyOutDir: true,
  },
  server: {
    port: 3000,
    open: true
  }
});
