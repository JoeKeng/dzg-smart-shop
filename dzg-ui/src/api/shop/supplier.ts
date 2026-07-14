import request from '@/utils/request';
import { ShopSupplier } from './types';

export const listSupplier = (query?: ShopSupplier & Record<string, any>) => {
  return request({
    url: '/shop/supplier/list',
    method: 'get',
    params: query
  });
};

export const supplierOptions = (query?: ShopSupplier) => {
  return request({
    url: '/shop/supplier/options',
    method: 'get',
    params: query
  });
};

export const saveSupplier = (data: ShopSupplier) => {
  return request({
    url: '/shop/supplier',
    method: data.supplierId ? 'put' : 'post',
    data
  });
};

export const deleteSupplier = (supplierIds: string | number | Array<string | number>) => {
  return request({
    url: `/shop/supplier/${supplierIds}`,
    method: 'delete'
  });
};
