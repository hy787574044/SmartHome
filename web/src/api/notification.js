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
  return request.post('/notification/config', data)
}

export function deleteNotificationConfig(configId) {
  return request.delete(`/notification/config/${configId}`)
}

export function testNotification(notifyType, message) {
  return request.post('/notification/test', null, { params: { notifyType, message } })
}

export function updateQuietHours(data) {
  return request.post('/notification/quiet-hours', data)
}

export function getQuietHours() {
  return request.get('/notification/quiet-hours')
}
