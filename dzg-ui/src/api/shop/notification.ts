import request from '@/utils/request';

export const listNotification = () => {
  return request({
    url: '/shop/notification/list',
    method: 'get'
  });
};

export const updateNotificationStatus = (noticeId: string | number, status: '0' | '1') => {
  return request({
    url: `/shop/notification/${noticeId}/status/${status}`,
    method: 'put'
  });
};

export const readAllNotification = () => {
  return request({
    url: '/shop/notification/read-all',
    method: 'put'
  });
};
