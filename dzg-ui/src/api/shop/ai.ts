import request from '@/utils/request';

export const getShopAiAnalysis = () => {
  return request({
    url: '/shop/ai/analysis',
    method: 'get'
  });
};

export const getShopAiBusinessAnalysis = () => {
  return request({
    url: '/shop/ai/business-analysis',
    method: 'get'
  });
};

export const askShopAiBusinessAnalysis = (data: { content: string }) => {
  return request({
    url: '/shop/ai/business-analysis/chat',
    method: 'post',
    data
  });
};
