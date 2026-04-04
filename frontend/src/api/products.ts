import apiClient from './client';
import type { PagedResponse } from '../types';

export interface Product {
  id: number;
  sku: string;
  name: string;
  description: string | null;
  categoryId: number | null;
  categoryName: string | null;
  unitOfMeasure: string;
  minStockLevel: number;
  maxStockLevel: number | null;
  reorderPoint: number;
  costPrice: number;
  sellingPrice: number;
  barcode: string | null;
  imageUrl: string | null;
  isActive: boolean;
  totalStock: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface ProductRequest {
  name: string;
  description?: string;
  categoryId?: number | null;
  unitOfMeasure?: string;
  minStockLevel?: number;
  maxStockLevel?: number | null;
  reorderPoint?: number;
  costPrice?: number;
  sellingPrice?: number;
  barcode?: string;
  imageUrl?: string;
  isActive?: boolean;
}

export const productsApi = {
  getAll: (params: { page?: number; size?: number; search?: string; categoryId?: number; isActive?: boolean } = {}) =>
    apiClient.get<PagedResponse<Product>>('/products', { params }).then(r => r.data),

  getById: (id: number) =>
    apiClient.get<Product>(`/products/${id}`).then(r => r.data),

  create: (data: ProductRequest) =>
    apiClient.post<Product>('/products', data).then(r => r.data),

  update: (id: number, data: ProductRequest) =>
    apiClient.put<Product>(`/products/${id}`, data).then(r => r.data),

  delete: (id: number) =>
    apiClient.delete(`/products/${id}`),

  uploadImage: (id: number, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiClient.post<{ imageUrl: string }>(`/products/${id}/image`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(r => r.data);
  },

  deleteImage: (id: number) =>
    apiClient.delete(`/products/${id}/image`),
};
