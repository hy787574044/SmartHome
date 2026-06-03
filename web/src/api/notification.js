import request from './request'

export function listNotificationConfigs() {
  return request.get('/notification/config')
}

export function getNotificationConfig(configId) {
  return request.get(`/notification/config/${configId}`)
}

export function addNotificationConfig(data) {
  return request.post('/notification/config', data)
}

export function updateNotificationConfig(data) {
  return request.put('/notification/config', data)
}

export function deleteNotificationConfig(configId) {
  return request.delete(`/notification/config/${configId}`)
}

export function testNotification(configId) {
  return request.post(`/notification/config/${configId}/test`)
}

export function updateQuietHours(data) {
  return request.put('/notification/quiet-hours', data)
}

export function getQuietHours() {
  return request.get('/notification/quiet-hours')
}
