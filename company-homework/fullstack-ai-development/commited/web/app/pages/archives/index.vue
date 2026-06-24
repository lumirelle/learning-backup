<script setup lang="ts">
import { useAuthStore } from '~/composables/store/useAuthStore'

useHead({ title: '档案库' })
const { $api } = useNuxtApp()
const auth = useAuthStore()

interface Category { id: string, name: string, path: string, children?: Category[] }
interface Item { id: string, title: string, employee_name?: string, category_id?: string, status: string, security_level?: string }
interface Borrow { id: string, item_title?: string, borrower_name?: string, status: string, reason?: string, due_date?: string }

// 列表用普通 ref + 显式 load（不走 useAsyncData：其 refresh 在本版本会命中缓存而不重新执行 handler）
const items = ref<Item[] | null>(null)
const borrows = ref<Borrow[] | null>(null)
async function refreshItems() {
  items.value = await $api<Item[]>('/v1/archives')
}
async function refreshBorrows() {
  borrows.value = await $api<Borrow[]>('/v1/borrows')
}
await Promise.all([refreshItems(), refreshBorrows()])

const { data: cats } = await useAsyncData('arch-cats', () => $api<Category[]>('/v1/archive-categories'))

// 分类树拍平（选择器 + id→名称 映射）
const flatCats = computed(() => {
  const out: { id: string, name: string, path: string }[] = []
  const walk = (nodes?: Category[]) => {
    for (const n of nodes || []) {
      out.push({ id: n.id, name: n.name, path: n.path })
      walk(n.children)
    }
  }
  walk(cats.value || [])
  return out
})
const catName = computed(() => Object.fromEntries(flatCats.value.map(c => [c.id, c.name])))

const itemStatus: Record<string, { label: string, tone: string }> = {
  in_stock: { label: '在库', tone: 'green' },
  borrowed: { label: '借出', tone: 'amber' },
}
const borrowStatus: Record<string, { label: string, tone: string }> = {
  pending: { label: '待审批', tone: 'blue' },
  borrowed: { label: '已借出', tone: 'amber' },
  returned: { label: '已归还', tone: 'green' },
  rejected: { label: '已驳回', tone: 'red' },
}
const securityMap: Record<string, { label: string, tone: string }> = {
  normal: { label: '普通', tone: 'gray' },
  confidential: { label: '机密', tone: 'amber' },
  secret: { label: '绝密', tone: 'red' },
}

// ---- 新建档案 ----
const newItem = reactive({ title: '', category_id: '', employee_id: '', security_level: 'normal' })
const addErr = ref('')
async function addItem() {
  addErr.value = ''
  if (!newItem.title) {
    addErr.value = '请填写档案标题'
    return
  }
  try {
    await $api('/v1/archives', { method: 'POST', body: { ...newItem } })
    newItem.title = ''
    newItem.employee_id = ''
    await refreshItems()
  }
  catch (e: any) {
    addErr.value = e?.data?.message || '创建失败'
  }
}

// ---- 借阅表单 ----
const borrowFor = ref<Item | null>(null)
const approverId = ref('')
const reason = ref('')
const dueDate = ref('')
const err = ref('')
async function submitBorrow() {
  err.value = ''
  if (!approverId.value) {
    err.value = '请选择审批人'
    return
  }
  try {
    await $api(`/v1/archives/${borrowFor.value!.id}/borrow`, {
      method: 'POST',
      body: { approver_id: approverId.value, reason: reason.value, due_date: dueDate.value },
    })
    borrowFor.value = null
    reason.value = ''
    dueDate.value = ''
    await Promise.all([refreshItems(), refreshBorrows()])
  }
  catch (e: any) {
    err.value = e?.data?.message || '提交失败'
  }
}

async function act(id: string, action: string) {
  await $api(`/v1/borrows/${id}`, { method: 'POST', body: { action } })
  await Promise.all([refreshItems(), refreshBorrows()])
}
</script>

