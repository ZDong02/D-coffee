const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '')

export function request(path, options = {}) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${API_BASE_URL}${path}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        ...(uni.getStorageSync('dcoffee-user-token')
          ? { Authorization: `Bearer ${uni.getStorageSync('dcoffee-user-token')}` }
          : {}),
        ...(options.header || {}),
      },
      timeout: 10000,
      success(response) {
        const body = response.data
        if (response.statusCode >= 200 && response.statusCode < 300 && body?.code === 200) {
          resolve(body.data)
          return
        }
        reject(new Error(body?.message || `请求失败（${response.statusCode}）`))
      },
      fail(error) {
        reject(new Error(error.errMsg || '无法连接商品服务'))
      },
    })
  })
}

export const getCategories = () => request('/categories')
export const getStores = () => request('/stores')
export const userLogin = (data) => request('/user/login', { method: 'POST', data })
export const userRegister = (data) => request('/user/register', { method: 'POST', data })
export const getCart = (storeId) => request(`/user/cart?storeId=${encodeURIComponent(storeId)}`)
export const addCartItem = (data) => request('/user/cart/items', { method: 'POST', data })
export const updateCartItem = (itemId, storeId, quantity) => request(`/user/cart/items/${itemId}?storeId=${storeId}`, { method: 'PATCH', data: { quantity } })
export const removeCartItem = (itemId, storeId) => request(`/user/cart/items/${itemId}?storeId=${storeId}`, { method: 'DELETE' })
export const createOrder = (data) => request('/user/orders', { method: 'POST', data })
export const getOrders = () => request('/user/orders')
export const getOrder = (orderId) => request(`/user/orders/${encodeURIComponent(orderId)}`)
export const cancelOrder = (orderId) => request(`/user/orders/${encodeURIComponent(orderId)}/cancel`, { method: 'POST' })
export const getProduct = (id) => request(`/products/${encodeURIComponent(id)}`)
export const getProducts = (params = {}) => {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&')
  return request(`/products${query ? `?${query}` : ''}`)
}
