import apiClient from './client';
import type { PagedResponse } from '../types';

export interface Supplier {
  id: number;
  name: string;
  email: string | null;
  phone: string | null;
  address: string | null;
  city: string | null;
  country: string | null;
  contactPerson: string | null;
  paymentTerms: string | null;
  isActive: boolean;
  createdAt: string;
}

export interface SupplierRequest {
  name: string;
  email?: string;
  phone?: string;
  address?: string;
  city?: string;
  country?: string;
  contactPerson?: string;
  paymentTerms?: string;
  isActive?: boolean;
}

export const suppliersApi = {
  getAll: (params: { page?: number; size?: number; search?: string } = {}) =>
    apiClient.get<PagedResponse<Supplier>>('/suppliers', { params }).then(r => r.data),

  getAllActive: () =>
    apiClient.get<Supplier[]>('/suppliers/active').then(r => r.data),

  getById: (id: number) =>
    apiClient.get<Supplier>(`/suppliers/${id}`).then(r => r.data),

  create: (data: SupplierRequest) =>
    apiClient.post<Supplier>('/suppliers', data).then(r => r.data),

  update: (id: number, data: SupplierRequest) =>
    apiClient.put<Supplier>(`/suppliers/${id}`, data).then(r => r.data),

  delete: (id: number) =>
    apiClient.delete(`/suppliers/${id}`),
};
