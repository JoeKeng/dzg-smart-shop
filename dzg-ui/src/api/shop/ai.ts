import request from '@/utils/request';

export const getShopAiAnalysis = (force = false) => {
  return request({
    url: '/shop/ai/analysis',
    method: 'get',
    params: { force }
  });
};

export const getShopAiBusinessAnalysis = (force = false) => {
  return request({
    url: '/shop/ai/business-analysis',
    method: 'get',
    params: { force }
  });
};

export const askShopAiBusinessAnalysis = (data: { content: string }) => {
  return request({
    url: '/shop/ai/business-analysis/chat',
    method: 'post',
    data
  });
};