<template>
  <div class="flex flex-col gap-5">
    <HPageHeader title="档案库" desc="人事档案条目管理（分类 / 密级）与借阅审批闭环。" />

    <HCard title="新建档案">
      <div class="flex flex-wrap gap-3 items-end">
        <label class="text-sm flex flex-1 flex-col gap-1.5 min-w-60">
          <span class="text-13px text-truegray-500 font-medium">标题 *</span>
          <input v-model="newItem.title" placeholder="如 张三-劳动合同" class="input-base" @keyup.enter="addItem">
        </label>
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">分类</span>
          <select v-model="newItem.category_id" class="input-base w-44">
            <option value="">
              （未分类）
            </option>
            <option v-for="c in flatCats" :key="c.id" :value="c.id">
              {{ c.name }}
            </option>
          </select>
        </label>
        <div class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">关联员工</span>
          <HPersonPicker v-model="newItem.employee_id" placeholder="（不关联）" class="w-52" />
        </div>
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">密级</span>
          <select v-model="newItem.security_level" class="input-base w-28">
            <option v-for="(m, k) in securityMap" :key="k" :value="k">
              {{ m.label }}
            </option>
          </select>
        </label>
        <button class="btn-primary" @click="addItem">
          <span class="i-carbon-add" />新建
        </button>
      </div>
      <p v-if="addErr" class="text-sm text-rose-500 mt-3 flex gap-1.5 items-center">
        <span class="i-carbon-warning-alt" />{{ addErr }}
      </p>
    </HCard>

    <HCard title="档案条目">
      <table class="text-sm w-full">
        <thead>
          <tr class="border-b border-black/6 dark:border-white/8">
            <th class="th-base">
              标题
            </th>
            <th class="th-base">
              分类
            </th>
            <th class="th-base">
              关联员工
            </th>
            <th class="th-base">
              密级
            </th>
            <th class="th-base">
              状态
            </th>
            <th class="th-base" />
          </tr>
        </thead>
        <tbody>
          <tr v-for="it in items" :key="it.id" class="row-base">
            <td class="font-medium td-base">
              {{ it.title }}
            </td>
            <td class="text-truegray-500 td-base">
              {{ catName[it.category_id || ''] || '—' }}
            </td>
            <td class="text-truegray-500 td-base">
              {{ it.employee_name || '—' }}
            </td>
            <td class="td-base">
              <HBadge
                v-if="it.security_level"
                :tone="securityMap[it.security_level]?.tone"
                :label="securityMap[it.security_level]?.label || it.security_level"
              />
              <span v-else class="text-truegray-300 dark:text-truegray-600">—</span>
            </td>
            <td class="td-base">
              <HBadge dot :tone="itemStatus[it.status]?.tone" :label="itemStatus[it.status]?.label || it.status" />
            </td>
            <td class="td-base text-right">
              <button v-if="it.status === 'in_stock'" class="link-action" @click="borrowFor = it; approverId = ''">
                借阅
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <HEmpty v-if="!items?.length" icon="i-carbon-box" label="暂无档案" hint="在上方表单创建第一份档案" />
    </HCard>

    <!-- 借阅申请表单 -->
    <HCard v-if="borrowFor" :title="`借阅：${borrowFor.title}`" desc="提交后进入审批，通过即出库">
      <div class="flex flex-wrap gap-3 items-end">
        <div class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">审批人 *</span>
          <HPersonPicker v-model="approverId" source="users" placeholder="请选择审批人" class="w-56" />
        </div>
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">归还期限</span>
          <input v-model="dueDate" type="date" class="input-base">
        </label>
        <label class="text-sm flex flex-1 flex-col gap-1.5 min-w-50">
          <span class="text-13px text-truegray-500 font-medium">事由</span>
          <input v-model="reason" placeholder="借阅事由" class="input-base">
        </label>
        <button class="btn-primary" @click="submitBorrow">
          提交借阅
        </button>
        <button class="btn-secondary" @click="borrowFor = null">
          取消
        </button>
      </div>
      <p v-if="err" class="text-sm text-rose-500 mt-3 flex gap-1.5 items-center">
        <span class="i-carbon-warning-alt" />{{ err }}
      </p>
    </HCard>

    <HCard title="借阅单">
      <table class="text-sm w-full">
        <thead>
          <tr class="border-b border-black/6 dark:border-white/8">
            <th class="th-base">
              档案
            </th>
            <th class="th-base">
              借阅人
            </th>
            <th class="th-base">
              事由
            </th>
            <th class="th-base">
              归还期限
            </th>
            <th class="th-base">
              状态
            </th>
            <th class="th-base" />
          </tr>
        </thead>
        <tbody>
          <tr v-for="b in borrows" :key="b.id" class="row-base">
            <td class="font-medium td-base">
              {{ b.item_title || '—' }}
            </td>
            <td class="text-truegray-500 td-base">
              {{ b.borrower_name || '—' }}
            </td>
            <td class="text-truegray-500 td-base">
              {{ b.reason || '—' }}
            </td>
            <td class="tnum text-truegray-500 td-base">
              {{ fmtDate(b.due_date) }}
            </td>
            <td class="td-base">
              <HBadge dot :tone="borrowStatus[b.status]?.tone" :label="borrowStatus[b.status]?.label || b.status" />
            </td>
            <td class="td-base text-right">
              <template v-if="b.status === 'pending' && auth.isAdmin">
                <button class="link-action mr-3" @click="act(b.id, 'approve')">
                  通过
                </button>
                <button class="text-sm text-rose-500 font-medium underline-offset-3 cursor-pointer hover:underline" @click="act(b.id, 'reject')">
                  驳回
                </button>
              </template>
              <button v-else-if="b.status === 'borrowed'" class="link-action" @click="act(b.id, 'return')">
                归还
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <HEmpty v-if="!borrows?.length" icon="i-carbon-catalog" label="暂无借阅单" />
    </HCard>
  </div>
</template>
