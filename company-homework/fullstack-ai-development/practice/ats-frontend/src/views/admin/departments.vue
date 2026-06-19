<script setup lang="ts">
import type { FormInst, FormRules, TreeOption } from 'naive-ui'
import type { OrgTreeNodeVO } from '@/api/organizations'
import {
  NButton,
  NForm,
  NFormItem,
  NInput,
  NPopconfirm,
  NSpace,
  NSpin,
  NTag,
  NTree,
  useMessage,
} from 'naive-ui'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { organizationsApi } from '@/api/organizations'
import { BizError } from '@/api/request'

const msg = useMessage()

const loading = ref(false)
const saving = ref(false)
const treeRoot = ref<OrgTreeNodeVO | null>(null)
const selectedKeys = ref<string[]>([])

const treeData = computed<TreeOption[]>(() =>
  treeRoot.value ? [treeRoot.value as unknown as TreeOption] : [],
)

function findNode(node: OrgTreeNodeVO, key: string): OrgTreeNodeVO | null {
  if (node.key === key)
    return node
  for (const c of node.children ?? []) {
    const hit = findNode(c, key)
    if (hit)
      return hit
  }
  return null
}

const selectedNode = computed<OrgTreeNodeVO | null>(() => {
  const key = selectedKeys.value[0]
  if (!key || !treeRoot.value)
    return null
  return findNode(treeRoot.value, key)
})

const nodeTypeLabel = computed(() => {
  const n = selectedNode.value
  if (!n)
    return ''
  if (n.nodeType === 'ROOT')
    return '根组织'
  if (n.nodeType === 'DEPARTMENT')
    return '部门'
  return '子部门'
})

async function loadTree() {
  loading.value = true
  try {
    treeRoot.value = await organizationsApi.tree()
    if (selectedKeys.value.length && !selectedNode.value)
      selectedKeys.value = []
  }
  catch (e) {
    if (e instanceof BizError)
      msg.error(e.message)
    else throw e
  }
  finally {
    loading.value = false
  }
}

onMounted(loadTree)

// ── 编辑表单 ──
const editFormRef = ref<FormInst | null>(null)
const editForm = reactive({ name: '', location: '' })
const editRules = computed<FormRules>(() => {
  const r: FormRules = {
    name: [{ required: true, message: '名称必填', trigger: 'blur' }],
  }
  if (selectedNode.value?.nodeType === 'SUB_DEPARTMENT') {
    r.location = [{ required: true, message: '工作地点必填', trigger: 'blur' }]
  }
  return r
})

watch(selectedNode, (n) => {
  if (!n || n.nodeType === 'ROOT') {
    editForm.name = ''
    editForm.location = ''
    return
  }
  editForm.name = n.label
  editForm.location = n.location ?? ''
}, { immediate: true })

async function saveSelected() {
  const n = selectedNode.value
  if (!n || n.nodeType === 'ROOT')
    return
  try {
    await editFormRef.value?.validate()
  }
  catch {
    return
  }
  saving.value = true
  try {
    if (n.nodeType === 'DEPARTMENT') {
      await organizationsApi.updateDepartment(n.id, { name: editForm.name.trim() })
    }
    else {
      await organizationsApi.updateSubDepartment(n.id, {
        name: editForm.name.trim(),
        location: editForm.location.trim(),
      })
    }
    msg.success('已保存')
    await loadTree()
    selectedKeys.value = [n.key]
  }
  catch (e) {
    if (e instanceof BizError)
      msg.error(e.message)
  }
  finally {
    saving.value = false
  }
}

// ── 新建子节点（弹窗式内联区） ──
const createMode = ref<'department' | 'sub' | null>(null)
const createParentDeptId = ref<number | null>(null)
const createFormRef = ref<FormInst | null>(null)
const createForm = reactive({ name: '', location: '' })
const createRules = computed<FormRules>(() => {
  const r: FormRules = {
    name: [{ required: true, message: '名称必填', trigger: 'blur' }],
  }
  if (createMode.value === 'sub') {
    r.location = [{ required: true, message: '工作地点必填', trigger: 'blur' }]
  }
  return r
})

