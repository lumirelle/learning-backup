declare interface EpsColumn {
  comment: string
  length: number
  nullable: boolean
  propertyName: string
  type: string
  component: string
  defaultValue: any
  dict: string[] | string
}

declare interface EpsApi {
  path: string
  summary: string
  method?: string
  prefix?: string
  tag?: string
  dts?: any
  [key: string]: any
}

declare interface EpsModule {
  api: EpsApi[]
  columns: EpsColumn[]
  prefix: string
  router: string
  module: string
  fieldEq?: string[]
  keyWordLikeFields?: string[]
  [key: string]: any
}

declare interface EpsData {
  [key: string]: EpsModule[]
}
