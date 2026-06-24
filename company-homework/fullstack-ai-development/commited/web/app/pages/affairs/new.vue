<script setup lang="ts">
useHead({ title: '发起人事流程' })
const route = useRoute()
const { $api } = useNuxtApp()

const type = ref<'onboard' | 'transfer' | 'regularize' | 'offboard'>('transfer')
const employeeId = ref<string>((route.query.employee as string) || '')
const approverId = ref<string>('')
const submitting = ref(false)
const error = ref('')

const targetDeptId = ref('')
const jobLevel = ref('')
const reason = ref('')

// 入职建档信息（type === 'onboard' 时使用）
const onboard = reactive({
  name: '',
  employee_no: '',
  gender: 'male',
  phone: '',
  work_email: '',
  education: 'bachelor',
})

const { data: deptTree } = await useAsyncData('dept-options', () => $api<Hr.DeptNode[]>('/v1/departments/tree'))

const typeOptions = [
  { value: 'onboard', label: '入职', icon: 'i-carbon-user-follow', desc: '新员工建档入职' },
  { value: 'transfer', label: '岗位调动', icon: 'i-carbon-shuffle', desc: '调整部门 / 职级' },
  { value: 'regularize', label: '转正', icon: 'i-carbon-user-certification', desc: '试用期转正式' },
  { value: 'offboard', label: '离职', icon: 'i-carbon-logout', desc: '办理离职手续' },
] as const

const deptList = computed(() => {
  const out: { id: string, name: string, path: string }[] = []
  const walk = (nodes?: Hr.DeptNode[]) => {
    for (const n of nodes || []) {
      out.push({ id: n.id, name: n.name, path: n.path })
      walk(n.children as Hr.DeptNode[] | undefined)
    }
  }
  walk(deptTree.value || [])
  return out
})
const selectedDept = computed(() => deptList.value.find(d => d.id === targetDeptId.value))

