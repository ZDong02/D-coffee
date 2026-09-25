import http from './http.js'

export const adminLogin = (credentials) => http.post('/auth/admin/login', credentials)
export const fetchCategories = () => http.get('/categories')
export const fetchProducts = (params) => http.get('/admin/products', { params })
export const fetchProduct = (id) => http.get(`/admin/products/${id}`)
export const createProduct = (data) => http.post('/admin/products', data)
export const updateProduct = (id, data) => http.put(`/admin/products/${id}`, data)
export const updateProductStatus = (id, status) => http.patch(`/admin/products/${id}/status`, { status })
export const adjustProductInventory = (id, data) => http.patch(`/admin/products/${id}/inventory`, data)
export const updateProductConfiguration = (id, data) => http.put(`/admin/products/${id}/configuration`, data)
