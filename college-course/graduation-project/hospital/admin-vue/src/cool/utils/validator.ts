export function phoneValidator(rule: any, value: any, callback: any) {
  if (!value) {
    callback(new Error('请输入手机号'))
  }
  if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  }
  callback()
}
