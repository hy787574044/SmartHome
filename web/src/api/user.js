import request from './request'

// 获取用户资料
export function getProfile() {
  return request.get('/user/profile')
}

// 更新用户资料
export function updateProfile(data) {
  return request.post('/user/profile/update', data)
}

// 修改密码
export function updatePassword(data) {
  return request.post('/user/password/update', data)
}

// 更新头像
export function updateAvatar(avatar) {
  return request.post('/user/avatar/update', { avatar })
}
