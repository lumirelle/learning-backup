<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import type { AdminUserListItemVO, BatchCreateItem, BatchCreateResult, CreateUserReq, UpdateUserReq } from '@/api/admin'
import type { SubDepartmentVO } from '@/api/departments'
import { departmentsApi } from '@/api/departments'
import {
  NButton,
  NForm,
  NFormItem,
  NInput,
  NInputGroup,
  NDrawer,
  NDrawerContent,
  NPopconfirm,
  NSelect,
  NSwitch,
  NSpin,
  NSpace,
  NTabPane,
  NTabs,
  NTooltip,
  useMessage,
} from 'naive-ui'
import { computed, h, onMounted, reactive, ref } from 'vue'
import { adminApi } from '@/api/admin'
import { BizError } from '@/api/request'
import CopyButton from '@/components/CopyButton.vue'

const msg = useMessage()

// ── 公共：随机强密码生成 ─────────────────────────────────
function genPassword(len = 12): string {
  const lowers = 'abcdefghijklmnopqrstuvwxyz'
  const uppers = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  const digits = '23456789'
  const symbols = '!@#$%^&*'
  const all = lowers + uppers + digits + symbols
  const a = (s: string) => s[Math.floor(Math.random() * s.length)]
  const required = [a(lowers), a(uppers), a(digits), a(symbols)]
  const rest = Array.from({ length: len - required.length }, () => a(all))
  return [...required, ...rest].sort(() => Math.random() - 0.5).join('')
}

// ════════════════════════════════════════════════════════════════
//                          单个创建
// ════════════════════════════════════════════════════════════════
const singleForm = reactive<CreateUserReq>({
  email: '',
  password: '',
  fullName: '',
  role: 'HR',
  subDepartmentIds: [],
})

const subDepartments = ref<SubDepartmentVO[]>([])

const userList = ref<AdminUserListItemVO[]>([])
const listLoading = ref(false)

async function fetchUserList() {
  listLoading.value = true
  try {
    userList.value = await adminApi.listUsers()
  }
  catch (e) {
    if (e instanceof BizError)
      msg.error(e.message)
  }
  finally {
    listLoading.value = false
  }
}

onMounted(async () => {
  try {
    subDepartments.value = await departmentsApi.listAllSubDepartments()
  }
  catch (e) {
    console.warn('load sub-departments failed', e)
  }
  fetchUserList()
})

const subDepartmentOptionsGrouped = computed(() => {
  const groups = new Map<number, { name: string, list: SubDepartmentVO[] }>()
  subDepartments.value.forEach((sd) => {
    if (!groups.has(sd.parentDepartmentId))
      groups.set(sd.parentDepartmentId, { name: sd.parentDepartmentName, list: [] })
    groups.get(sd.parentDepartmentId)!.list.push(sd)
  })
  return Array.from(groups.entries()).map(([deptId, { name, list }]) => ({
    type: 'group' as const,
    label: name,
    key: `dept-${deptId}`,
    children: list.map(sd => ({
      label: `${sd.name} / ${sd.location}`,
      value: sd.id,
    })),
  }))
})

const singleFormRef = ref<FormInst | null>(null)
const singleSubmitting = ref(false)
const singlePasswordVisible = ref(false)
const recentlyCreated = ref<Array<{ email: string, role: string, fullName: string, createdAt: number }>>([])

const singleRules: FormRules = {
  email: [
    { required: true, message: '邮箱必填', trigger: ['blur', 'input'] },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur'] },
  ],
  password: [
    { required: true, message: '密码必填', trigger: ['blur', 'input'] },
    { min: 8, max: 72, message: '密码长度 8 - 72 位', trigger: ['blur'] },
  ],
  fullName: [
    { required: true, message: '姓名必填', trigger: ['blur', 'input'] },
    { max: 100, message: '姓名最多 100 字符', trigger: ['blur'] },
  ],
  role: [{ required: true, message: '角色必选', trigger: ['blur', 'change'] }],
  subDepartmentIds: [{
    validator: (_r, value: number[] | undefined) => {
      if (singleForm.role !== 'HR')
        return true
      if (value && value.length > 0)
        return true
      return new Error('HR 账号必须绑定至少一个子部门')
    },
    trigger: ['blur', 'change'],
  }],
}

