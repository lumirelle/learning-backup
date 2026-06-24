/**
 * Use namespace to define types related to API products, in order to avoid potential naming conflicts in the global
 * scope.
 */
namespace ApiProducts {
  interface QueryParams {
    category?: string
    limit?: number
    offset?: number
  }

  interface UpsertBody {
    id?: string
    name: string
    price: number
    category: string
  }

  interface Product {
    id: string
    name: string
    price: number
    category: string
  }
}
