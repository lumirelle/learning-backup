import type { Nullable } from '@antfu/utils'
import type { ServerResponse } from '../types/response'

/**
 * Create a standard server response.
 *
 * @param data Data response to client
 * @param meta Metadata
 * @returns Standard server response object
 */
export function createResponse<
  DataT,
  MetaT extends Record<string, any> = Record<string, any>,
>(data: Nullable<DataT> = null, meta: Nullable<MetaT> = null): ServerResponse<DataT, MetaT> {
  return {
    data,
    meta,
  }
}

/**
 * Create a standard server response for pagination queries.
 *
 * @param data Array of data items
 * @param total Total number of items
 * @param page Current page number
 * @param pageSize Number of items per page
 * @returns Standard server response object with pagination metadata
 */
export function createQueryResponse<T>(
  data: T[],
  total: number,
  page: number,
  pageSize: number,
): ServerResponse<T[], { total: number, page: number, pageSize: number, pageCount: number }> {
  return createResponse(data, {
    total,
    page,
    pageSize,
    pageCount: Math.ceil(total / pageSize),
  })
}
