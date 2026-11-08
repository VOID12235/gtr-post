import sharp from 'sharp'
import { readFileSync } from 'node:fs'

const logo = readFileSync('public/brand/logo.svg')
const mark = readFileSync('public/brand/logo-mark.svg')

await sharp(logo).resize(192, 192).png().toFile('public/brand/icon-192.png')
await sharp(logo).resize(512, 512).png().toFile('public/brand/icon-512.png')

const maskable = await sharp({
  create: {
    width: 512,
    height: 512,
    channels: 4,
    background: '#050505'
  }
})
  .composite([{ input: await sharp(mark).resize(360, 360).png().toBuffer(), top: 76, left: 76 }])
  .png()
  .toFile('public/brand/icon-maskable-512.png')

await sharp(logo).resize(180, 180).png().toFile('public/brand/apple-touch-icon.png')

console.log('icons generated')
