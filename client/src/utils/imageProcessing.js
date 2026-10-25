const MAX_INPUT_SIZE = 10 * 1024 * 1024
const MAX_OUTPUT_SIZE = 2 * 1024 * 1024
const ALLOWED_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp'])

export async function processProductImage(file) {
  if (!file || !ALLOWED_TYPES.has(file.type)) {
    throw new Error('Choose a JPEG, PNG, or WebP image.')
  }
  if (file.size > MAX_INPUT_SIZE) {
    throw new Error('Choose an image smaller than 10 MB.')
  }

  const image = await loadImage(file)
  for (const maxDimension of [800, 700, 600, 500, 400]) {
    const canvas = drawOnWhiteCanvas(image, maxDimension)
    const blob = await encodeUnderLimit(canvas)
    if (blob) return blob
  }
  throw new Error('The processed image is still larger than 2 MB. Choose a simpler image.')
}

function loadImage(file) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file)
    const image = new Image()
    image.onload = () => {
      URL.revokeObjectURL(url)
      resolve(image)
    }
    image.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('The selected file could not be read as an image.'))
    }
    image.src = url
  })
}

function drawOnWhiteCanvas(image, maxDimension) {
  const canvas = document.createElement('canvas')
  canvas.width = maxDimension
  canvas.height = maxDimension
  const context = canvas.getContext('2d')
  context.fillStyle = '#ffffff'
  context.fillRect(0, 0, maxDimension, maxDimension)
  const scale = Math.min(maxDimension / image.naturalWidth, maxDimension / image.naturalHeight, 1)
  const width = Math.round(image.naturalWidth * scale)
  const height = Math.round(image.naturalHeight * scale)
  context.drawImage(image, Math.round((maxDimension - width) / 2), Math.round((maxDimension - height) / 2), width, height)
  return canvas
}

async function encodeUnderLimit(canvas) {
  const canEncodeWebp = await canvasToBlob(canvas, 'image/webp', 0.85)
  const type = canEncodeWebp?.type === 'image/webp' ? 'image/webp' : 'image/jpeg'
  for (const quality of [0.85, 0.75, 0.65, 0.55, 0.45]) {
    const blob = await canvasToBlob(canvas, type, quality)
    if (blob && blob.size <= MAX_OUTPUT_SIZE) return blob
  }
  return null
}

function canvasToBlob(canvas, type, quality) {
  return new Promise((resolve) => canvas.toBlob(resolve, type, quality))
}

export function formatImageSize(bytes) {
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}
