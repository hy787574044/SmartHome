import request from './request'

// 获取用户资料
export function getProfile() {
  return request.get('/user/profile')
}

// 更新用户资料
export function updateProfile(data) {
  return request.put('/user/profile', data)
}

// 修改密码
export function updatePassword(data) {
  return request.put('/user/password', data)
}

// 更新头像
export function updateAvatar(avatar) {
  return request.put('/user/avatar', { avatar })
}
