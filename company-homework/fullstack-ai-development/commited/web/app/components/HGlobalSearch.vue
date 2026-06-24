<script lang="ts" setup>
// 顶栏全局搜索：按 姓名/工号/手机号 检索员工，回车或点击直达档案页。
const { $api } = useNuxtApp()

const q = ref('')
const open = ref(false)
const loading = ref(false)
const results = ref<Hr.Employee[]>([])
let timer: ReturnType<typeof setTimeout> | undefined

watch(q, (val) => {
  clearTimeout(timer)
  if (!val.trim()) {
    results.value = []
    open.value = false
    return
  }
  timer = setTimeout(async () => {
    loading.value = true
    try {
      const page = await $api<Hr.Page<Hr.Employee>>('/v1/employees', {
        query: { keyword: val.trim(), page: 1, page_size: 8 },
      })
      results.value = page.list || []
      open.value = true
    }
    catch {
      results.value = []
    }
    finally {
      loading.value = false
    }
  }, 250)
})

async function go(emp: Hr.Employee) {
  open.value = false
  q.value = ''
  await navigateTo(`/roster/${emp.id}`)
}

function onEnter() {
  if (results.value.length)
    go(results.value[0]!)
}

function onBlur() {
  // 延迟关闭，留出点击下拉项的时间
  setTimeout(() => (open.value = false), 150)
}
</script>

<template>
  <div class="relative">
    <span class="i-carbon-search text-sm text-truegray-300 left-2.5 top-1/2 absolute -translate-y-1/2" />
    <input
      v-model="q"
      placeholder="搜索员工…"
      aria-label="全局搜索员工"
      class="input-base text-13px py-1.5 pl-8 w-52"
      @keyup.enter="onEnter"
      @keyup.esc="open = false"
      @focus="q && results.length && (open = true)"
      @blur="onBlur"
    >
    <div v-if="open" class="mt-1.5 p-1.5 app-card max-h-80 w-72 shadow-lg right-0 top-full absolute z-50 overflow-auto">
      <button
        v-for="e in results" :key="e.id"
        class="text-sm px-2.5 py-2 text-left rounded-lg flex gap-2.5 w-full cursor-pointer items-center hover:bg-truegray-50 dark:hover:bg-white/5"
        @click="go(e)"
      >
        <span class="font-medium shrink-0">{{ e.name }}</span>
        <span class="text-xs text-truegray-400 truncate">{{ e.dept_name }} · {{ e.job_level || '—' }}</span>
        <span class="text-11px text-truegray-300 font-mono ml-auto shrink-0 dark:text-truegray-500">{{ e.employee_no }}</span>
      </button>
      <div v-if="!results.length" class="text-13px text-truegray-400 px-2.5 py-3 text-center">
        {{ loading ? '搜索中…' : '无匹配员工' }}
      </div>
    </div>
  </div>
</template>
