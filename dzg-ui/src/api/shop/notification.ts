import request from '@/utils/request';

export const listNotification = () => {
  return request({
    url: '/shop/notification/list',
    method: 'get'
  });
};
