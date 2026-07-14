import request from '@/utils/request';

export const getShopDashboard = () => {
  return request({
    url: '/shop/dashboard',
    method: 'get'
  });
};
