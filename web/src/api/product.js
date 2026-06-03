import request from './request'

export function addProduct(data) {
  return request.post('/product/update', data)
}

export function updateProduct(data) {
  return request.post('/product/update', data)
}

export function deleteProduct(productId) {
  return request.delete(`/product/${productId}`)
}

export function getProduct(productId) {
  return request.get(`/product/${productId}`)
}

export function listProducts(params) {
  return request.get('/product/list', { params })
}

export function listAllProducts() {
  return request.get('/product/all')
}

// 物模型
export function addThingsModel(data) {
  return request.post('/product/model/update', data)
}

export function updateThingsModel(data) {
  return request.post('/product/model/update', data)
}

export function deleteThingsModel(modelId) {
  return request.delete(`/product/model/${modelId}`)
}

export function listThingsModels(productId) {
  return request.get(`/product/${productId}/models`)
}
