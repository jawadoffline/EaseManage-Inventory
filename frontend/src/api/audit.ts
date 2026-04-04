import apiClient from './client';
import type { PagedResponse } from '../types';

export interface AuditLog {
  id: number;
  userId: number | null;
  username: string;
  entityType: string;
  entityId: number;
  action: string;
  oldValues: string | null;
  newValues: string | null;
  ipAddress: string | null;
  createdAt: string;
}

export const auditApi = {
  getAll: (params: { page?: number; size?: number; entityType?: string; action?: string; search?: string } = {}) =>
    apiClient.get<PagedResponse<AuditLog>>('/audit', { params }).then(r => r.data),
};
