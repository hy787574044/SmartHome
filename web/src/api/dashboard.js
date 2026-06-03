import request from './request'

export function getDashboardStats() {
  return request.get('/dashboard/stats')
}

export function getRealtimeSensorData() {
  return request.get('/dashboard/realtime-sensors')
}
