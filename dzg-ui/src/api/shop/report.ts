import request from '@/utils/request';

export const getShopReport = (range = 'today') => {
  return request({
    url: '/shop/report/summary',
    method: 'get',
    params: { range }
  });
};
