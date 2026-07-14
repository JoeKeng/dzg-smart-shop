import request from '@/utils/request';
import { ShopStock } from './types';

export const listStock = (query?: ShopStock & Record<string, any>) => {
  return request({
    url: '/shop/stock/list',
    method: 'get',
    params: query
  });
};

export const adjustStock = (data: { productId?: string | number; quantity?: number; changeType?: string; remark?: string }) => {
  return request({
    url: '/shop/stock/adjust',
    method: 'post',
    data
  });
};

export const listStockLog = (query?: Record<string, any>) => {
  return request({
    url: '/shop/stock/log/list',
    method: 'get',
    params: query
  });
};
