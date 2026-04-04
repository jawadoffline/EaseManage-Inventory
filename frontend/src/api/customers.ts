import apiClient from './client';
import type { PagedResponse } from '../types';

export interface Customer {
  id: number;
  name: string;
  email: string | null;
  phone: string | null;
  address: string | null;
  city: string | null;
  country: string | null;
  contactPerson: string | null;
  notes: string | null;
  isActive: boolean;
  createdAt: string;
}

export interface CustomerRequest {
  name: string;
  email?: string;
  phone?: string;
  address?: string;
  city?: string;
  country?: string;
  contactPerson?: string;
  notes?: string;
  isActive?: boolean;
}

export const customersApi = {
  getAll: (params: { page?: number; size?: number; search?: string } = {}) =>
    apiClient.get<PagedResponse<Customer>>('/customers', { params }).then(r => r.data),
  getAllActive: () =>
    apiClient.get<Customer[]>('/customers/active').then(r => r.data),
  getById: (id: number) =>
    apiClient.get<Customer>(`/customers/${id}`).then(r => r.data),
  create: (data: CustomerRequest) =>
    apiClient.post<Customer>('/customers', data).then(r => r.data),
  update: (id: number, data: CustomerRequest) =>
    apiClient.put<Customer>(`/customers/${id}`, data).then(r => r.data),
  delete: (id: number) =>
    apiClient.delete(`/customers/${id}`),
};