const roleOptions = [
  { label: 'HR · 招聘专员', value: 'HR' },
  { label: 'CANDIDATE · 候选人', value: 'CANDIDATE' },
]

function genSinglePassword() {
  singleForm.password = genPassword(12)
  singlePasswordVisible.value = true
  msg.success('已生成强密码，提交后请提示对方首次登录后修改')
}

async function submitSingle() {
  if (!singleFormRef.value)
    return
  try {
    await singleFormRef.value.validate()
  }
  catch {
    return
  }
  singleSubmitting.value = true
  try {
    const me = await adminApi.createUser({ ...singleForm })
    msg.success(`已创建 ${me.role} 账号 · ${me.email}`)
    fetchUserList()
    recentlyCreated.value.unshift({
      email: me.email,
      role: me.role,
      fullName: me.fullName,
      createdAt: Date.now(),
    })
    singleForm.email = ''
    singleForm.password = ''
    singleForm.fullName = ''
  }
  catch (e) {
    if (e instanceof BizError)
      msg.error(e.message)
    else msg.error('创建失败，请重试')
  }
  finally {
    singleSubmitting.value = false
  }
}

// ════════════════════════════════════════════════════════════════
//                          批量导入
// ════════════════════════════════════════════════════════════════
const batchInput = ref('')
const batchPlaceholder
  = `# 每行 1 条，逗号分隔：email,password,fullName,role,subDepartmentIds(可选)\n# HR 第 5 列为子部门 id，多个用分号分隔，如 1;2;14\n# 例：\nhr.alice@company.com,InitPass2026!,Alice Wang,HR,1;2\n`

interface ParsedRow {
  rowIndex: number
  raw: string
  data?: CreateUserReq
  error?: string
}

const parsedRows = ref<ParsedRow[]>([])
const batchSubmitting = ref(false)
const batchResult = ref<BatchCreateResult | null>(null)

const validParsed = computed(() => parsedRows.value.filter(r => r.data && !r.error))
const invalidParsed = computed(() => parsedRows.value.filter(r => r.error))

function parseBatchInput() {
  batchResult.value = null
  const lines = batchInput.value.split(/\r?\n/)
  const rows: ParsedRow[] = []
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim()
    if (line.length === 0 || line.startsWith('#'))
      continue
    const parts = line.split(',').map(s => s.trim())
    if (parts.length < 4) {
      rows.push({ rowIndex: i, raw: line, error: '格式错误：至少 4 列（email,password,fullName,role[,subDepartmentIds]）' })
      continue
    }
    const [email, password, fullName, roleRaw, subDeptRaw] = parts
    const role = roleRaw.toUpperCase()
    if (role !== 'HR' && role !== 'CANDIDATE') {
      rows.push({ rowIndex: i, raw: line, error: `role 只能是 HR 或 CANDIDATE，当前：${roleRaw}` })
      continue
    }
    let subDepartmentIds: number[] | undefined
    if (subDeptRaw) {
      subDepartmentIds = subDeptRaw.split(';').map(s => Number(s.trim())).filter(n => !Number.isNaN(n))
      if (subDepartmentIds.length === 0) {
        rows.push({ rowIndex: i, raw: line, error: 'subDepartmentIds 格式错误，示例：1;2;14' })
        continue
      }
    }
    if (role === 'HR' && (!subDepartmentIds || subDepartmentIds.length === 0)) {
      rows.push({ rowIndex: i, raw: line, error: 'HR 行必须提供第 5 列 subDepartmentIds' })
      continue
    }
    if (!email || !email.includes('@')) {
      rows.push({ rowIndex: i, raw: line, error: '邮箱格式不正确' })
      continue
    }
    if (!password || password.length < 8 || password.length > 72) {
      rows.push({ rowIndex: i, raw: line, error: '密码长度 8 - 72 位' })
      continue
    }
    if (!fullName || fullName.length > 100) {
      rows.push({ rowIndex: i, raw: line, error: '姓名必填且最多 100 字符' })
      continue
    }
    rows.push({
      rowIndex: i,
      raw: line,
      data: {
        email,
        password,
        fullName,
        role: role as 'HR' | 'CANDIDATE',
        subDepartmentIds: role === 'HR' ? subDepartmentIds : undefined,
      },
    })
  }
  parsedRows.value = rows
  if (rows.length === 0)
    msg.warning('未解析到任何有效数据')
  else msg.info(`已解析 ${rows.length} 行 · ${validParsed.value.length} 条可提交 / ${invalidParsed.value.length} 条本地校验失败`)
}

