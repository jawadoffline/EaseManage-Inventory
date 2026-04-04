import apiClient from './client';

export interface ProductValuation {
  productId: number;
  productName: string;
  sku: string;
  categoryName: string;
  quantity: number;
  costPrice: number;
  sellingPrice: number;
  totalCostValue: number;
  totalRetailValue: number;
}

export interface InventoryValuationReport {
  totalCostValue: number;
  totalRetailValue: number;
  totalProducts: number;
  totalUnits: number;
  items: ProductValuation[];
}

export interface CategoryStock {
  categoryName: string;
  productCount: number;
  totalQuantity: number;
}

export interface StockSummaryReport {
  totalProducts: number;
  inStockCount: number;
  lowStockCount: number;
  outOfStockCount: number;
  byCategory: CategoryStock[];
}

export interface OrderSummaryReport {
  totalPurchaseOrders: number;
  totalPurchaseValue: number;
  purchaseByStatus: Record<string, number>;
  totalSalesOrders: number;
  totalSalesValue: number;
  salesByStatus: Record<string, number>;
}

export const reportsApi = {
  getInventoryValuation: () =>
    apiClient.get<InventoryValuationReport>('/reports/inventory-valuation').then(r => r.data),

  getStockSummary: () =>
    apiClient.get<StockSummaryReport>('/reports/stock-summary').then(r => r.data),

  getOrderSummary: () =>
    apiClient.get<OrderSummaryReport>('/reports/order-summary').then(r => r.data),
};
