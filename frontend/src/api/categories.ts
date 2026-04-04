import apiClient from './client';
import type { PagedResponse } from '../types';

export interface Category {
  id: number;
  name: string;
  description: string | null;
  parentId: number | null;
  parentName: string | null;
  createdAt: string;
  children: Category[] | null;
}

export interface CategoryRequest {
  name: string;
  description?: string;
  parentId?: number | null;
}

export const categoriesApi = {
  getAll: (params: { page?: number; size?: number; search?: string } = {}) =>
    apiClient.get<PagedResponse<Category>>('/categories', { params }).then(r => r.data),

  getTree: () =>
    apiClient.get<Category[]>('/categories/tree').then(r => r.data),

  getAllFlat: () =>
    apiClient.get<Category[]>('/categories/all').then(r => r.data),

  getById: (id: number) =>
    apiClient.get<Category>(`/categories/${id}`).then(r => r.data),

  create: (data: CategoryRequest) =>
    apiClient.post<Category>('/categories', data).then(r => r.data),

  update: (id: number, data: CategoryRequest) =>
    apiClient.put<Category>(`/categories/${id}`, data).then(r => r.data),

  delete: (id: number) =>
    apiClient.delete(`/categories/${id}`),
};
