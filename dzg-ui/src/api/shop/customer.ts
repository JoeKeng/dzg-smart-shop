import request from '@/utils/request';
import { ShopCustomer } from './types';

export const listCustomer = (query?: ShopCustomer & Record<string, any>) => {
  return request({
    url: '/shop/customer/list',
    method: 'get',
    params: query
  });
};

export const customerOptions = (query?: ShopCustomer) => {
  return request({
    url: '/shop/customer/options',
    method: 'get',
    params: query
  });
};

export const saveCustomer = (data: ShopCustomer) => {
  return request({
    url: '/shop/customer',
    method: data.customerId ? 'put' : 'post',
    data
  });
};

export const deleteCustomer = (customerIds: string | number | Array<string | number>) => {
  return request({
    url: `/shop/customer/${customerIds}`,
    method: 'delete'
  });
};