function openCreateDepartment() {
  const n = selectedNode.value
  createMode.value = 'department'
  createParentDeptId.value = n?.nodeType === 'DEPARTMENT' ? n.id : null
  createForm.name = ''
  createForm.location = ''
}

function openCreateSubDepartment() {
  const n = selectedNode.value
  if (!n || n.nodeType !== 'DEPARTMENT') {
    msg.warning('请先选中一个「部门」节点，再添加子部门')
    return
  }
  createMode.value = 'sub'
  createParentDeptId.value = n.id
  createForm.name = ''
  createForm.location = ''
}

function cancelCreate() {
  createMode.value = null
}

async function submitCreate() {
  try {
    await createFormRef.value?.validate()
  }
  catch {
    return
  }
  saving.value = true
  try {
    if (createMode.value === 'department') {
      const created = await organizationsApi.createDepartment({
        name: createForm.name.trim(),
        parentDepartmentId: createParentDeptId.value,
      })
      msg.success('部门已创建')
      await loadTree()
      selectedKeys.value = [created.key]
    }
    else if (createMode.value === 'sub' && createParentDeptId.value != null) {
      const created = await organizationsApi.createSubDepartment({
        parentDepartmentId: createParentDeptId.value,
        name: createForm.name.trim(),
        location: createForm.location.trim(),
      })
      msg.success('子部门已创建')
      await loadTree()
      selectedKeys.value = [created.key]
    }
    createMode.value = null
  }
  catch (e) {
    if (e instanceof BizError)
      msg.error(e.message)
  }
  finally {
    saving.value = false
  }
}

async function deleteSelected() {
  const n = selectedNode.value
  if (!n || n.nodeType === 'ROOT')
    return
  saving.value = true
  try {
    if (n.nodeType === 'DEPARTMENT')
      await organizationsApi.deleteDepartment(n.id)
    else
      await organizationsApi.deleteSubDepartment(n.id)
    msg.success('已删除')
    selectedKeys.value = []
    await loadTree()
  }
  catch (e) {
    if (e instanceof BizError)
      msg.error(e.message)
  }
  finally {
    saving.value = false
  }
}

const canAddDepartment = computed(() => {
  const n = selectedNode.value
  return !n || n.nodeType === 'ROOT' || n.nodeType === 'DEPARTMENT'
})

const canAddSub = computed(() => selectedNode.value?.nodeType === 'DEPARTMENT')
const canEdit = computed(() => selectedNode.value?.editable === true)
const canDelete = computed(() =>
  selectedNode.value?.nodeType === 'DEPARTMENT' || selectedNode.value?.nodeType === 'SUB_DEPARTMENT',
)
</script>

