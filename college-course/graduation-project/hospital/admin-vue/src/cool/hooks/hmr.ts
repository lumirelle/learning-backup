// 解决热更新后数据失效问题
// 初始化数据对象，如果热更新数据存在则使用它
const data = import.meta.hot?.data.getData?.() || {}

// 检查是否支持热更新
if (import.meta.hot) {
  // 将当前数据存储函数赋值给热更新数据对象
  import.meta.hot.data.getData = () => {
    return data
  }
}

// 导出一个热更新模块对象
export const hmr = {
  data, // 当前数据对象

  // 设置数据的方法
  setData(key: string, value: any) {
    // 将指定键值对存入数据对象
    data[key] = value
  },

  // 获取数据的方法
  getData(key: string, defaultValue?: any) {
    // 如果指定键不存在且提供了默认值，则设置默认值
    if (defaultValue !== undefined && !data[key]) {
      this.setData(key, defaultValue)
    }
    // 返回指定键的值
    return data[key]
  },
}
