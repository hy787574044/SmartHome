import request from './request'

// 设备管理
export function addDevice(data) {
  return request.post('/device/update', data)
}

export function updateDevice(data) {
  return request.post('/device/update', data)
}

export function deleteDevice(deviceId) {
  return request.delete(`/device/${deviceId}`)
}

export function getDevice(deviceId) {
  return request.get(`/device/${deviceId}`)
}

export function listDevices(params) {
  return request.get('/device/list', { params })
}

export function getDeviceStatus(deviceId) {
  return request.get(`/device/${deviceId}/status`)
}

export function listDevicesByRoom(roomId) {
  return request.get(`/device/room/${roomId}`)
}

export function listOnlineDevices() {
  return request.get('/device/online')
}

export function controlDevice(deviceId, identifier, value) {
  return request.post(`/device/${deviceId}/control`, null, {
    params: { identifier, value },
  })
}

export function getDeviceDetail(deviceId) {
  return request.get(`/device/${deviceId}/detail`)
}

export function getDeviceHistory(deviceId, params) {
  return request.get(`/device/${deviceId}/history`, { params })
}
