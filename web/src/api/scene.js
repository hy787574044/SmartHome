import request from './request'

export function createScene(data) {
  return request.post('/scene', data)
}

export function updateScene(data) {
  return request.put('/scene', data)
}

export function deleteScene(sceneId) {
  return request.delete(`/scene/${sceneId}`)
}

export function getScene(sceneId) {
  return request.get(`/scene/${sceneId}`)
}

export function listScenes() {
  return request.get('/scene/list')
}

export function listTriggers(sceneId) {
  return request.get(`/scene/${sceneId}/triggers`)
}

export function listActions(sceneId) {
  return request.get(`/scene/${sceneId}/actions`)
}

export function executeScene(sceneId) {
  return request.post(`/scene/${sceneId}/execute`)
}

export function getSceneTemplates() {
  return request.get('/scene/templates')
}

export function createSceneFromTemplate(templateId, data) {
  return request.post(`/scene/templates/${templateId}/create`, data)
}

export function copyScene(sceneId) {
  return request.post(`/scene/${sceneId}/copy`)
}

export function getSceneLogs(params) {
  return request.get('/scene/logs', { params })
}
