import request from '@/utils/request';

export const listAiConversations = () => {
  return request({
    url: '/shop/ai/conversation/list',
    method: 'get'
  });
};

export const createAiConversation = () => {
  return request({
    url: '/shop/ai/conversation',
    method: 'post'
  });
};

export const listAiMessages = (conversationId: string | number) => {
  return request({
    url: `/shop/ai/conversation/${conversationId}/messages`,
    method: 'get'
  });
};

export const getAiSuggestions = () => {
  return request({
    url: '/shop/ai/suggestions',
    method: 'get'
  });
};

export const sendAiChat = (data: { conversationId?: string | number; content: string }) => {
  return request({
    url: '/shop/ai/chat',
    method: 'post',
    data
  });
};
