import request from '@/utils/request';

export const getShopReport = () => {
  return request({
    url: '/shop/report/summary',
    method: 'get'
  });
};
