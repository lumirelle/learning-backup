<script setup lang="ts">
import { useAuthStore } from '~/composables/store/useAuthStore'

const route = useRoute()
const id = route.params.id as string
const { $api } = useNuxtApp()
const auth = useAuthStore()

const { data: p, refresh } = await useAsyncData(`process-${id}`, () => $api<Hr.Process>(`/v1/processes/${id}`))
useHead({ title: () => p.value ? `${processTypeMap[p.value.type]}流程` : '流程详情' })

const comment = ref('')
const acting = ref(false)
const error = ref('')

const currentStep = computed(() => p.value?.approval?.steps?.find(s => s.step_no === p.value?.approval?.current_step))
const canAct = computed(() =>
  p.value?.status === 'pending'
  && (auth.isAdmin || currentStep.value?.approver_id === auth.user?.id),
)

const payloadRows = computed(() => {
  const pl = p.value?.payload || {}
  const m: Record<string, string> = {
    name: '姓名',
    employee_no: '工号',
    gender: '性别',
    phone: '手机',
    work_email: '工作邮箱',
    education: '学历',
    dept_name: p.value?.type === 'onboard' ? '入职部门' : '目标部门',
    job_level: p.value?.type === 'onboard' ? '职级' : '目标职级',
    reason: '事由',
    last_work_day: '最后工作日',
  }
  const fmt = (k: string, v: unknown) => {
    if (k === 'gender')
      return genderMap[String(v)] || String(v)
    if (k === 'education')
      return educationMap[String(v)] || String(v)
    return String(v)
  }
  return Object.entries(pl)
    .filter(([k, v]) => m[k] && v !== '' && v != null)
    .map(([k, v]) => [m[k], fmt(k, v)])
})

async function act(kind: 'approve' | 'reject') {
  error.value = ''
  acting.value = true
  try {
    await $api(`/v1/processes/${id}/${kind}`, { method: 'POST', body: { comment: comment.value } })
    comment.value = ''
    await refresh()
  }
  catch (e: any) {
    error.value = e?.data?.message || e?.message || '操作失败'
  }
  finally {
    acting.value = false
  }
}
</script>

<template>
  <div v-if="p" class="mx-auto flex flex-col gap-5 max-w-3xl w-full">
    <HPageHeader
      :title="`${processTypeMap[p.type] || p.type}流程`"
      :desc="`单号 ${p.process_no}`"
      back="/affairs" back-label="返回事务列表"
    >
      <template #title-extra>
        <HBadge dot :tone="processStatusMap[p.status]?.tone" :label="processStatusMap[p.status]?.label || p.status" />
      </template>
    </HPageHeader>

    <HCard title="流程内容">
      <dl class="text-sm gap-x-8 grid grid-cols-1 md:grid-cols-2">
        <div v-for="[k, v] in payloadRows" :key="k" class="py-2.5 border-b border-black/4 flex justify-between dark:border-white/6">
          <dt class="text-truegray-400">
            {{ k }}
          </dt>
          <dd class="font-medium">
            {{ v }}
          </dd>
        </div>
        <div v-if="p.effective_date" class="py-2.5 border-b border-black/4 flex justify-between dark:border-white/6">
          <dt class="text-truegray-400">
            生效日期
          </dt>
          <dd class="tnum font-medium">
            {{ fmtDate(p.effective_date) }}
          </dd>
        </div>
      </dl>
    </HCard>

    <HCard title="审批轨迹">
      <ol class="flex flex-col">
        <li v-for="(s, i) in p.approval?.steps" :key="s.id" class="flex gap-3.5">
          <div class="flex shrink-0 flex-col w-5 items-center">
            <span
              class="mt-0.5 rounded-full flex shrink-0 h-5 w-5 items-center justify-center"
              :class="s.action === 'approved'
                ? 'bg-emerald-50 text-emerald-500 dark:bg-emerald-500/15'
                : s.action === 'rejected'
                  ? 'bg-rose-50 text-rose-500 dark:bg-rose-500/15'
                  : 'bg-truegray-100 text-truegray-400 dark:bg-white/8'"
            >
              <span
                class="text-xs"
                :class="s.action === 'approved' ? 'i-carbon-checkmark' : s.action === 'rejected' ? 'i-carbon-close' : 'i-carbon-time'"
              />
            </span>
            <span v-if="i < (p.approval?.steps?.length ?? 0) - 1" class="my-1 bg-black/8 flex-1 w-px dark:bg-white/10" />
          </div>
          <div class="text-sm pb-5 flex-1">
            <div class="flex items-center justify-between">
              <span class="font-medium">第 {{ s.step_no }} 步</span>
              <HBadge
                :tone="s.action === 'approved' ? 'green' : s.action === 'rejected' ? 'red' : 'gray'"
                :label="s.action === 'approved' ? '已通过' : s.action === 'rejected' ? '已驳回' : '待审批'"
              />
            </div>
            <div v-if="s.comment" class="text-xs text-truegray-500 mt-1">
              意见：{{ s.comment }}
            </div>
            <div v-if="s.acted_at" class="tnum text-xs text-truegray-400 mt-0.5">
              {{ fmtDateTime(s.acted_at) }}
            </div>
          </div>
        </li>
      </ol>
    </HCard>

    <HCard v-if="canAct" title="我的审批" desc="通过后流程立即生效，驳回需填写处理意见">
      <div class="flex flex-col gap-3">
        <input v-model="comment" placeholder="审批意见（可选）" class="input-base">
        <p v-if="error" class="text-sm text-rose-500 flex gap-1.5 items-center">
          <span class="i-carbon-warning-alt" />{{ error }}
        </p>
        <div class="flex gap-2 justify-end">
          <button class="btn-danger" :disabled="acting" @click="act('reject')">
            <span class="i-carbon-close" />驳回
          </button>
          <button class="btn-primary" :disabled="acting" @click="act('approve')">
            <span class="i-carbon-checkmark" />通过
          </button>
        </div>
      </div>
    </HCard>
    <p v-else-if="p.status === 'pending'" class="text-sm text-truegray-400 flex gap-1.5 items-center">
      <span class="i-carbon-time" />等待审批人处理。
    </p>
  </div>
</template>
