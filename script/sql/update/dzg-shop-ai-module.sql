create database if not exists dzg_shop default character set utf8mb4 collate utf8mb4_general_ci;
create database if not exists dzg_cloud default character set utf8mb4 collate utf8mb4_general_ci;
use dzg_shop;

create table if not exists dzg_ai_conversation (
    conversation_id bigint(20)   not null comment '对话ID',
    tenant_id       varchar(20)  default '000000' comment '租户编号',
    title           varchar(120) not null comment '对话标题',
    status          char(1)      default '0' comment '状态',
    del_flag        char(1)      default '0' comment '删除标志',
    create_dept     bigint(20)   default null comment '创建部门',
    create_by       bigint(20)   default null comment '创建者',
    create_time     datetime     default null comment '创建时间',
    update_by       bigint(20)   default null comment '更新者',
    update_time     datetime     default null comment '更新时间',
    remark          varchar(500) default null comment '备注',
    primary key (conversation_id),
    key idx_dzg_ai_conversation_time (update_time, create_time)
) engine=innodb comment='店掌柜AI对话';

create table if not exists dzg_ai_message (
    message_id      bigint(20)  not null comment '消息ID',
    tenant_id       varchar(20) default '000000' comment '租户编号',
    conversation_id bigint(20)  not null comment '对话ID',
    role            varchar(20) not null comment '消息角色',
    content         text        not null comment '消息内容',
    del_flag        char(1)     default '0' comment '删除标志',
    create_dept     bigint(20)  default null comment '创建部门',
    create_by       bigint(20)  default null comment '创建者',
    create_time     datetime    default null comment '创建时间',
    update_by       bigint(20)  default null comment '更新者',
    update_time     datetime    default null comment '更新时间',
    primary key (message_id),
    key idx_dzg_ai_message_conversation (conversation_id, create_time)
) engine=innodb comment='店掌柜AI消息';

use dzg_cloud;

delete from sys_menu where menu_id in (20100, 20101, 20102);

insert into sys_menu values ('20100', 'AI助手', '0', '2', 'ai', null, '', 1, 0, 'M', '0', '0', '', 'education', 103, 1, sysdate(), null, null, '店掌柜AI助手一级菜单');
insert into sys_menu values ('20101', 'AI对话', '20100', '1', 'chat', 'shop/ai/index', '', 1, 0, 'C', '0', '0', 'shop:ai:chat', 'message', 103, 1, sysdate(), null, null, 'AI聊天与经营问答');
insert into sys_menu values ('20102', 'AI经营分析', '20100', '2', 'business-analysis', 'shop/ai/analysis/index', '', 1, 0, 'C', '0', '0', 'shop:ai:analysis', 'chart', 103, 1, sysdate(), null, null, 'AI详细经营分析和长期规划');
