<script setup>
import { FONTS, MARGINS, THEMES } from '../../composables/useReaderSettings'

defineProps({
  settings: { type: Object, required: true },
})

const choices = [
  { key: 'font', label: '字型', options: FONTS },
  { key: 'margin', label: '邊距', options: MARGINS },
]
</script>

<template>
  <section class="panel">
    <div class="row">
      <span>主題</span>
      <div class="group">
        <button
          v-for="theme in THEMES"
          :key="theme.value"
          :class="['swatch', { active: settings.theme === theme.value }]"
          :style="{ background: theme.bg, color: theme.text }"
          @click="settings.theme = theme.value"
        >
          {{ theme.label }}
        </button>
      </div>
    </div>

    <div v-for="choice in choices" :key="choice.key" class="row">
      <span>{{ choice.label }}</span>
      <div class="group">
        <button
          v-for="option in choice.options"
          :key="option.value"
          :class="['swatch', { active: settings[choice.key] === option.value }]"
          @click="settings[choice.key] = option.value"
        >
          {{ option.label }}
        </button>
      </div>
    </div>

    <label class="row">
      <span>字級 {{ settings.fontSize }}</span>
      <input v-model.number="settings.fontSize" type="range" min="14" max="32" step="1" />
    </label>
    <label class="row">
      <span>行距 {{ settings.lineHeight.toFixed(1) }}</span>
      <input v-model.number="settings.lineHeight" type="range" min="1.4" max="2.4" step="0.1" />
    </label>
    <label class="row">
      <span>寬螢幕雙欄</span>
      <input v-model="settings.spread" type="checkbox" />
    </label>
  </section>
</template>

<style scoped>
.panel {
  position: absolute;
  top: 52px;
  right: 12px;
  z-index: 20;
  width: min(340px, calc(100vw - 24px));
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px;
  color: #222;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgb(0 0 0 / 25%);
  font-size: 14px;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.row input[type='range'] {
  flex: 1;
  max-width: 200px;
}

.group {
  display: flex;
  gap: 6px;
}

.swatch {
  padding: 6px 10px;
  font: inherit;
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 6px;
  cursor: pointer;
}

.swatch.active {
  outline: 2px solid var(--accent);
}
</style>