function fillBatchTemplate() {
  batchInput.value
    = `# CSV · 每行：email,password,fullName,role,subDepartmentIds(HR必填)\nhr.alice@example.com,${genPassword()},Alice Wang,HR,1;2\nhr.bob@example.com,${genPassword()},Bob Liu,HR,3\n`
}

async function submitBatch() {
  if (validParsed.value.length === 0) {
    msg.warning('没有可提交的有效行，请先解析或修正本地校验错误')
    return
  }
  if (validParsed.value.length > 100) {
    msg.error('单批最多 100 条，请分批提交')
    return
  }
  batchSubmitting.value = true
  try {
    const users = validParsed.value.map(r => r.data!)
    batchResult.value = await adminApi.batchCreate(users)
    msg.success(`批量创建完成 · 成功 ${batchResult.value.successCount} / 失败 ${batchResult.value.failureCount}`)
  }
  catch (e) {
    if (e instanceof BizError)
      msg.error(e.message)
    else msg.error('批量创建请求失败')
  }
  finally {
    batchSubmitting.value = false
  }
}

function copyFailedRows() {
  if (!batchResult.value)
    return
  const failed = batchResult.value.items.filter((it: BatchCreateItem) => !it.success)
  if (failed.length === 0) {
    msg.info('没有失败行可以复制')
    return
  }
  const lines = failed.map((it) => {
    const src = parsedRows.value.find(r => r.data?.email.toLowerCase() === it.email)
    return `${src?.raw ?? it.email}    # ${it.errorMsg ?? '失败'}`
  })
  navigator.clipboard.writeText(lines.join('\n'))
  msg.success(`已复制 ${failed.length} 行失败数据到剪贴板，可粘贴到输入框修改后重新提交`)
}

function resetBatch() {
  batchInput.value = ''
  parsedRows.value = []
  batchResult.value = null
}

