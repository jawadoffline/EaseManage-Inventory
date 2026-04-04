import apiClient from './client';
import type { User, CreateUserRequest, UpdateUserRequest, PagedResponse } from '../types';

export interface UserQueryParams {
  page?: number;
  size?: number;
  role?: string;
  status?: string;
  search?: string;
}

export const usersApi = {
  getAll: (params: UserQueryParams = {}) =>
    apiClient.get<PagedResponse<User>>('/users', { params }).then((r) => r.data),

  getById: (id: number) =>
    apiClient.get<User>(`/users/${id}`).then((r) => r.data),

  getMe: () =>
    apiClient.get<User>('/users/me').then((r) => r.data),

  create: (data: CreateUserRequest) =>
    apiClient.post<User>('/users', data).then((r) => r.data),

  update: (id: number, data: UpdateUserRequest) =>
    apiClient.put<User>(`/users/${id}`, data).then((r) => r.data),

  updateProfile: (data: { firstName?: string; lastName?: string; email?: string }) =>
    apiClient.put<User>('/users/me', data).then(r => r.data),

  changePassword: (currentPassword: string, newPassword: string) =>
    apiClient.post<{ message: string }>('/users/me/change-password', { currentPassword, newPassword }).then(r => r.data),

  delete: (id: number) =>
    apiClient.delete(`/users/${id}`),
};
