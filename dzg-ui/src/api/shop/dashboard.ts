import request from '@/utils/request';
import { ShopPaymentSummary } from './types';

export const getShopDashboard = () => {
  return request({
    url: '/shop/dashboard',
    method: 'get'
  });
};

export const getShopPaymentSummary = (range = 'today') => {
  return request<ShopPaymentSummary>({
    url: '/shop/dashboard/payment-summary',
    method: 'get',
    params: { range }
  });
};