// ── 时间显示工具 ─────────────────────────────────
function fmtTime(ts: number) {
  const d = new Date(ts)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}:${d.getSeconds().toString().padStart(2, '0')}`
}

// ── 用户列表 · 编辑抽屉 ─────────────────────────────────────
const editVisible = ref(false)
const editSaving = ref(false)
const editingId = ref<number | null>(null)
const editForm = reactive<UpdateUserReq & { email: string }>({
  email: '',
  fullName: '',
  role: 'HR',
  active: true,
  subDepartmentIds: [],
  newPassword: '',
})

function openEdit(u: AdminUserListItemVO) {
  if (u.role === 'ADMIN') {
    msg.warning('管理员账号请通过数据库或运维流程维护')
    return
  }
  editingId.value = u.id
  editForm.email = u.email
  editForm.fullName = u.fullName
  editForm.role = u.role === 'CANDIDATE' ? 'CANDIDATE' : 'HR'
  editForm.active = u.active
  editForm.subDepartmentIds = [...(u.subDepartmentIds ?? [])]
  editForm.newPassword = ''
  editVisible.value = true
}

async function saveEdit() {
  if (editingId.value == null)
    return
  if (editForm.role === 'HR' && (!editForm.subDepartmentIds || editForm.subDepartmentIds.length === 0)) {
    msg.error('HR 至少绑定一个子部门')
    return
  }
  editSaving.value = true
  try {
    const payload: UpdateUserReq = {
      fullName: editForm.fullName,
      role: editForm.role,
      active: editForm.active,
      subDepartmentIds: editForm.role === 'HR' ? editForm.subDepartmentIds : [],
    }
    if (editForm.newPassword?.trim())
      payload.newPassword = editForm.newPassword.trim()
    await adminApi.updateUser(editingId.value, payload)
    msg.success('已保存')
    editVisible.value = false
    await fetchUserList()
  }
  catch (e) {
    if (e instanceof BizError)
      msg.error(e.message)
  }
  finally {
    editSaving.value = false
  }
}

// 渲染密码 input 右侧"生成"按钮（避免单独写 <template>）
function renderGenButton(onClick: () => void) {
  return h(
    NButton,
    { type: 'tertiary', onClick },
    { default: () => '生成' },
  )
}
</script>

<template>
  <div max-w-1200px mx-auto p="x-6 b-16" class="pt-[calc(60px+40px)]">
    <!-- ─────────── Hero ─────────── -->
    <header mb-8>
      <p kicker mb-2>
        Admin · 账号管理
      </p>
      <h1 m-0 text-36px text-gray-900 font="display black" tracking="[-0.03em]" leading="[1.05]">
        新建 / 批量导入 <span text-gradient>HR 账号</span>
      </h1>
      <p mt-2 text-secondary>
        ADMIN 专属 · 用于运营侧批量开通 HR / 候选人账户。<strong text-warning-700>初始密码请通过安全渠道告知对方，并提示首次登录后修改。</strong>
      </p>
    </header>

    <!-- ─────────── Tabs ─────────── -->
    <NTabs type="line" size="large" animated>
      <NTabPane name="list" tab="用户列表">
        <NSpin :show="listLoading">
          <section mt-4 p-4 rounded-xl bg-elevated border="~ subtle">
            <p mb-3 text-sm text-secondary>
              共 {{ userList.length }} 个账号（不含 ADMIN 修改入口在 PATCH）
            </p>
            <table w-full text-sm>
              <thead>
                <tr text-left text-tertiary border-b="~ subtle">
                  <th py-2>邮箱</th>
                  <th py-2>姓名</th>
                  <th py-2>角色</th>
                  <th py-2>状态</th>
                  <th py-2>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in userList" :key="u.id" border-b="~ subtle">
                  <td py-2>{{ u.email }}</td>
                  <td py-2>{{ u.fullName }}</td>
                  <td py-2>{{ u.role }}</td>
                  <td py-2>{{ u.active ? '启用' : '禁用' }}</td>
                  <td py-2>
                    <NButton
                      v-if="u.role !== 'ADMIN'"
                      size="tiny"
                      tertiary
                      @click="openEdit(u)"
                    >
                      编辑
                    </NButton>
                    <span v-else text-tertiary>—</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </section>
        </NSpin>

        <NDrawer v-model:show="editVisible" :width="420" placement="right">
          <NDrawerContent title="编辑账号" closable>
            <NForm label-placement="top">
              <NFormItem label="邮箱">
                <NInput :value="editForm.email" disabled />
              </NFormItem>
              <NFormItem label="姓名">
                <NInput v-model:value="editForm.fullName" />
              </NFormItem>
              <NFormItem label="角色">
                <NSelect
                  v-model:value="editForm.role"
                  :options="roleOptions"
                />
              </NFormItem>
              <NFormItem label="状态">
                <NSwitch v-model:value="editForm.active">
                  <template #checked>启用</template>
                  <template #unchecked>禁用</template>
                </NSwitch>
              </NFormItem>
              <NFormItem v-if="editForm.role === 'HR'" label="绑定子部门">
                <NSelect
                  v-model:value="editForm.subDepartmentIds"
                  :options="subDepartmentOptionsGrouped"
                  multiple
                  filterable
                  max-tag-count="responsive"
                />
              </NFormItem>
              <NFormItem label="重置密码（可选）">
                <NInputGroup>
                  <NInput
                    v-model:value="editForm.newPassword"
                    type="password"
                    placeholder="留空则不修改"
                    show-password-on="click"
                  />
                  <NButton tertiary @click="editForm.newPassword = genPassword()">
                    生成
                  </NButton>
                </NInputGroup>
              </NFormItem>
            </NForm>
            <template #footer>
              <NSpace>
                <NButton @click="editVisible = false">
                  取消
                </NButton>
                <NButton type="primary" :loading="editSaving" @click="saveEdit">
                  保存
                </NButton>
              </NSpace>
            </template>
          </NDrawerContent>
        </NDrawer>
      </NTabPane>
      <!-- ━━━━━━━━━━━━━━━ 单个创建 ━━━━━━━━━━━━━━━ -->
      <NTabPane name="single" tab="单个创建">
        <div
          mt-2
          grid="~ cols-1 lg:cols-2"
          gap-6
        >
          <!-- 表单 -->
          <section
            p-6
            rounded-xl
            bg-elevated
            border="~ subtle"
            shadow-sm
          >
            <h2 mt-0 mb-4 text-lg font-semibold>
              账号信息
            </h2>
            <NForm
              ref="singleFormRef"
              :model="singleForm"
              :rules="singleRules"
              label-placement="top"
              size="medium"
            >
              <NFormItem path="email" label="邮箱">
                <NInput v-model:value="singleForm.email" placeholder="hr.alice@company.com" />
              </NFormItem>

              <NFormItem path="password" label="初始密码">
                <NInputGroup>
                  <NInput
                    v-model:value="singleForm.password"
                    :type="singlePasswordVisible ? 'text' : 'password'"
                    placeholder="8 - 72 位"
                    show-password-on="click"
                  />
                  <NTooltip>
                    <template #trigger>
                      <component :is="renderGenButton(genSinglePassword)" />
                    </template>
                    一键生成 12 位强密码（含大小写 + 数字 + 符号）
                  </NTooltip>
                </NInputGroup>
              </NFormItem>

              <NFormItem path="fullName" label="姓名">
                <NInput v-model:value="singleForm.fullName" placeholder="Alice Wang" />
              </NFormItem>

              <NFormItem path="role" label="角色">
                <NSelect
                  v-model:value="singleForm.role"
                  :options="roleOptions"
                  placeholder="选择角色"
                />
              </NFormItem>

              <NFormItem
                v-if="singleForm.role === 'HR'"
                path="subDepartmentIds"
                label="绑定子部门"
              >
                <NSelect
                  v-model:value="singleForm.subDepartmentIds"
                  :options="subDepartmentOptionsGrouped"
                  multiple
                  filterable
                  placeholder="至少选择一个子部门"
                  max-tag-count="responsive"
                />
              </NFormItem>

              <NSpace mt-4>
                <NPopconfirm
                  :positive-text="`确认创建 ${singleForm.role}`"
                  negative-text="取消"
                  @positive-click="submitSingle"
                >
                  <template #trigger>
                    <NButton
                      type="primary"
                      :loading="singleSubmitting"
                      size="large"
                    >
                      创建账号
                    </NButton>
                  </template>
                  即将创建 <strong>{{ singleForm.role }}</strong> 账号 ·
                  <span font-mono>{{ singleForm.email || '（未填写邮箱）' }}</span> ·
                  请确认初始密码已通过安全渠道告知对方。
                </NPopconfirm>
                <NButton
                  size="large"
                  :disabled="singleSubmitting"
                  @click="() => { singleForm.email = ''; singleForm.password = ''; singleForm.fullName = ''; singleForm.subDepartmentIds = [] }"
                >
                  清空
                </NButton>
              </NSpace>
            </NForm>
          </section>

          <!-- 最近创建 -->
          <section
            p-6
            rounded-xl
            bg-elevated
            border="~ subtle"
            shadow-sm
          >
            <h2 mt-0 mb-4 text-lg font-semibold>
              本次会话已创建
            </h2>
            <div v-if="recentlyCreated.length === 0" py-12 text-center text-tertiary text-sm>
              尚未创建任何账号
            </div>
            <ul v-else m-0 p-0 list-none>
              <li
                v-for="item in recentlyCreated"
                :key="`${item.email}-${item.createdAt}`"
                py-3
                border="b subtle"
                flex="~ items-center justify-between gap-3"
              >
                <div min-w-0 flex="~ col gap-2px">
                  <span font-mono text-sm text-primary truncate>{{ item.email }}</span>
                  <span text-xs text-tertiary>{{ item.fullName }}</span>
                </div>
                <CopyButton
                  :text="item.email"
                  hint="已复制邮箱"
                  tooltip="复制邮箱"
                  size="tiny"
                />
                <span
                  shrink-0
                  px-2 py-2px
                  rounded-md
                  text-xs font-semibold tracking-wide
                  :class="item.role === 'HR' ? 'text-brand-700 bg-brand-50' : 'text-info-700 bg-info-50'"
                >
                  {{ item.role }}
                </span>
                <span shrink-0 text-xs text-tertiary>{{ fmtTime(item.createdAt) }}</span>
              </li>
            </ul>
          </section>
        </div>
      </NTabPane>

      <!-- ━━━━━━━━━━━━━━━ 批量导入 ━━━━━━━━━━━━━━━ -->
      <NTabPane name="batch" tab="批量导入">
        <div
          mt-2
          grid="~ cols-1 lg:cols-2"
          gap-6
        >
          <!-- 输入区 -->
          <section
            p-6
            rounded-xl
            bg-elevated
            border="~ subtle"
            shadow-sm
            flex="~ col gap-4"
          >
            <div flex="~ items-center justify-between gap-3">
              <h2 m-0 text-lg font-semibold>
                CSV 输入
              </h2>
              <NSpace size="small">
                <NButton size="small" tertiary @click="fillBatchTemplate">
                  填充示例
                </NButton>
                <NButton size="small" tertiary @click="resetBatch">
                  清空
                </NButton>
              </NSpace>
            </div>
            <NInput
              v-model:value="batchInput"
              type="textarea"
              :rows="14"
              :placeholder="batchPlaceholder"
              font-mono
            />
            <NSpace>
              <NButton type="default" size="medium" @click="parseBatchInput">
                解析预览
              </NButton>
              <NPopconfirm
                :positive-text="`确认创建 ${validParsed.length} 个账号`"
                negative-text="取消"
                @positive-click="submitBatch"
              >
                <template #trigger>
                  <NButton
                    type="primary"
                    size="medium"
                    :loading="batchSubmitting"
                    :disabled="validParsed.length === 0"
                  >
                    提交批量创建（{{ validParsed.length }} 条）
                  </NButton>
                </template>
                即将批量创建 <strong>{{ validParsed.length }}</strong> 个账号 ·
                单行失败不会回滚整批，结果会按行展示。<br>
                请确认所有初始密码已通过安全渠道分发。
              </NPopconfirm>
            </NSpace>
            <p v-if="parsedRows.length > 0" m-0 text-sm text-tertiary>
              已解析 <strong text-primary>{{ parsedRows.length }}</strong> 行 ·
              可提交 <strong text-success-700>{{ validParsed.length }}</strong> ·
              本地校验失败 <strong text-danger-700>{{ invalidParsed.length }}</strong>
            </p>
          </section>

          <!-- 解析预览 + 提交结果 -->
          <section
            p-6
            rounded-xl
            bg-elevated
            border="~ subtle"
            shadow-sm
            min-h-420px
            flex="~ col gap-4"
          >
            <div flex="~ items-center justify-between gap-3">
              <h2 m-0 text-lg font-semibold>
                <template v-if="batchResult">
                  提交结果
                </template>
                <template v-else>
                  解析预览
                </template>
              </h2>
              <NButton
                v-if="batchResult && batchResult.failureCount > 0"
                size="small"
                tertiary
                @click="copyFailedRows"
              >
                复制失败行
              </NButton>
            </div>

            <!-- 提交后：服务器返回结果 -->
            <template v-if="batchResult">
              <div
                p-3
                rounded-md
                bg-muted
                grid="~ cols-2"
                gap-2
                text-sm
              >
                <div>
                  <p m-0 text-xs text-tertiary>
                    成功
                  </p>
                  <p m-0 text-2xl font-bold text-success-700>
                    {{ batchResult.successCount }}
                  </p>
                </div>
                <div>
                  <p m-0 text-xs text-tertiary>
                    失败
                  </p>
                  <p m-0 text-2xl font-bold :class="batchResult.failureCount > 0 ? 'text-danger-700' : 'text-tertiary'">
                    {{ batchResult.failureCount }}
                  </p>
                </div>
              </div>
              <ul m-0 p-0 list-none flex="~ col gap-2" overflow-y-auto max-h-400px>
                <li
                  v-for="item in batchResult.items"
                  :key="`${item.rowIndex}-${item.email}`"
                  px-3 py-2
                  rounded-md
                  border="~ subtle"
                  flex="~ items-center justify-between gap-3"
                  :class="item.success ? 'bg-success-50 border-success-500/30' : 'bg-danger-50 border-danger-500/30'"
                >
                  <div min-w-0 flex="~ col gap-2px">
                    <span font-mono text-sm text-primary truncate>{{ item.email }}</span>
                    <span v-if="item.success" text-xs text-success-700>
                      ✓ 已创建 · userId={{ item.userId }} · role={{ item.role }}
                    </span>
                    <span v-else text-xs text-danger-700>
                      ✗ {{ item.errorMsg }}（code={{ item.errorCode }}）
                    </span>
                  </div>
                  <span shrink-0 text-xs text-tertiary>row {{ item.rowIndex + 1 }}</span>
                </li>
              </ul>
            </template>

            <!-- 提交前：本地预览 -->
            <template v-else>
              <div v-if="parsedRows.length === 0" py-12 text-center text-tertiary text-sm>
                左侧填入 CSV 后点击「解析预览」，本地校验失败的行会标红
              </div>
              <ul v-else m-0 p-0 list-none flex="~ col gap-2" overflow-y-auto max-h-480px>
                <li
                  v-for="row in parsedRows"
                  :key="`pre-${row.rowIndex}`"
                  px-3 py-2
                  rounded-md
                  border="~ subtle"
                  flex="~ items-center justify-between gap-3"
                  :class="row.error ? 'bg-danger-50 border-danger-500/30' : 'bg-elevated'"
                >
                  <div min-w-0 flex="~ col gap-2px">
                    <span v-if="row.data" font-mono text-sm text-primary truncate>
                      {{ row.data.email }} · {{ row.data.fullName }} · {{ row.data.role }}
                    </span>
                    <span v-else font-mono text-sm text-secondary truncate>
                      {{ row.raw }}
                    </span>
                    <span v-if="row.error" text-xs text-danger-700>
                      ✗ {{ row.error }}
                    </span>
                    <span v-else text-xs text-tertiary>
                      ✓ 待提交
                    </span>
                  </div>
                  <span shrink-0 text-xs text-tertiary>row {{ row.rowIndex + 1 }}</span>
                </li>
              </ul>
            </template>
          </section>
        </div>
      </NTabPane>
    </NTabs>
  </div>
</template>
