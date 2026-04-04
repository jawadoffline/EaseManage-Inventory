import apiClient from './client';
import type { PagedResponse } from '../types';

export interface Notification {
  id: number;
  title: string;
  message: string;
  type: string;
  isRead: boolean;
  createdAt: string;
}

export const notificationsApi = {
  getAll: (params: { page?: number; size?: number } = {}) =>
    apiClient.get<PagedResponse<Notification>>('/notifications', { params }).then(r => r.data),

  getUnreadCount: () =>
    apiClient.get<{ count: number }>('/notifications/unread-count').then(r => r.data),

  markAsRead: (id: number) =>
    apiClient.patch(`/notifications/${id}/read`),

  markAllAsRead: () =>
    apiClient.post('/notifications/mark-all-read'),
};