async function submit() {
  error.value = ''
  if (!approverId.value) {
    error.value = '请选择审批人'
    return
  }
  if (type.value === 'onboard') {
    if (!onboard.name || !selectedDept.value) {
      error.value = '入职流程需填写姓名并选择入职部门'
      return
    }
  }
  else if (!employeeId.value) {
    error.value = '请选择员工'
    return
  }

  const payload: Record<string, any> = { reason: reason.value }
  if (type.value === 'onboard') {
    Object.assign(payload, {
      name: onboard.name,
      employee_no: onboard.employee_no,
      gender: onboard.gender,
      phone: onboard.phone,
      work_email: onboard.work_email,
      education: onboard.education,
      dept_id: selectedDept.value!.id,
      dept_name: selectedDept.value!.name,
      org_path: selectedDept.value!.path,
    })
    if (jobLevel.value)
      payload.job_level = jobLevel.value
  }
  if (type.value === 'transfer') {
    if (selectedDept.value) {
      payload.dept_id = selectedDept.value.id
      payload.dept_name = selectedDept.value.name
      payload.org_path = selectedDept.value.path
    }
    if (jobLevel.value)
      payload.job_level = jobLevel.value
  }

  submitting.value = true
  try {
    const p = await $api<Hr.Process>('/v1/processes', {
      method: 'POST',
      body: {
        type: type.value,
        employee_id: type.value === 'onboard' ? '' : employeeId.value,
        payload,
        approver_ids: [approverId.value],
      },
    })
    await navigateTo(`/affairs/${p.id}`)
  }
  catch (e: any) {
    error.value = e?.data?.message || e?.message || '提交失败'
  }
  finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="mx-auto flex flex-col gap-5 max-w-2xl w-full">
    <HPageHeader title="发起人事流程" desc="选择流程类型与员工，提交后进入审批。" back="/affairs" back-label="返回事务列表" />

    <HCard>
      <div class="flex flex-col gap-5">
        <!-- 流程类型卡片选择 -->
        <div class="flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">流程类型</span>
          <div class="gap-2.5 grid grid-cols-2 md:grid-cols-4">
            <button
              v-for="t in typeOptions" :key="t.value"
              class="p-3 text-left border rounded-xl cursor-pointer transition-colors"
              :class="type === t.value
                ? 'border-primary/50 bg-primary-50/60 dark:border-primary-400/40 dark:bg-primary-500/10'
                : 'border-black/8 bg-white hover:border-primary/30 dark:border-white/10 dark:bg-ink-800'"
              @click="type = t.value"
            >
              <span class="text-lg" :class="[t.icon, type === t.value ? 'text-primary' : 'text-truegray-400']" />
              <div class="text-sm font-medium mt-1.5" :class="type === t.value ? 'text-primary dark:text-primary-300' : ''">
                {{ t.label }}
              </div>
              <div class="text-xs text-truegray-400 mt-0.5">
                {{ t.desc }}
              </div>
            </button>
          </div>
        </div>

        <!-- 入职：新员工建档信息 -->
        <template v-if="type === 'onboard'">
          <div class="gap-4 grid grid-cols-1 md:grid-cols-2">
            <label class="text-sm flex flex-col gap-1.5">
              <span class="text-13px text-truegray-500 font-medium">姓名 *</span>
              <input v-model="onboard.name" placeholder="新员工姓名" class="input-base">
            </label>
            <label class="text-sm flex flex-col gap-1.5">
              <span class="text-13px text-truegray-500 font-medium">工号</span>
              <input v-model="onboard.employee_no" placeholder="留空自动生成" class="input-base">
            </label>
            <label class="text-sm flex flex-col gap-1.5">
              <span class="text-13px text-truegray-500 font-medium">性别</span>
              <select v-model="onboard.gender" class="input-base">
                <option value="male">
                  男
                </option>
                <option value="female">
                  女
                </option>
              </select>
            </label>
            <label class="text-sm flex flex-col gap-1.5">
              <span class="text-13px text-truegray-500 font-medium">学历</span>
              <select v-model="onboard.education" class="input-base">
                <option v-for="(label, k) in educationMap" :key="k" :value="k">
                  {{ label }}
                </option>
              </select>
            </label>
            <label class="text-sm flex flex-col gap-1.5">
              <span class="text-13px text-truegray-500 font-medium">手机</span>
              <input v-model="onboard.phone" placeholder="联系电话" class="input-base">
            </label>
            <label class="text-sm flex flex-col gap-1.5">
              <span class="text-13px text-truegray-500 font-medium">工作邮箱</span>
              <input v-model="onboard.work_email" placeholder="name@company.com" class="input-base">
            </label>
            <label class="text-sm flex flex-col gap-1.5">
              <span class="text-13px text-truegray-500 font-medium">入职部门 *</span>
              <select v-model="targetDeptId" class="input-base">
                <option value="">
                  请选择
                </option>
                <option v-for="d in deptList" :key="d.id" :value="d.id">
                  {{ d.name }}（{{ d.path }}）
                </option>
              </select>
            </label>
            <label class="text-sm flex flex-col gap-1.5">
              <span class="text-13px text-truegray-500 font-medium">职级</span>
              <input v-model="jobLevel" placeholder="如 P5 / M1" class="input-base">
            </label>
          </div>
        </template>

        <!-- 非入职：选择在册员工 -->
        <div v-else class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">员工</span>
          <HPersonPicker v-model="employeeId" status="active" placeholder="请选择员工" />
        </div>

        <template v-if="type === 'transfer'">
          <div class="gap-4 grid grid-cols-1 md:grid-cols-2">
            <label class="text-sm flex flex-col gap-1.5">
              <span class="text-13px text-truegray-500 font-medium">目标部门</span>
              <select v-model="targetDeptId" class="input-base">
                <option value="">
                  请选择
                </option>
                <option v-for="d in deptList" :key="d.id" :value="d.id">
                  {{ d.name }}（{{ d.path }}）
                </option>
              </select>
            </label>
            <label class="text-sm flex flex-col gap-1.5">
              <span class="text-13px text-truegray-500 font-medium">目标职级</span>
              <input v-model="jobLevel" placeholder="如 P7 / M2" class="input-base">
            </label>
          </div>
        </template>

        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">事由</span>
          <input v-model="reason" placeholder="变动事由" class="input-base">
        </label>

        <div class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">审批人</span>
          <HPersonPicker v-model="approverId" source="users" placeholder="请选择审批人" />
        </div>

        <p v-if="error" class="text-sm text-rose-500 flex gap-1.5 items-center">
          <span class="i-carbon-warning-alt" />{{ error }}
        </p>

        <div class="flex gap-2 justify-end">
          <NuxtLink to="/affairs" class="btn-secondary">
            取消
          </NuxtLink>
          <button class="btn-primary" :disabled="submitting" @click="submit">
            {{ submitting ? '提交中…' : '提交审批' }}
          </button>
        </div>
      </div>
    </HCard>
  </div>
</template>
