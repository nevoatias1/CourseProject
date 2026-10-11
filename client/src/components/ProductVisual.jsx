const categoryIcons = {
  Smartphones: 'bi-phone',
  Laptops: 'bi-laptop',
  Audio: 'bi-headphones',
  Smartwatches: 'bi-smartwatch',
  Gaming: 'bi-controller',
  Accessories: 'bi-mouse',
}

function ProductVisual({ categoryName, productName, detail = false }) {
  const icon = categoryIcons[categoryName] || 'bi-box-seam'
  return (
    <div className={`product-visual ${detail ? 'product-visual-detail' : ''}`} role="img" aria-label={`${productName} ${categoryName || 'product'} icon`}>
      <i className={`bi ${icon}`} aria-hidden="true" />
    </div>
  )
}

export default ProductVisual
