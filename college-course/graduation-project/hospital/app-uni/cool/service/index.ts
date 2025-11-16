import { BaseService } from './base'

// service 数据集合
// @ts-expect-error xxx
export const service: Eps.Service = {
  request: new BaseService().request,
}

export * from './base'
