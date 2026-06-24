<script lang="ts" setup>
// 可折叠树。避免模板内递归组件（Nuxt 自动导入对自引用组件有 TDZ 问题）：
// 在 JS 里把树拍平为带 depth 的列表，按「折叠集合」过滤掉被收起节点的后代，用缩进渲染。
const props = withDefaults(defineProps<{
  nodes?: Hr.OrgNode[]
  defaultExpandDepth?: number // 初始展开到第几层（含），更深的默认折叠
}>(), { defaultExpandDepth: 1 })

// 折叠集合：记录被「收起」的节点 id。初始按 defaultExpandDepth 折叠深层节点。
const collapsed = ref<Set<string>>(new Set())
let inited = false
function initCollapsed(nodes: Hr.OrgNode[] = [], depth = 0) {
  for (const n of nodes) {
    if (n.children?.length && depth >= props.defaultExpandDepth)
      collapsed.value.add(n.id)
    initCollapsed(n.children, depth + 1)
  }
}
watchEffect(() => {
  if (!inited && props.nodes?.length) {
    initCollapsed(props.nodes)
    inited = true
  }
})

interface Flat { node: Hr.OrgNode, depth: number, hasChildren: boolean }
const flat = computed(() => {
  const out: Flat[] = []
  const walk = (nodes: Hr.OrgNode[] = [], depth = 0) => {
    for (const n of nodes) {
      const hasChildren = !!n.children?.length
      out.push({ node: n, depth, hasChildren })
      if (hasChildren && !collapsed.value.has(n.id))
        walk(n.children, depth + 1)
    }
  }
  walk(props.nodes)
  return out
})

function toggle(id: string) {
  const s = new Set(collapsed.value)
  if (s.has(id))
    s.delete(id)
  else s.add(id)
  collapsed.value = s
}

function nodeIcon(node: Hr.OrgNode, depth: number) {
  if (node.type === 'org' || node.type === 'group')
    return depth === 0 ? 'i-carbon-enterprise' : 'i-carbon-building'
  return 'i-carbon-folder'
}
</script>

<template>
  <ul class="flex flex-col">
    <li
      v-for="{ node, depth, hasChildren } in flat" :key="node.id"
      class="text-sm py-1.5 pr-2 rounded-lg flex gap-1.5 transition-colors items-center hover:bg-truegray-50 dark:hover:bg-white/4"
      :style="{ paddingLeft: `${depth * 18 + 8}px` }"
    >
      <button
        v-if="hasChildren"
        type="button"
        :aria-label="collapsed.has(node.id) ? `展开 ${node.name}` : `收起 ${node.name}`"
        class="text-truegray-400 flex shrink-0 transition-transform hover:text-primary"
        :class="collapsed.has(node.id) ? '' : 'rotate-90'"
        @click="toggle(node.id)"
      >
        <span class="i-carbon-chevron-right block" />
      </button>
      <span v-else class="shrink-0 w-4" />

      <span
        class="shrink-0"
        :class="[nodeIcon(node, depth), depth === 0 ? 'text-primary' : 'text-truegray-300 dark:text-truegray-500']"
      />
      <span
        class="font-medium"
        :class="depth === 0 ? 'text-truegray-900 dark:text-white' : 'text-truegray-700 dark:text-truegray-300'"
      >
        {{ node.name }}
      </span>
      <HBadge v-if="(node.type === 'org' || node.type === 'group') && depth === 0" tone="blue" label="集团" />
    </li>
  </ul>
</template>
