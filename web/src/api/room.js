import request from './request'

export function addRoom(data) {
  return request.post('/room/update', data)
}

export function updateRoom(data) {
  return request.post('/room/update', data)
}

export function deleteRoom(roomId) {
  return request.delete(`/room/${roomId}`)
}

export function getRoom(roomId) {
  return request.get(`/room/${roomId}`)
}

export function listRooms() {
  return request.get('/room/list')
}
