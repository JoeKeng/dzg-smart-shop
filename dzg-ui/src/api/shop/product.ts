import request from '@/utils/request';
import { ShopCategory, ShopProduct } from './types';

export const listCategory = (query?: ShopCategory & Record<string, any>) => {
  return request({
    url: '/shop/category/list',
    method: 'get',
    params: query
  });
};

export const categoryOptions = () => {
  return request({
    url: '/shop/category/options',
    method: 'get'
  });
};

export const saveCategory = (data: ShopCategory) => {
  return request({
    url: '/shop/category',
    method: data.categoryId ? 'put' : 'post',
    data
  });
};

export const deleteCategory = (categoryIds: string | number | Array<string | number>) => {
  return request({
    url: `/shop/category/${categoryIds}`,
    method: 'delete'
  });
};

export const listProduct = (query?: ShopProduct & Record<string, any>) => {
  return request({
    url: '/shop/product/list',
    method: 'get',
    params: query
  });
};

export const productOptions = (query?: ShopProduct) => {
  return request({
    url: '/shop/product/options',
    method: 'get',
    params: query
  });
};

export const saveProduct = (data: ShopProduct) => {
  return request({
    url: '/shop/product',
    method: data.productId ? 'put' : 'post',
    data
  });
};

export const deleteProduct = (productIds: string | number | Array<string | number>) => {
  return request({
    url: `/shop/product/${productIds}`,
    method: 'delete'
  });
};
