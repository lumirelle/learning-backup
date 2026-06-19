import { get, patch, post } from './request'

export interface MeVO {
  id: number
  email: string
  fullName: string
  role: 'ADMIN' | 'HR' | 'CANDIDATE'
  interests?: string[]
}

export interface TokenVO {
  accessToken: string
  expiresIn: number
  tokenType: string
  user: MeVO
}

export const authApi = {
  register: (data: { email: string, password: string, fullName: string, interests?: string[] }) =>
    post<MeVO>('/auth/register', data),

  login: (data: { email: string; password: string }) =>
    post<TokenVO>('/auth/login', data),

  /** refresh token 由浏览器自动携带 cookie，无需显式传参 */
  refresh: () => post<TokenVO>('/auth/refresh'),

  logout: () => post<void>('/auth/logout'),

  me: () => get<MeVO>('/auth/me'),

  changePassword: (data: { currentPassword: string, newPassword: string }) =>
    post<void>('/auth/change-password', data),

  updateProfile: (data: { interests?: string[] }) =>
    patch<MeVO>('/auth/profile', data),
}
