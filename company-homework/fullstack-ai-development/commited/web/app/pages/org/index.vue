<script setup lang="ts">
import { useAuthStore } from '~/composables/store/useAuthStore'

useHead({ title: '组织架构' })
const { $api } = useNuxtApp()
const auth = useAuthStore()

interface SyncLog {
  id: string
  trigger: string
  operator: string
  status: string
  orgs: number
  depts: number
  employees: number
  users: number
  deactivated: number
  message: string
  duration_ms: number
  created_at: string
}

// 统一组织+部门树（普通 ref + 显式 load，避免 useAsyncData refresh 命中缓存不重拉）
const tree = ref<Hr.OrgNode[]>([])
const levels = ref<{ id: string, level_code: string, name: string }[]>([])
const ldapEnabled = ref(false)
const logs = ref<SyncLog[]>([])
const loading = ref(true)

async function loadTree() {
  tree.value = await $api<Hr.OrgNode[]>('/v1/org-structure')
}
async function loadLogs() {
  if (!auth.isAdmin)
    return
  try {
    logs.value = await $api<SyncLog[]>('/v1/ldap/sync-logs')
  }
  catch { /* 非管理员或未启用 */ }
}

const lastSync = computed(() => logs.value.find(l => l.status === 'success'))

onMounted(async () => {
  try {
    // 整页加载时 user 可能尚未回填 → 先确保拿到当前用户，再据 isAdmin 取 LDAP 配置/历史
    if (!auth.user)
      await auth.fetchMe()

    const [, lv, cfg] = await Promise.all([
      loadTree(),
      $api<{ id: string, level_code: string, name: string }[]>('/v1/job-levels').catch(() => []),
      auth.isAdmin
        ? $api<{ enabled: boolean }>('/v1/ldap/config').catch(() => ({ enabled: false }))
        : Promise.resolve({ enabled: false }),
    ])
    levels.value = lv as typeof levels.value
    ldapEnabled.value = (cfg as { enabled: boolean }).enabled
    await loadLogs()
  }
  finally {
    loading.value = false
  }
})

// 手动同步
const syncing = ref(false)
const syncMsg = ref('')
async function syncNow() {
  syncMsg.value = ''
  syncing.value = true
  try {
    const rep = await $api<{ depts: { created: number, updated: number }, employees: { created: number, updated: number } }>(
      '/v1/ldap/sync',
      { method: 'POST' },
    )
    syncMsg.value = `✓ 同步完成：部门 +${rep.depts.created}/~${rep.depts.updated}，员工 +${rep.employees.created}/~${rep.employees.updated}`
    await Promise.all([loadTree(), loadLogs()])
  }
  catch (e: any) {
    syncMsg.value = `✗ ${e?.data?.message || e?.message || '同步失败'}`
  }
  finally {
    syncing.value = false
  }
}

function fmtTime(s?: string) {
  if (!s)
    return '—'
  return new Date(s).toLocaleString('zh-CN', { hour12: false })
}
const triggerLabel: Record<string, string> = { manual: '手动', scheduled: '定时', cli: '命令行' }
</script>

<template>
  <div class="flex flex-col gap-5">
    <HPageHeader title="组织架构" desc="组织与部门统一树，数据由企业 SSO（LDAP）同步维护。" />

    <!-- 同步控制台（仅管理员且 LDAP 启用） -->
    <HCard v-if="auth.isAdmin && ldapEnabled" title="LDAP 同步" desc="组织架构以 LDAP 为唯一事实源，此处手动触发同步并查看历史">
      <div class="flex flex-wrap gap-x-8 gap-y-3 items-center">
        <div class="flex flex-col gap-0.5">
          <span class="text-xs text-truegray-400">上次成功同步</span>
          <span class="tnum text-sm text-truegray-700 font-medium dark:text-truegray-200">{{ fmtTime(lastSync?.created_at) }}</span>
        </div>
        <div v-if="lastSync" class="flex flex-col gap-0.5">
          <span class="text-xs text-truegray-400">规模</span>
          <span class="tnum text-sm text-truegray-700 dark:text-truegray-200">部门 {{ lastSync.depts }} · 员工 {{ lastSync.employees }} · 账号 {{ lastSync.users }}</span>
        </div>
        <button class="btn-primary ml-auto" :disabled="syncing" @click="syncNow">
          <span :class="syncing ? 'i-carbon-circle-dash animate-spin' : 'i-carbon-renew'" />
          {{ syncing ? '同步中…' : '立即同步' }}
        </button>
      </div>
      <p v-if="syncMsg" class="text-sm mt-3" :class="syncMsg.startsWith('✓') ? 'text-emerald-600' : 'text-rose-500'">
        {{ syncMsg }}
      </p>

      <!-- 同步历史 -->
      <div v-if="logs.length" class="mt-4 pt-4 border-t border-black/6 dark:border-white/8">
        <div class="text-xs text-truegray-400 mb-2">
          同步历史
        </div>
        <div class="overflow-x-auto">
          <table class="text-sm w-full">
            <thead>
              <tr class="text-xs text-truegray-400 text-left">
                <th class="font-medium pb-2 pr-4">
                  时间
                </th>
                <th class="font-medium pb-2 pr-4">
                  触发
                </th>
                <th class="font-medium pb-2 pr-4">
                  操作人
                </th>
                <th class="font-medium pb-2 pr-4">
                  结果
                </th>
                <th class="font-medium pb-2 pr-4">
                  规模
                </th>
                <th class="font-medium pb-2">
                  耗时
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="l in logs" :key="l.id" class="border-t border-black/4 dark:border-white/6">
                <td class="tnum text-truegray-600 py-1.5 pr-4 dark:text-truegray-300">
                  {{ fmtTime(l.created_at) }}
                </td>
                <td class="py-1.5 pr-4">
                  {{ triggerLabel[l.trigger] || l.trigger }}
                </td>
                <td class="text-truegray-600 py-1.5 pr-4 dark:text-truegray-300">
                  {{ l.operator || '系统' }}
                </td>
                <td class="py-1.5 pr-4">
                  <HBadge :tone="l.status === 'success' ? 'green' : 'red'" :label="l.status === 'success' ? '成功' : '失败'" />
                </td>
                <td class="tnum text-truegray-500 py-1.5 pr-4">
                  <span v-if="l.status === 'success'">部门 {{ l.depts }} · 员工 {{ l.employees }}<span v-if="l.deactivated"> · 收敛 {{ l.deactivated }}</span></span>
                  <span v-else class="text-rose-500" :title="l.message">{{ l.message }}</span>
                </td>
                <td class="tnum text-truegray-400 py-1.5">
                  {{ l.duration_ms }}ms
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </HCard>

    <!-- 统一可折叠树 -->
    <HCard title="组织与部门" desc="点击节点前的箭头展开 / 收起；结构由 LDAP 同步维护，不可手动调整">
      <HEmpty v-if="!loading && !tree.length" icon="i-carbon-tree-view-alt" label="暂无组织数据，请先同步 LDAP" />
      <HTree v-else :nodes="tree" :default-expand-depth="1" />
    </HCard>

    <HCard title="职级体系" desc="全公司统一职级序列">
      <div class="flex flex-wrap gap-2">
        <span
          v-for="l in levels" :key="l.id"
          class="text-xs text-truegray-600 font-medium px-2.5 py-1 border border-black/6 rounded-lg bg-truegray-50 dark:text-truegray-300 dark:border-white/8 dark:bg-white/4"
          :title="l.name"
        >
          {{ l.level_code }}
        </span>
      </div>
    </HCard>
  </div>
</template>
