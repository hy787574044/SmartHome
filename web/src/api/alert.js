import request from './request'

// 告警规则
export function createAlertRule(data) {
  return request.post('/alert/rule', data)
}

export function updateAlertRule(data) {
  return request.put('/alert/rule', data)
}

export function deleteAlertRule(alertId) {
  return request.delete(`/alert/rule/${alertId}`)
}

export function listAlertRules(deviceId) {
  return request.get('/alert/rule/list', { params: { deviceId } })
}

// 告警日志
export function listAlertLogs(params) {
  return request.get('/alert/log/list', { params })
}

export function handleAlert(logId, remark) {
  return request.post(`/alert/log/${logId}/handle`, null, { params: { remark } })
}

export function ignoreAlert(logId, remark) {
  return request.post(`/alert/log/${logId}/ignore`, null, { params: { remark } })
}
