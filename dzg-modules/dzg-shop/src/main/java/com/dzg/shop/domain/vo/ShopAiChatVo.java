package com.dzg.shop.domain.vo;

import com.dzg.shop.domain.ShopAiConversation;
import com.dzg.shop.domain.ShopAiMessage;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ShopAiChatVo {

    private ShopAiConversation conversation;

    private ShopAiMessage reply;

    private Boolean generatedByAi = false;

    private String fallbackReason;

    private List<String> suggestions = new ArrayList<>();
}
