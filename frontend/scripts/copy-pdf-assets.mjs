import { cpSync, rmSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

const root = new URL('../', import.meta.url)
const source = new URL('node_modules/pdfjs-dist/', root)
const target = new URL('public/pdfjs/', root)

// 中文字型必需
rmSync(fileURLToPath(target), { recursive: true, force: true })
for (const folder of ['cmaps', 'standard_fonts', 'wasm', 'iccs']) {
  cpSync(fileURLToPath(new URL(folder, source)), fileURLToPath(new URL(folder, target)), {
    recursive: true,
  })
}
