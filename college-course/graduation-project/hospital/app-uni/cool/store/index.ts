import { useDictStore } from './dict'
import { useMealStore } from './meal'
import { useOrderStore } from './order'
import { useUserStore } from './user'
import { useFeedbackStore } from './feedback'

export function useStore() {
  return {
    user: useUserStore(),
    dict: useDictStore(),
    meal: useMealStore(),
    order: useOrderStore(),
    feedback: useFeedbackStore(),
  }
}
