import { onBeforeUnmount, ref } from 'vue'

// 重寄倒數
export function useCountdown(seconds = 60) {
  const left = ref(0)
  let timer = 0

  function start() {
    clearInterval(timer)
    left.value = seconds
    timer = setInterval(() => {
      left.value -= 1
      if (left.value <= 0) clearInterval(timer)
    }, 1000)
  }

  onBeforeUnmount(() => clearInterval(timer))

  return { left, start }
}