<template>
  <div max-w-1400px mx-auto p="x-6 b-16" class="pt-[calc(60px+40px)]">
    <header mb-8>
      <p kicker mb-2>
        Admin · 组织管理
      </p>
      <h1 m-0 text-36px text-gray-900 font="display black" tracking="[-0.03em]" leading="[1.05]">
        部门树 · <span text-gradient>xx科技集团</span>
      </h1>
      <p mt-2 text-secondary>
        根节点固定不可编辑。部门可嵌套；子部门为叶子，承载工作地点，并关联 HR 与岗位。
      </p>
    </header>

    <div grid="~ cols-1 lg:cols-[minmax(280px,360px)_1fr]" gap-6>
      <!-- 树 -->
      <section p-5 rounded-xl bg-elevated border="~ subtle" shadow-sm min-h-480px>
        <div flex="~ justify-between items-center" mb-4>
          <h2 m-0 text-base font-semibold>
            组织结构
          </h2>
          <NButton size="tiny" quaternary :loading="loading" @click="loadTree">
            刷新
          </NButton>
        </div>
        <NSpin :show="loading">
          <NTree v-if="treeData.length" v-model:selected-keys="selectedKeys" block-line selectable :data="treeData"
            key-field="key" label-field="label" children-field="children" default-expand-all />
          <p v-else m-0 text-sm text-tertiary>
            暂无组织数据
          </p>
        </NSpin>
      </section>

      <!-- 详情 / 操作 -->
      <section p-5 rounded-xl bg-elevated border="~ subtle" shadow-sm min-h-480px>
        <template v-if="!selectedNode">
          <p m-0 text-secondary text-sm>
            在左侧选择节点，可查看详情或执行增删改。
          </p>
        </template>

        <template v-else>
          <div flex="~ items-center wrap" gap-2 mb-4>
            <h2 m-0 text-lg font-semibold>
              {{ selectedNode.label }}
            </h2>
            <NTag size="small" :bordered="false" type="info">
              {{ nodeTypeLabel }}
            </NTag>
            <NTag v-if="selectedNode.nodeType === 'SUB_DEPARTMENT'" size="small" :bordered="false">
              {{ selectedNode.location }}
            </NTag>
          </div>

          <NSpace mb-4>
            <NButton v-if="canAddDepartment" size="small" @click="openCreateDepartment">
              + 部门
            </NButton>
            <NButton v-if="canAddSub" size="small" type="primary" @click="openCreateSubDepartment">
              + 子部门
            </NButton>
            <NPopconfirm v-if="canDelete" @positive-click="deleteSelected">
              <template #trigger>
                <NButton size="small" type="error" tertiary :loading="saving">
                  删除节点
                </NButton>
              </template>
              确认删除？子部门下仍有岗位或 HR 绑定时将无法删除。
            </NPopconfirm>
          </NSpace>

          <!-- 新建 -->
          <div v-if="createMode" mb-6 p-4 rounded-lg border="~ subtle" bg-app>
            <p m-0 mb-3 text-sm font-medium text-primary>
              {{ createMode === 'department' ? '新建部门' : '新建子部门' }}
              <span v-if="createParentDeptId" text-tertiary font-normal>（父部门 #{{ createParentDeptId }}）</span>
            </p>
            <NForm ref="createFormRef" :model="createForm" :rules="createRules" label-placement="top" size="small">
              <NFormItem label="名称" path="name">
                <NInput v-model:value="createForm.name" :placeholder="createMode === 'sub' ? '技术研发-上海浦东' : '技术研发'" />
              </NFormItem>
              <NFormItem v-if="createMode === 'sub'" label="工作地点" path="location">
                <NInput v-model:value="createForm.location" placeholder="上海·浦东" />
              </NFormItem>
              <NSpace>
                <NButton type="primary" size="small" :loading="saving" @click="submitCreate">
                  创建
                </NButton>
                <NButton size="small" @click="cancelCreate">
                  取消
                </NButton>
              </NSpace>
            </NForm>
          </div>

          <!-- 编辑 -->
          <div v-if="canEdit">
            <p m-0 mb-3 text-sm text-secondary>
              编辑当前节点
            </p>
            <NForm ref="editFormRef" :model="editForm" :rules="editRules" label-placement="top" size="medium">
              <NFormItem label="名称" path="name">
                <NInput v-model:value="editForm.name" />
              </NFormItem>
              <NFormItem v-if="selectedNode.nodeType === 'SUB_DEPARTMENT'" label="工作地点" path="location">
                <NInput v-model:value="editForm.location" />
              </NFormItem>
              <NButton type="primary" :loading="saving" @click="saveSelected">
                保存修改
              </NButton>
            </NForm>
          </div>

          <p v-else-if="selectedNode.nodeType === 'ROOT'" m-0 text-sm text-tertiary>
            根组织「xx科技集团」不可编辑。可在其下添加部门。
          </p>
        </template>
      </section>
    </div>
  </div>
</template>
