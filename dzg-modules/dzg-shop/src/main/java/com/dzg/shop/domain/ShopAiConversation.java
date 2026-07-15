package com.dzg.shop.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dzg.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dzg_ai_conversation")
public class ShopAiConversation extends TenantEntity {

    @TableId(value = "conversation_id")
    private Long conversationId;

    private String title;

    private String status;

    @TableLogic
    private String delFlag;
}
