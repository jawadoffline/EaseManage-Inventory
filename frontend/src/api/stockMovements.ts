import apiClient from './client';
import type { PagedResponse } from '../types';

export interface StockMovement {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  fromWarehouseId: number | null;
  fromWarehouseName: string | null;
  toWarehouseId: number | null;
  toWarehouseName: string | null;
  quantity: number;
  movementType: string;
  referenceType: string | null;
  referenceId: number | null;
  reason: string | null;
  createdByName: string | null;
  createdAt: string;
}

export interface StockMovementRequest {
  productId: number;
  fromWarehouseId?: number;
  toWarehouseId?: number;
  quantity: number;
  movementType: string;
  reason?: string;
}

export const stockMovementsApi = {
  getAll: (params: { page?: number; size?: number; productId?: number; warehouseId?: number; movementType?: string } = {}) =>
    apiClient.get<PagedResponse<StockMovement>>('/stock-movements', { params }).then(r => r.data),

  create: (data: StockMovementRequest) =>
    apiClient.post<StockMovement>('/stock-movements', data).then(r => r.data),
};
