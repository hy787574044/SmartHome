import request from './request'

// 获取所有系统配置
export function listConfigs() {
  return request.get('/system/config')
}

// 更新系统配置（批量）
export function updateConfigs(data) {
  return request.post('/system/config/update', data)
}

// 获取系统信息
export function getSystemInfo() {
  return request.get('/system/info')
}

// 清理过期日志
export function cleanLogs(days) {
  return request.post('/system/cleanLogs', null, { params: { days } })
}
