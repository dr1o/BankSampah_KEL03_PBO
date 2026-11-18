import { defineConfig } from 'vite';
import { spawn, spawnSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import http from 'node:http';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

function collectJavaFiles(dir, list = []) {
  if (!fs.existsSync(dir)) return list;
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      collectJavaFiles(full, list);
    } else if (entry.isFile() && entry.name.endsWith('.java')) {
      list.push(full);
    }
  }
  return list;
}

function checkBackendAlive(port = 8080) {
  return new Promise((resolve) => {
    const req = http.get(`http://127.0.0.1:${port}/api/state`, { timeout: 1200 }, (res) => {
      res.resume();
      resolve(res.statusCode === 200);
    });
    req.on('error', () => resolve(false));
    req.on('timeout', () => {
      req.destroy();
      resolve(false);
    });
  });
}

function javaBackendPlugin() {
  let javaProcess = null;

  return {
    name: 'pure-java-oop-backend',
    async configureServer() {
      const alive = await checkBackendAlive(8080);
      if (alive) {
        console.log('\x1b[32m[Java OOP Backend] Server sudah aktif di http://127.0.0.1:8080\x1b[0m');
        return;
      }

      const outDir = path.join(__dirname, 'out');
      if (!fs.existsSync(outDir)) {
        fs.mkdirSync(outDir, { recursive: true });
      }

      const sep = process.platform === 'win32' ? ';' : ':';
      const javaFiles = collectJavaFiles(path.join(__dirname, 'src'));
      console.log('\x1b[33m[Java OOP Backend] Mengompilasi 15 kelas domain PBO & HTTP Server...\x1b[0m');

      const compile = spawnSync(
        'javac',
        ['-encoding', 'UTF-8', '-cp', `lib/*`, '-d', 'out', ...javaFiles],
        { cwd: __dirname, stdio: 'inherit' }
      );

      if (compile.status !== 0) {
        console.error('\x1b[31m[Java OOP Backend] Gagal mengompilasi kode Java!\x1b[0m');
        return;
      }

      console.log('\x1b[36m[Java OOP Backend] Menjalankan banksampah.server.WebServerMain di port 8080...\x1b[0m');
      javaProcess = spawn(
        'java',
        ['-cp', `out${sep}lib/*`, 'banksampah.server.WebServerMain'],
        { cwd: __dirname, stdio: 'inherit' }
      );

      const cleanup = () => {
        if (javaProcess && !javaProcess.killed) {
          javaProcess.kill();
          javaProcess = null;
        }
      };

      process.on('exit', cleanup);
      process.on('SIGINT', () => {
        cleanup();
        process.exit(0);
      });
      process.on('SIGTERM', () => {
        cleanup();
        process.exit(0);
      });

      // Tunggu hingga port 8080 siap melayani request pertama
      for (let i = 0; i < 25; i++) {
        if (await checkBackendAlive(8080)) {
          break;
        }
        await new Promise((r) => setTimeout(r, 250));
      }
    }
  };
}

export default defineConfig({
  root: 'frontend',
  plugins: [javaBackendPlugin()],
  server: {
    port: 5173,
    strictPort: false,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: '../dist',
    emptyOutDir: true
  }
});
