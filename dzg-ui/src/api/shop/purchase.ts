import request from '@/utils/request';
import { ShopPurchaseForm, ShopPurchaseOrder } from './types';

export const createPurchase = (data: ShopPurchaseForm) => {
  return request({
    url: '/shop/purchase',
    method: 'post',
    data
  });
};

export const listPurchase = (query?: ShopPurchaseOrder & Record<string, any>) => {
  return request({
    url: '/shop/purchase/list',
    method: 'get',
    params: query
  });
};
