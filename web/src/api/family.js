import request from './request'

// 创建家庭
export function createFamily(data) {
  return request.post('/family', data)
}

// 加入家庭
export function joinFamily(data) {
  return request.post('/family/join', data)
}

// 获取家庭信息
export function getFamilyInfo() {
  return request.get('/family/info')
}

// 获取成员列表
export function getMembers() {
  return request.get('/family/members')
}

// 移除成员
export function removeMember(memberId) {
  return request.delete(`/family/members/${memberId}`)
}

// 修改成员角色
export function updateMemberRole(memberId, role) {
  return request.post(`/family/members/${memberId}/role`, { role })
}

// 刷新邀请码
export function refreshInviteCode() {
  return request.post('/family/invite-code')
}
