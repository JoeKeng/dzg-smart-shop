import request from '@/utils/request';

export const getShopAiAnalysis = () => {
  return request({
    url: '/shop/ai/analysis',
    method: 'get'
  });
};
