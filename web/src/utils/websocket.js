import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs.min.js'

let stompClient = null
const listeners = new Map()

/**
 * 连接 WebSocket
 */
export function connectWebSocket() {
  stompClient = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    onConnect: () => {
      console.log('WebSocket 已连接')
      // 订阅设备状态变更
      stompClient.subscribe('/topic/device/status', (message) => {
        const data = JSON.parse(message.body)
        notifyListeners('deviceStatus', data)
      })
      // 订阅告警
      stompClient.subscribe('/topic/alert', (message) => {
        const data = JSON.parse(message.body)
        notifyListeners('alert', data)
      })
    },
    onStompError: (frame) => {
      console.error('WebSocket 错误:', frame.headers['message'])
    },
  })
  stompClient.activate()
}

/**
 * 断开 WebSocket
 */
export function disconnectWebSocket() {
  if (stompClient) {
    stompClient.deactivate()
  }
}

/**
 * 注册监听器
 */
export function onWebSocketMessage(type, callback) {
  if (!listeners.has(type)) {
    listeners.set(type, [])
  }
  listeners.get(type).push(callback)
}

/**
 * 移除监听器
 */
export function offWebSocketMessage(type, callback) {
  if (listeners.has(type)) {
    const cbs = listeners.get(type)
    const index = cbs.indexOf(callback)
    if (index > -1) {
      cbs.splice(index, 1)
    }
  }
}

function notifyListeners(type, data) {
  if (listeners.has(type)) {
    listeners.get(type).forEach((cb) => cb(data))
  }
}
