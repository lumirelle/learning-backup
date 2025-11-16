import store from 'store'

export default {
  // 后缀标识
  suffix: '_deadtime',

  /**
   * 获取
   * @param {string} key 关键字
   */
  get(key: string) {
    return store.get(key)
  },

  /**
   * 获取全部
   */
  info() {
    const data: Record<string, any> = {}

    store.each((value: any, key: any) => {
      data[key] = value
    })

    return data
  },

  /**
   * 设置
   * @param {string} key 关键字
   * @param {*} value 值
   * @param {number} expires 过期时间
   */
  set(key: string, value: any, expires?: number) {
    store.set(key, value)

    if (expires) {
      const expirationTime = Date.now() + expires * 1000
      store.set(`${key}${this.suffix}`, expirationTime)
    }
  },

  /**
   * 是否过期
   * @param {string} key 关键字
   */
  isExpired(key: string) {
    const expiration = this.getExpiration(key) || 0
    return expiration - Date.now() <= 2000
  },

  /**
   * 获取到期时间
   * @param {string} key 关键字
   */
  getExpiration(key: string) {
    return this.get(key + this.suffix)
  },

  /**
   * 移除
   * @param {string} key 关键字
   */
  remove(key: string) {
    store.remove(key)
    this.removeExpiration(key)
  },

  /**
   * 移除到期时间
   * @param {string} key 关键字
   */
  removeExpiration(key: string) {
    store.remove(key + this.suffix)
  },

  /**
   * 清理
   */
  clearAll() {
    store.clearAll()
  },
}
