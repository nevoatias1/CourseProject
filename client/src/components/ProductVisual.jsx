import { useEffect, useState } from 'react'
import { getApiImageUrl } from '../api.js'

const categoryIcons = {
  Smartphones: 'bi-phone',
  Laptops: 'bi-laptop',
  Audio: 'bi-headphones',
  Smartwatches: 'bi-smartwatch',
  Gaming: 'bi-controller',
  Accessories: 'bi-mouse',
}

function ProductVisual({ categoryName, productName, imageUrl, previewUrl, detail = false, className = '' }) {
  const icon = categoryIcons[categoryName] || 'bi-box-seam'
  const [imageFailed, setImageFailed] = useState(false)
  const source = previewUrl || getApiImageUrl(imageUrl)

  useEffect(() => {
    setImageFailed(false)
  }, [source])

  if (source && !imageFailed) {
    return <div className={`product-visual product-visual-has-image ${detail ? 'product-visual-detail' : ''} ${className}`}>
      <img src={source} alt={productName} loading="lazy" onError={() => setImageFailed(true)} />
    </div>
  }

  return (
    <div className={`product-visual ${detail ? 'product-visual-detail' : ''} ${className}`} role="img" aria-label={`${productName} ${categoryName || 'product'} icon`}>
      <i className={`bi ${icon}`} aria-hidden="true" />
    </div>
  )
}

export default ProductVisual
