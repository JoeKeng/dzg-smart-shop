package com.dzg.shop.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dzg.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dzg_notification")
public class ShopNotification extends TenantEntity {

    @TableId(value = "notice_id")
    private Long noticeId;

    private String noticeType;

    private String noticeTitle;

    private String noticeContent;

    private String bizType;

    private Long bizId;

    private String status;

    @TableLogic
    private String delFlag;
}
