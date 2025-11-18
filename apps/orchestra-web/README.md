# orchestra-web

## Prerequisites

- Node.js 18+ and npm
- Running `orchestra-api` instance available on `http://localhost:8080`

## Local development

```bash
cd apps/orchestra-web
npm install
npm start
```

Vite dev server listens on http://localhost:3000 and proxies `/api/*` requests to the backend.

## Build & test

```bash
npm run build   # type-check + production build in dist/
npm run test    # runs Vitest in jsdom environment
```
