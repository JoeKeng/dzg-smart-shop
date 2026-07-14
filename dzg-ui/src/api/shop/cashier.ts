import request from '@/utils/request';
import { ShopOrderForm } from './types';

export const createCashierOrder = (data: ShopOrderForm) => {
  return request({
    url: '/shop/order/cashier',
    method: 'post',
    data
  });
};

export const listOrder = (query?: Record<string, any>) => {
  return request({
    url: '/shop/order/list',
    method: 'get',
    params: query
  });
};
