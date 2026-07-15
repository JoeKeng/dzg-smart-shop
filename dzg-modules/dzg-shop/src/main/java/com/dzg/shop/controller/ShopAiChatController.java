package com.dzg.shop.controller;

import com.dzg.common.core.domain.R;
import com.dzg.shop.domain.ShopAiConversation;
import com.dzg.shop.domain.ShopAiMessage;
import com.dzg.shop.domain.bo.ShopAiChatBo;
import com.dzg.shop.domain.vo.ShopAiChatVo;
import com.dzg.shop.service.ShopAiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ShopAiChatController {

    private final ShopAiChatService aiChatService;

    @GetMapping("/ai/conversation/list")
    public R<List<ShopAiConversation>> conversations() {
        return R.ok(aiChatService.listConversations());
    }

    @PostMapping("/ai/conversation")
    public R<ShopAiConversation> createConversation() {
        return R.ok(aiChatService.createConversation());
    }

    @GetMapping("/ai/conversation/{conversationId}/messages")
    public R<List<ShopAiMessage>> messages(@PathVariable Long conversationId) {
        return R.ok(aiChatService.listMessages(conversationId));
    }

    @GetMapping("/ai/suggestions")
    public R<List<String>> suggestions() {
        return R.ok(aiChatService.suggestions());
    }

    @PostMapping("/ai/chat")
    public R<ShopAiChatVo> chat(@RequestBody ShopAiChatBo bo) {
        return R.ok(aiChatService.chat(bo));
    }
}
