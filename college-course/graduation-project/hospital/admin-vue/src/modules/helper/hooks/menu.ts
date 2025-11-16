import { ElMessage } from 'element-plus'
import { service } from '/@/cool'

export function useMenu() {
  // 根据 router 删除菜单
  async function del(router: string) {
    const menus = await service.base.sys.menu.list()
    const item = menus.find(e => e.router == router)
    if (item) {
      await service.base.sys.menu.delete({ ids: [item.id] })
    }
  }

  // 创建菜单、权限、文件
  function create(data: EpsModule): Promise<() => void> {
    // eslint-disable-next-line no-async-promise-executor
    return new Promise(async (resolve, reject) => {
      // 视图文件路径
      data.viewPath = `modules/${data.module}/views${data.router?.replace(
        `/${data.module}`,
        '',
      )}.vue`

      // 删除原菜单
      await del(data.router)

      // 添加新菜单
      service.base.sys.menu
        .add({
          type: 1,
          isShow: true,
          keepAlive: true,
          ...data,
          api: undefined,
          code: undefined,
        })
        .then((res) => {
          // 权限列表
          const perms = data.api?.map((e) => {
            const d = {
              type: 2,
              parentId: res.id,
              name: e.summary || e.path,
              perms: [e.path],
            }

            if (e.path == '/update') {
              if (data.api?.find(a => a.path == '/info')) {
                d.perms.push('/info')
              }
            }

            return {
              ...d,
              perms: d.perms
                .map(e => (data.prefix?.replace('/admin/', '') + e).replace(/\//g, ':'))
                .join(','),
            }
          })

          // 批量插入权限
          service.base.sys.menu.add(perms).then(() => {
            resolve(() => {
              // 创建文件
              service
                .request({
                  url: '/__cool_createFile',
                  method: 'POST',
                  proxy: false,
                  data: {
                    code: data.code,
                    path: data.viewPath,
                  },
                })
                .then(() => {
                  location.reload()
                })
            })
          })
        })
        .catch((err) => {
          ElMessage.error(err.message)
          reject()
        })
    })
  }

  return {
    del,
    create,
  }
}
