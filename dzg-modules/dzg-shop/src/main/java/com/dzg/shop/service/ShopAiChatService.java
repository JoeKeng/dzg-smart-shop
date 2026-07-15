package com.dzg.shop.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dzg.common.core.exception.ServiceException;
import com.dzg.common.core.utils.StringUtils;
import com.dzg.shop.config.ShopAiProperties;
import com.dzg.shop.domain.ShopAiConversation;
import com.dzg.shop.domain.ShopAiMessage;
import com.dzg.shop.domain.bo.ShopAiChatBo;
import com.dzg.shop.domain.vo.ShopAiAnalysisVo;
import com.dzg.shop.domain.vo.ShopAiChatVo;
import com.dzg.shop.mapper.ShopAiConversationMapper;
import com.dzg.shop.mapper.ShopAiMessageMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ShopAiChatService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ShopAiConversationMapper conversationMapper;
    private final ShopAiMessageMapper messageMapper;
    private final ShopAiAnalysisService aiAnalysisService;
    private final DeepSeekAnalysisClient deepSeekAnalysisClient;
    private final ShopAiProperties aiProperties;
    private final ObjectMapper objectMapper;

    public List<ShopAiConversation> listConversations() {
        return conversationMapper.selectList(Wrappers.<ShopAiConversation>lambdaQuery()
            .orderByDesc(ShopAiConversation::getUpdateTime)
            .orderByDesc(ShopAiConversation::getCreateTime));
    }

    public ShopAiConversation createConversation() {
        ShopAiConversation conversation = new ShopAiConversation();
        conversation.setTitle("新的 AI 对话");
        conversation.setStatus(ShopConstants.NORMAL);
        conversationMapper.insert(conversation);
        return conversation;
    }

    public List<ShopAiMessage> listMessages(Long conversationId) {
        requireConversation(conversationId);
        return messageMapper.selectList(Wrappers.<ShopAiMessage>lambdaQuery()
            .eq(ShopAiMessage::getConversationId, conversationId)
            .orderByAsc(ShopAiMessage::getCreateTime)
            .orderByAsc(ShopAiMessage::getMessageId));
    }

    public List<String> suggestions() {
        return List.of(
            "今天最应该先处理哪件事？",
            "哪些商品需要优先补货？",
            "赊账客户应该怎么跟进？",
            "帮我总结今天的经营情况",
            "下一次采购应该买什么？"
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public ShopAiChatVo chat(ShopAiChatBo bo) {
        if (bo == null || !StringUtils.isNotBlank(bo.getContent())) {
            throw new ServiceException("请输入要询问 AI 的内容");
        }

        ShopAiConversation conversation = bo.getConversationId() == null ? createConversation() : requireConversation(bo.getConversationId());
        if ("新的 AI 对话".equals(conversation.getTitle())) {
            conversation.setTitle(titleFrom(bo.getContent()));
            conversationMapper.updateById(conversation);
        }

        ShopAiMessage userMessage = message(conversation.getConversationId(), "user", bo.getContent());
        messageMapper.insert(userMessage);

        ShopAiChatVo vo = new ShopAiChatVo();
        vo.setConversation(conversation);
        vo.setSuggestions(suggestions());
        try {
            AiReplyResult aiReply = aiReply(conversation, bo.getContent());
            messageMapper.insert(aiReply.message());
            vo.setReply(aiReply.message());
            vo.setGeneratedByAi(true);
            if (!CollectionUtils.isEmpty(aiReply.suggestions())) {
                vo.setSuggestions(aiReply.suggestions());
            }
        } catch (Exception e) {
            ShopAiMessage fallback = message(conversation.getConversationId(), "assistant", fallbackReply(bo.getContent(), e.getMessage()));
            messageMapper.insert(fallback);
            vo.setReply(fallback);
            vo.setGeneratedByAi(false);
            vo.setFallbackReason("DeepSeek 调用失败，已使用本地回复：" + e.getMessage());
        }
        conversationMapper.updateById(conversation);
        return vo;
    }

    private AiReplyResult aiReply(ShopAiConversation conversation, String question) throws JsonProcessingException {
        if (!Boolean.TRUE.equals(aiProperties.getEnabled())) {
            throw new IllegalStateException("AI 分析未启用");
        }
        if (!StringUtils.isNotBlank(aiProperties.getApiKey())) {
            throw new IllegalStateException("DeepSeek API Key 未配置");
        }
        String content = deepSeekAnalysisClient.completeJson(buildPrompt(conversation.getConversationId(), question));
        Map<String, Object> data = objectMapper.readValue(extractJson(content), MAP_TYPE);
        String reply = stringValue(data.get("reply"));
        if (!StringUtils.isNotBlank(reply)) {
            throw new IllegalStateException("DeepSeek 回复为空");
        }
        return new AiReplyResult(message(conversation.getConversationId(), "assistant", reply), stringListValue(data.get("suggestions")));
    }

    private String buildPrompt(Long conversationId, String question) {
        ShopAiAnalysisVo analysis = aiAnalysisService.analysis();
        List<ShopAiMessage> history = messageMapper.selectList(Wrappers.<ShopAiMessage>lambdaQuery()
            .eq(ShopAiMessage::getConversationId, conversationId)
            .orderByDesc(ShopAiMessage::getCreateTime)
            .last("limit 8"));
        List<String> historyLines = new ArrayList<>();
        for (int i = history.size() - 1; i >= 0; i--) {
            ShopAiMessage item = history.get(i);
            historyLines.add(("user".equals(item.getRole()) ? "老板" : "AI") + "：" + item.getContent());
        }
        return """
            你是“店掌柜”的 AI 经营助手，服务对象是乡镇小店和中老年经营者。
            请围绕收银、库存、采购、赊账、经营报表回答，不要编造系统里没有的数据。
            只返回 JSON 对象，不要 Markdown。JSON 字段：
            - reply：给老板看的回答，尽量具体，可以分点，但不要太长。
            - suggestions：3 个后续可问的问题。

            当前经营分析摘要：
            风险等级：%s
            摘要：%s

            最近对话：
            %s

            老板这次的问题：
            %s
            """.formatted(
            analysis.getRiskLevel(),
            analysis.getSummary(),
            CollectionUtils.isEmpty(historyLines) ? "暂无" : String.join("\n", historyLines),
            question
        );
    }

    private ShopAiConversation requireConversation(Long conversationId) {
        ShopAiConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new ServiceException("AI 对话不存在");
        }
        return conversation;
    }

    private ShopAiMessage message(Long conversationId, String role, String content) {
        ShopAiMessage message = new ShopAiMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        return message;
    }

    private String fallbackReply(String question, String reason) {
        ShopAiAnalysisVo analysis = aiAnalysisService.analysis();
        return "我现在没有连上 DeepSeek，先按本地经营数据给你建议："
            + analysis.getSummary()
            + " 你刚才问的是“" + question + "”，建议先看库存预警和未还赊账，处理完再刷新经营报表。"
            + (StringUtils.isNotBlank(reason) ? "（原因：" + reason + "）" : "");
    }

    private String titleFrom(String content) {
        String title = content.trim().replaceAll("\\s+", " ");
        return title.length() > 18 ? title.substring(0, 18) + "..." : title;
    }

    private String extractJson(String content) {
        int begin = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (begin < 0 || end <= begin) {
            throw new IllegalStateException("DeepSeek 未返回 JSON 对象");
        }
        return content.substring(begin, end + 1);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private List<String> stringListValue(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
            .map(this::stringValue)
            .filter(StringUtils::isNotBlank)
            .limit(5)
            .toList();
    }

    private record AiReplyResult(ShopAiMessage message, List<String> suggestions) {
    }
}
