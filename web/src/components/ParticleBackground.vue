<template>
  <canvas ref="canvasRef" class="particle-canvas"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const canvasRef = ref(null)
let animationId = null

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  let particles = []
  let mouseX = 0
  let mouseY = 0

  const resize = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }

  const createParticles = () => {
    particles = []
    const count = Math.floor((canvas.width * canvas.height) / 15000)
    for (let i = 0; i < count; i++) {
      particles.push({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        vx: (Math.random() - 0.5) * 0.5,
        vy: (Math.random() - 0.5) * 0.5,
        size: Math.random() * 2 + 0.5,
        opacity: Math.random() * 0.5 + 0.2,
        color: Math.random() > 0.5 ? '#00d4ff' : '#7b61ff',
      })
    }
  }

  const drawParticles = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    // 绘制渐变背景
    const gradient = ctx.createLinearGradient(0, 0, canvas.width, canvas.height)
    gradient.addColorStop(0, '#1e2433')
    gradient.addColorStop(0.5, '#222940')
    gradient.addColorStop(1, '#1e2840')
    ctx.fillStyle = gradient
    ctx.fillRect(0, 0, canvas.width, canvas.height)

    // 绘制网格
    ctx.strokeStyle = 'rgba(0, 212, 255, 0.04)'
    ctx.lineWidth = 1
    const gridSize = 50
    for (let x = 0; x < canvas.width; x += gridSize) {
      ctx.beginPath()
      ctx.moveTo(x, 0)
      ctx.lineTo(x, canvas.height)
      ctx.stroke()
    }
    for (let y = 0; y < canvas.height; y += gridSize) {
      ctx.beginPath()
      ctx.moveTo(0, y)
      ctx.lineTo(canvas.width, y)
      ctx.stroke()
    }

    // 绘制粒子
    particles.forEach((p, i) => {
      p.x += p.vx
      p.y += p.vy

      if (p.x < 0 || p.x > canvas.width) p.vx *= -1
      if (p.y < 0 || p.y > canvas.height) p.vy *= -1

      ctx.beginPath()
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
      ctx.fillStyle = p.color
      ctx.globalAlpha = p.opacity
      ctx.fill()
      ctx.globalAlpha = 1

      // 粒子连线
      for (let j = i + 1; j < particles.length; j++) {
        const p2 = particles[j]
        const dx = p.x - p2.x
        const dy = p.y - p2.y
        const dist = Math.sqrt(dx * dx + dy * dy)

        if (dist < 120) {
          ctx.beginPath()
          ctx.moveTo(p.x, p.y)
          ctx.lineTo(p2.x, p2.y)
          ctx.strokeStyle = `rgba(0, 212, 255, ${0.15 * (1 - dist / 120)})`
          ctx.lineWidth = 0.5
          ctx.stroke()
        }
      }

      // 鼠标连线
      const dxMouse = p.x - mouseX
      const dyMouse = p.y - mouseY
      const distMouse = Math.sqrt(dxMouse * dxMouse + dyMouse * dyMouse)
      if (distMouse < 200) {
        ctx.beginPath()
        ctx.moveTo(p.x, p.y)
        ctx.lineTo(mouseX, mouseY)
        ctx.strokeStyle = `rgba(123, 97, 255, ${0.3 * (1 - distMouse / 200)})`
        ctx.lineWidth = 0.8
        ctx.stroke()
      }
    })

    // 绘制光晕
    const time = Date.now() / 1000
    const glowX = canvas.width / 2 + Math.sin(time * 0.5) * canvas.width * 0.3
    const glowY = canvas.height / 2 + Math.cos(time * 0.3) * canvas.height * 0.3
    const glow = ctx.createRadialGradient(glowX, glowY, 0, glowX, glowY, 300)
    glow.addColorStop(0, 'rgba(0, 212, 255, 0.08)')
    glow.addColorStop(0.5, 'rgba(123, 97, 255, 0.04)')
    glow.addColorStop(1, 'transparent')
    ctx.fillStyle = glow
    ctx.fillRect(0, 0, canvas.width, canvas.height)

    animationId = requestAnimationFrame(drawParticles)
  }

  const handleMouseMove = (e) => {
    mouseX = e.clientX
    mouseY = e.clientY
  }

  window.addEventListener('resize', resize)
  window.addEventListener('mousemove', handleMouseMove)
  resize()
  createParticles()
  drawParticles()

  onUnmounted(() => {
    cancelAnimationFrame(animationId)
    window.removeEventListener('resize', resize)
    window.removeEventListener('mousemove', handleMouseMove)
  })
})
</script>

<style scoped>
.particle-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}
</style>
