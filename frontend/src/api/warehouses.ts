import apiClient from './client';
import type { PagedResponse } from '../types';

export interface Warehouse {
  id: number;
  name: string;
  code: string;
  address: string | null;
  city: string | null;
  state: string | null;
  country: string | null;
  capacity: number | null;
  isActive: boolean;
  createdAt: string;
}

export interface WarehouseRequest {
  name: string;
  code: string;
  address?: string;
  city?: string;
  state?: string;
  country?: string;
  capacity?: number;
  isActive?: boolean;
}

export const warehousesApi = {
  getAll: (params: { page?: number; size?: number; search?: string } = {}) =>
    apiClient.get<PagedResponse<Warehouse>>('/warehouses', { params }).then(r => r.data),

  getAllActive: () =>
    apiClient.get<Warehouse[]>('/warehouses/active').then(r => r.data),

  getById: (id: number) =>
    apiClient.get<Warehouse>(`/warehouses/${id}`).then(r => r.data),

  create: (data: WarehouseRequest) =>
    apiClient.post<Warehouse>('/warehouses', data).then(r => r.data),

  update: (id: number, data: WarehouseRequest) =>
    apiClient.put<Warehouse>(`/warehouses/${id}`, data).then(r => r.data),

  delete: (id: number) =>
    apiClient.delete(`/warehouses/${id}`),
};
