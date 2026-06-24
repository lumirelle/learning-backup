import type { Nullable } from '@antfu/utils'

export interface ServerResponse<DataT, MetaT extends Record<string, any>> {
  data: Nullable<DataT>
  meta: Nullable<MetaT>
}
