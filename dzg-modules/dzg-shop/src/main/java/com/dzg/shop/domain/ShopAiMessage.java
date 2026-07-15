package com.dzg.shop.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dzg.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dzg_ai_message")
public class ShopAiMessage extends TenantEntity {

    @TableId(value = "message_id")
    private Long messageId;

    private Long conversationId;

    private String role;

    private String content;

    @TableLogic
    private String delFlag;
}
