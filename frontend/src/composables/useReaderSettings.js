import { computed, reactive, watch } from 'vue'

const STORAGE_KEY = 'reader-settings'

export const THEMES = [
  { value: 'light', label: '白色', bg: '#fdfdfb', text: '#222222' },
  { value: 'sepia', label: '護眼', bg: '#f4ecd8', text: '#4b3a26' },
  { value: 'green', label: '豆綠', bg: '#d7e8d2', text: '#263326' },
  { value: 'dark', label: '深色', bg: '#16171a', text: '#c4c4c4' },
]

export const FONTS = [
  { value: 'serif', label: '明體', family: "'Noto Serif TC', 'Noto Serif CJK TC', 'PMingLiU', serif" },
  { value: 'sans', label: '黑體', family: "'Noto Sans TC', 'Noto Sans CJK TC', 'Microsoft JhengHei', sans-serif" },
  { value: 'kai', label: '楷體', family: "'Kaiti TC', 'BiauKai', 'DFKai-SB', 'KaiTi', serif" },
]

export const MARGINS = [
  { value: 'narrow', label: '窄', size: '3%' },
  { value: 'medium', label: '中', size: '8%' },
  { value: 'wide', label: '寬', size: '15%' },
]

const DEFAULTS = {
  theme: 'sepia',
  font: 'serif',
  margin: 'medium',
  fontSize: 19,
  lineHeight: 1.8,
  spread: true,
}

function restore() {
  try {
    return { ...DEFAULTS, ...JSON.parse(localStorage.getItem(STORAGE_KEY)) }
  } catch {
    return { ...DEFAULTS }
  }
}

const pick = (options, value) => options.find((option) => option.value === value) ?? options[0]

export function useReaderSettings() {
  const settings = reactive(restore())

  watch(settings, () => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(settings))
    } catch {
      // 無法儲存略過
    }
  })

  const styleVars = computed(() => {
    const theme = pick(THEMES, settings.theme)
    return {
      '--reader-bg': theme.bg,
      '--reader-text': theme.text,
      '--reader-font': pick(FONTS, settings.font).family,
      '--reader-margin': pick(MARGINS, settings.margin).size,
      '--reader-font-size': `${settings.fontSize}px`,
      '--reader-line-height': settings.lineHeight,
    }
  })

  return { settings, styleVars }
}
