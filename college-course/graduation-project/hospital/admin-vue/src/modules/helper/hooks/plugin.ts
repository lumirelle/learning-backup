import { ElMessage, ElMessageBox } from 'element-plus'
import { mitt, router, service } from '/@/cool'
import { t } from '/@/plugins/i18n'

export function usePlugin() {
  function getName(file: File) {
    return file.name.replace('.cool', '')
  }

  // 注册
  function register() {
    document.body.addEventListener('dragover', (e) => {
      e.preventDefault()
    })
    document.body.addEventListener('drop', (e) => {
      e.preventDefault()

      if (e.dataTransfer) {
        const file = e.dataTransfer.files[0]

        if (file.name.endsWith('.cool')) {
          ElMessageBox.confirm(
            t('检测到插件「{name}」，是否安装？', { name: getName(file) }),
            t('提示'),
            {
              type: 'warning',
              confirmButtonText: t('安装'),
            },
          )
            .then(() => {
              install(file)
            })
            .catch(() => null)
        }
      }
    })
  }

  // 安装
  function install(file: File) {
    const next = (force: boolean) => {
      const data = new FormData()

      data.append('files', file)
      data.append('force', String(force))

      service.plugin.info
        .request({
          url: '/install',
          method: 'POST',
          data,
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        })
        .then((res) => {
          if (!res) {
            // 发送事件
            mitt.emit('plugin.refresh')

            // 标题
            const title = t('插件「{name}」安装成功', { name: getName(file) })

            // 是否插件页面
            if (router.currentRoute.value.path == '/helper/plugins') {
              ElMessage.success(title)
            }
            else {
              ElMessageBox.alert(title, t('提示'), {
                type: 'success',
                confirmButtonText: t('点击查看'),
                showCancelButton: true,
              })
                .then(() => {
                  router.push('/helper/plugins')
                })
                .catch(() => null)
            }

            return
          }

          if (res.type == 0) {
            ElMessageBox.confirm(res.message, t('提示'), {
              type: 'error',
              showConfirmButton: false,
            })
              .then(() => {
                next(true)
              })
              .catch(() => null)
          }

          if (res.type == 1 || res.type == 2) {
            ElMessageBox.confirm(res.message, t('提示'), {
              type: 'warning',
              confirmButtonText: t('继续'),
            })
              .then(() => {
                next(true)
              })
              .catch(() => null)
          }

          if (res.type == 3) {
            next(true)
          }
        })
        .catch((err) => {
          ElMessage.error(err.message)
        })
    }

    next(false)
  }

  return {
    register,
    install,
  }
}
