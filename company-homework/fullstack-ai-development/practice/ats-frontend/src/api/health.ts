import { get } from './request'

export interface HealthVO {
  app: string
  time: string
  db: 'UP' | 'DOWN'
  redis: 'UP' | 'DOWN'
}

export function getHealth() {
  return get<HealthVO>('/health')
}
