import request from './request'

export function listDeviceGroups(params) {
  return request.get('/deviceGroup/list', { params })
}

export function getDeviceGroup(groupId) {
  return request.get(`/deviceGroup/${groupId}`)
}

export function createDeviceGroup(data) {
  return request.post('/deviceGroup', data)
}

export function updateDeviceGroup(data) {
  return request.put('/deviceGroup', data)
}

export function deleteDeviceGroup(groupId) {
  return request.delete(`/deviceGroup/${groupId}`)
}

export function controlDeviceGroup(groupId, identifier, value) {
  return request.post(`/deviceGroup/${groupId}/control`, null, {
    params: { identifier, value },
  })
}
