import request from '@/utils/request';
import { ShopCreditRecord } from './types';

export const listCredit = (query?: ShopCreditRecord & Record<string, any>) => {
  return request({
    url: '/shop/credit/list',
    method: 'get',
    params: query
  });
};

export const repayCredit = (data: { creditId?: string | number; repaymentAmount?: number; payType?: string; remark?: string }) => {
  return request({
    url: '/shop/credit/repay',
    method: 'post',
    data
  });
};
