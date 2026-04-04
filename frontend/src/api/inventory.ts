import apiClient from './client';
import type { PagedResponse } from '../types';

export interface InventoryItem {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  warehouseId: number;
  warehouseName: string;
  warehouseCode: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  minStockLevel: number;
  reorderPoint: number;
  lowStock: boolean;
  lastCountedAt: string | null;
}

export interface InventoryAdjustRequest {
  productId: number;
  warehouseId: number;
  quantity: number;
  reason?: string;
}

export interface DashboardStats {
  totalProducts: number;
  lowStockAlerts: number;
  totalWarehouses: number;
  totalCategories: number;
}

export interface ChartData {
  stockByCategory: { name: string; stock: number }[];
  orderStatus: { name: string; value: number }[];
}

export const inventoryApi = {
  getAll: (params: { page?: number; size?: number; warehouseId?: number; search?: string } = {}) =>
    apiClient.get<PagedResponse<InventoryItem>>('/inventory', { params }).then(r => r.data),

  getLowStock: () =>
    apiClient.get<InventoryItem[]>('/inventory/low-stock').then(r => r.data),

  adjust: (data: InventoryAdjustRequest) =>
    apiClient.post<InventoryItem>('/inventory/adjust', data).then(r => r.data),

  getDashboardStats: () =>
    apiClient.get<DashboardStats>('/dashboard/stats').then(r => r.data),

  getChartData: () =>
    apiClient.get<ChartData>('/dashboard/charts').then(r => r.data),
};
