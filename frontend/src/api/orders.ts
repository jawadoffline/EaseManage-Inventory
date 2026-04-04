import apiClient from './client';
import type { PagedResponse } from '../types';

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  quantity: number;
  unitPrice: number;
  receivedQuantity: number;
}

export interface PurchaseOrder {
  id: number;
  orderNumber: string;
  supplierId: number;
  supplierName: string;
  warehouseId: number;
  warehouseName: string;
  status: string;
  totalAmount: number;
  expectedDelivery: string | null;
  notes: string | null;
  createdByName: string | null;
  items: OrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface PurchaseOrderRequest {
  supplierId: number;
  warehouseId: number;
  expectedDelivery?: string;
  notes?: string;
  items: { productId: number; quantity: number; unitPrice: number }[];
}

export interface SalesOrder {
  id: number;
  orderNumber: string;
  customerName: string;
  warehouseId: number;
  warehouseName: string;
  status: string;
  totalAmount: number;
  shippingAddress: string | null;
  createdByName: string | null;
  items: OrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface SalesOrderRequest {
  customerName: string;
  warehouseId: number;
  shippingAddress?: string;
  items: { productId: number; quantity: number; unitPrice: number }[];
}

export const purchaseOrdersApi = {
  getAll: (params: { page?: number; size?: number; status?: string; search?: string } = {}) =>
    apiClient.get<PagedResponse<PurchaseOrder>>('/purchase-orders', { params }).then(r => r.data),

  getById: (id: number) =>
    apiClient.get<PurchaseOrder>(`/purchase-orders/${id}`).then(r => r.data),

  create: (data: PurchaseOrderRequest) =>
    apiClient.post<PurchaseOrder>('/purchase-orders', data).then(r => r.data),

  updateStatus: (id: number, status: string) =>
    apiClient.patch<PurchaseOrder>(`/purchase-orders/${id}/status`, null, { params: { status } }).then(r => r.data),

  delete: (id: number) =>
    apiClient.delete(`/purchase-orders/${id}`),

  downloadPurchaseOrderPdf: (id: number) =>
    apiClient.get(`/purchase-orders/${id}/pdf`, { responseType: 'blob' }).then(r => {
      const url = window.URL.createObjectURL(new Blob([r.data]));
      const link = document.createElement('a');
      link.href = url;
      link.download = `PO-${id}.pdf`;
      link.click();
      window.URL.revokeObjectURL(url);
    }),
};

export const salesOrdersApi = {
  getAll: (params: { page?: number; size?: number; status?: string; search?: string } = {}) =>
    apiClient.get<PagedResponse<SalesOrder>>('/sales-orders', { params }).then(r => r.data),

  getById: (id: number) =>
    apiClient.get<SalesOrder>(`/sales-orders/${id}`).then(r => r.data),

  create: (data: SalesOrderRequest) =>
    apiClient.post<SalesOrder>('/sales-orders', data).then(r => r.data),

  updateStatus: (id: number, status: string) =>
    apiClient.patch<SalesOrder>(`/sales-orders/${id}/status`, null, { params: { status } }).then(r => r.data),

  delete: (id: number) =>
    apiClient.delete(`/sales-orders/${id}`),

  downloadSalesOrderPdf: (id: number) =>
    apiClient.get(`/sales-orders/${id}/pdf`, { responseType: 'blob' }).then(r => {
      const url = window.URL.createObjectURL(new Blob([r.data]));
      const link = document.createElement('a');
      link.href = url;
      link.download = `SO-${id}.pdf`;
      link.click();
      window.URL.revokeObjectURL(url);
    }),
};
