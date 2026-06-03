import request from './request'

// 获取操作日志列表
export function listLogs(params) {
  return request.get('/operationLog/list', { params })
}

// 导出操作日志
export function exportLogs(params) {
  return request.get('/operationLog/export', {
    params,
    responseType: 'blob',
  })
}
