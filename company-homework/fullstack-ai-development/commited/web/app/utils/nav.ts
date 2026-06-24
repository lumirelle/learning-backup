// 全局导航配置：侧栏分组渲染 + 顶栏面包屑共用，避免两处各自硬编码。

export interface NavItem {
  to: string
  label: string
  icon: string
}

export interface NavGroup {
  label?: string
  items: NavItem[]
}

export const navGroups: NavGroup[] = [
  {
    items: [
      { to: '/', label: '工作台', icon: 'i-carbon-home' },
    ],
  },
  {
    label: '组织与人员',
    items: [
      { to: '/org', label: '组织架构', icon: 'i-carbon-tree-view-alt' },
      { to: '/roster', label: '员工花名册', icon: 'i-carbon-user-multiple' },
      { to: '/archives', label: '档案库', icon: 'i-carbon-box' },
    ],
  },
  {
    label: '事务与流程',
    items: [
      { to: '/affairs', label: '人事事务', icon: 'i-carbon-flow' },
      { to: '/contracts', label: '合同管理', icon: 'i-carbon-document-signed' },
      { to: '/performance', label: '任职奖惩', icon: 'i-carbon-trophy' },
      { to: '/care', label: '员工关怀', icon: 'i-carbon-favorite' },
    ],
  },
  {
    label: '洞察与报表',
    items: [
      { to: '/analytics', label: '统计分析', icon: 'i-carbon-chart-bar' },
      { to: '/reports', label: '人事报表', icon: 'i-carbon-report' },
      { to: '/timeline', label: '人事动态', icon: 'i-carbon-time' },
    ],
  },
]

export const navItems: NavItem[] = navGroups.flatMap(g => g.items)

/** 当前路由对应的导航项（顶栏面包屑用） */
export function matchNav(path: string): NavItem | undefined {
  if (path === '/')
    return navItems[0]
  return navItems.find(i => i.to !== '/' && path.startsWith(i.to))
}
