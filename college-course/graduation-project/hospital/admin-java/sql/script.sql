create table QRTZ_CALENDARS
(
    SCHED_NAME    varchar(120) not null,
    CALENDAR_NAME varchar(190) not null,
    CALENDAR      blob         not null,
    primary key (SCHED_NAME, CALENDAR_NAME)
);

create table QRTZ_FIRED_TRIGGERS
(
    SCHED_NAME        varchar(120) not null,
    ENTRY_ID          varchar(95)  not null,
    TRIGGER_NAME      varchar(190) not null,
    TRIGGER_GROUP     varchar(190) not null,
    INSTANCE_NAME     varchar(190) not null,
    FIRED_TIME        bigint       not null,
    SCHED_TIME        bigint       not null,
    PRIORITY          int          not null,
    STATE             varchar(16)  not null,
    JOB_NAME          varchar(190) null,
    JOB_GROUP         varchar(190) null,
    IS_NONCONCURRENT  varchar(1)   null,
    REQUESTS_RECOVERY varchar(1)   null,
    primary key (SCHED_NAME, ENTRY_ID)
);

create index IDX_QRTZ_FT_INST_JOB_REQ_RCVRY
    on QRTZ_FIRED_TRIGGERS (SCHED_NAME, INSTANCE_NAME, REQUESTS_RECOVERY);

create index IDX_QRTZ_FT_JG
    on QRTZ_FIRED_TRIGGERS (SCHED_NAME, JOB_GROUP);

create index IDX_QRTZ_FT_J_G
    on QRTZ_FIRED_TRIGGERS (SCHED_NAME, JOB_NAME, JOB_GROUP);

create index IDX_QRTZ_FT_TG
    on QRTZ_FIRED_TRIGGERS (SCHED_NAME, TRIGGER_GROUP);

create index IDX_QRTZ_FT_TRIG_INST_NAME
    on QRTZ_FIRED_TRIGGERS (SCHED_NAME, INSTANCE_NAME);

create index IDX_QRTZ_FT_T_G
    on QRTZ_FIRED_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);

create table QRTZ_JOB_DETAILS
(
    SCHED_NAME        varchar(120) not null,
    JOB_NAME          varchar(190) not null,
    JOB_GROUP         varchar(190) not null,
    DESCRIPTION       varchar(250) null,
    JOB_CLASS_NAME    varchar(250) not null,
    IS_DURABLE        varchar(1)   not null,
    IS_NONCONCURRENT  varchar(1)   not null,
    IS_UPDATE_DATA    varchar(1)   not null,
    REQUESTS_RECOVERY varchar(1)   not null,
    JOB_DATA          blob         null,
    primary key (SCHED_NAME, JOB_NAME, JOB_GROUP)
);

create index IDX_QRTZ_J_GRP
    on QRTZ_JOB_DETAILS (SCHED_NAME, JOB_GROUP);

create index IDX_QRTZ_J_REQ_RECOVERY
    on QRTZ_JOB_DETAILS (SCHED_NAME, REQUESTS_RECOVERY);

create table QRTZ_LOCKS
(
    SCHED_NAME varchar(120) not null,
    LOCK_NAME  varchar(40)  not null,
    primary key (SCHED_NAME, LOCK_NAME)
);

create table QRTZ_PAUSED_TRIGGER_GRPS
(
    SCHED_NAME    varchar(120) not null,
    TRIGGER_GROUP varchar(190) not null,
    primary key (SCHED_NAME, TRIGGER_GROUP)
);

create table QRTZ_SCHEDULER_STATE
(
    SCHED_NAME        varchar(120) not null,
    INSTANCE_NAME     varchar(190) not null,
    LAST_CHECKIN_TIME bigint       not null,
    CHECKIN_INTERVAL  bigint       not null,
    primary key (SCHED_NAME, INSTANCE_NAME)
);

create table QRTZ_TRIGGERS
(
    SCHED_NAME     varchar(120) not null,
    TRIGGER_NAME   varchar(190) not null,
    TRIGGER_GROUP  varchar(190) not null,
    JOB_NAME       varchar(190) not null,
    JOB_GROUP      varchar(190) not null,
    DESCRIPTION    varchar(250) null,
    NEXT_FIRE_TIME bigint       null,
    PREV_FIRE_TIME bigint       null,
    PRIORITY       int          null,
    TRIGGER_STATE  varchar(16)  not null,
    TRIGGER_TYPE   varchar(8)   not null,
    START_TIME     bigint       not null,
    END_TIME       bigint       null,
    CALENDAR_NAME  varchar(190) null,
    MISFIRE_INSTR  smallint     null,
    JOB_DATA       blob         null,
    primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    constraint qrtz_triggers_ibfk_1
        foreign key (SCHED_NAME, JOB_NAME, JOB_GROUP) references QRTZ_JOB_DETAILS (SCHED_NAME, JOB_NAME, JOB_GROUP)
);

create table QRTZ_BLOB_TRIGGERS
(
    SCHED_NAME    varchar(120) not null,
    TRIGGER_NAME  varchar(190) not null,
    TRIGGER_GROUP varchar(190) not null,
    BLOB_DATA     blob         null,
    primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    constraint qrtz_blob_triggers_ibfk_1
        foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) references QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

create index SCHED_NAME
    on QRTZ_BLOB_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);

create table QRTZ_CRON_TRIGGERS
(
    SCHED_NAME      varchar(120) not null,
    TRIGGER_NAME    varchar(190) not null,
    TRIGGER_GROUP   varchar(190) not null,
    CRON_EXPRESSION varchar(120) not null,
    TIME_ZONE_ID    varchar(80)  null,
    primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    constraint qrtz_cron_triggers_ibfk_1
        foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) references QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

create table QRTZ_SIMPLE_TRIGGERS
(
    SCHED_NAME      varchar(120) not null,
    TRIGGER_NAME    varchar(190) not null,
    TRIGGER_GROUP   varchar(190) not null,
    REPEAT_COUNT    bigint       not null,
    REPEAT_INTERVAL bigint       not null,
    TIMES_TRIGGERED bigint       not null,
    primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    constraint qrtz_simple_triggers_ibfk_1
        foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) references QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

create table QRTZ_SIMPROP_TRIGGERS
(
    SCHED_NAME    varchar(120)   not null,
    TRIGGER_NAME  varchar(190)   not null,
    TRIGGER_GROUP varchar(190)   not null,
    STR_PROP_1    varchar(512)   null,
    STR_PROP_2    varchar(512)   null,
    STR_PROP_3    varchar(512)   null,
    INT_PROP_1    int            null,
    INT_PROP_2    int            null,
    LONG_PROP_1   bigint         null,
    LONG_PROP_2   bigint         null,
    DEC_PROP_1    decimal(13, 4) null,
    DEC_PROP_2    decimal(13, 4) null,
    BOOL_PROP_1   varchar(1)     null,
    BOOL_PROP_2   varchar(1)     null,
    primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    constraint qrtz_simprop_triggers_ibfk_1
        foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) references QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

create index IDX_QRTZ_T_C
    on QRTZ_TRIGGERS (SCHED_NAME, CALENDAR_NAME);

create index IDX_QRTZ_T_G
    on QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_GROUP);

create index IDX_QRTZ_T_J
    on QRTZ_TRIGGERS (SCHED_NAME, JOB_NAME, JOB_GROUP);

create index IDX_QRTZ_T_JG
    on QRTZ_TRIGGERS (SCHED_NAME, JOB_GROUP);

create index IDX_QRTZ_T_NEXT_FIRE_TIME
    on QRTZ_TRIGGERS (SCHED_NAME, NEXT_FIRE_TIME);

create index IDX_QRTZ_T_NFT_MISFIRE
    on QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME);

create index IDX_QRTZ_T_NFT_ST
    on QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_STATE, NEXT_FIRE_TIME);

create index IDX_QRTZ_T_NFT_ST_MISFIRE
    on QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_STATE);

create index IDX_QRTZ_T_NFT_ST_MISFIRE_GRP
    on QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_GROUP, TRIGGER_STATE);

create index IDX_QRTZ_T_N_G_STATE
    on QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_GROUP, TRIGGER_STATE);

create index IDX_QRTZ_T_N_STATE
    on QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, TRIGGER_STATE);

create index IDX_QRTZ_T_STATE
    on QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_STATE);

create table accompany_staff
(
    id           bigint auto_increment
        primary key,
    create_time  datetime      null comment '创建时间',
    update_time  datetime      null comment '更新时间',
    name         varchar(255)  null comment '姓名',
    gender       int default 0 null comment '性别 0-未知 1-男 2-女',
    birthday     datetime      null comment '生日',
    phone        varchar(255)  null comment '电话',
    level        int default 0 null comment '级别 0-初级 1-中级 2-高级',
    status       int default 0 null comment '状态 0-正常 1-请假 2-其他',
    introduction varchar(255)  null comment '简介',
    remark       varchar(255)  null comment '备注',
    user_id      bigint        null comment '关联用户ID',
    constraint auto_idx_accompany_staff_phone
        unique (phone)
)
    comment '陪诊员信息';

create index auto_idx_accompany_staff_user_id
    on accompany_staff (user_id);

create table accompany_staff_review
(
    id          bigint auto_increment
        primary key,
    create_time datetime     null comment '创建时间',
    update_time datetime     null comment '更新时间',
    staff_id    bigint       null comment '陪诊员 ID',
    old_level   varchar(255) null comment '审核员原级别 0-初级 1-中级 2-高级',
    level       varchar(255) null comment '审核员新级别 0-初级 1-中级 2-高级',
    remark      varchar(255) null comment '审核意见'
)
    comment '陪诊员资质审核信息';

create index auto_idx_accompany_staff_review_staff_id
    on accompany_staff_review (staff_id);

create table base_sys_conf
(
    id          bigint auto_increment
        primary key,
    create_time datetime     null comment '创建时间',
    update_time datetime     null comment '更新时间',
    c_key       varchar(255) not null comment '配置键',
    c_value     text         not null comment '值',
    constraint auto_idx_base_sys_conf_c_key
        unique (c_key)
)
    comment '系统配置表';

create table base_sys_department
(
    id          bigint auto_increment
        primary key,
    create_time datetime      null comment '创建时间',
    update_time datetime      null comment '更新时间',
    name        varchar(255)  not null comment '部门名称',
    parent_id   bigint        null comment '上级部门ID',
    order_num   int default 0 null comment '排序'
)
    comment '系统部门';

create table base_sys_log
(
    id          bigint auto_increment
        primary key,
    create_time datetime      null comment '创建时间',
    update_time datetime      null comment '更新时间',
    user_id     bigint        null comment '用户ID',
    action      varchar(1000) null comment '行为',
    ip          varchar(50)   null comment 'IP',
    params      json          null comment '参数'
)
    comment '系统日志表';

create index auto_idx_base_sys_log_user_id
    on base_sys_log (user_id);

create table base_sys_menu
(
    id          bigint auto_increment
        primary key,
    create_time datetime         null comment '创建时间',
    update_time datetime         null comment '更新时间',
    parent_id   bigint           null comment '父菜单ID',
    name        varchar(255)     null comment '菜单名称',
    perms       text             null comment '权限',
    type        int default 0    null comment '类型 0：目录 1：菜单 2：按钮',
    icon        varchar(255)     null comment '图标',
    order_num   int default 0    null comment '排序',
    router      varchar(255)     null comment '菜单地址',
    view_path   varchar(255)     null comment '视图地址',
    keep_alive  bit default b'1' null comment '路由缓存',
    is_show     bit default b'1' null comment '是否显示'
)
    comment '系统菜单表';

create index auto_idx_base_sys_menu_parent_id
    on base_sys_menu (parent_id);

create table base_sys_param
(
    id          bigint auto_increment
        primary key,
    create_time datetime      null comment '创建时间',
    update_time datetime      null comment '更新时间',
    key_name    varchar(255)  not null comment '键',
    name        varchar(255)  null comment '名称',
    data        text          null comment '数据',
    data_type   int default 0 null comment '数据类型 0:字符串 1:数组 2:键值对',
    remark      varchar(255)  null comment '备注'
)
    comment '系统参数配置';

create index auto_idx_base_sys_param_key_name
    on base_sys_param (key_name);

create table base_sys_role
(
    id                 bigint auto_increment
        primary key,
    create_time        datetime      null comment '创建时间',
    update_time        datetime      null comment '更新时间',
    user_id            bigint        not null comment '用户ID',
    name               varchar(255)  not null comment '名称',
    label              varchar(255)  not null comment '角色标签',
    remark             varchar(255)  null comment '备注',
    relevance          int default 1 null comment '数据权限是否关联上下级',
    menu_id_list       json          null comment '菜单权限',
    department_id_list json          null comment '部门权限',
    constraint auto_idx_base_sys_role_label
        unique (label)
)
    comment '系统角色表';

create index auto_idx_base_sys_role_user_id
    on base_sys_role (user_id);

create table base_sys_role_department
(
    id            bigint auto_increment
        primary key,
    create_time   datetime null comment '创建时间',
    update_time   datetime null comment '更新时间',
    role_id       bigint   null comment '角色ID',
    department_id bigint   null comment '部门ID'
)
    comment '系统角色部门';

create table base_sys_role_menu
(
    id          bigint auto_increment
        primary key,
    create_time datetime null comment '创建时间',
    update_time datetime null comment '更新时间',
    menu_id     bigint   null comment '菜单',
    role_id     bigint   null comment '角色ID'
)
    comment '系统角色菜单表';

create table base_sys_user
(
    id            bigint auto_increment
        primary key,
    create_time   datetime      null comment '创建时间',
    update_time   datetime      null comment '更新时间',
    tenant_id     bigint        null comment '租户id',
    department_id bigint        null comment '部门ID',
    name          varchar(255)  null comment '姓名',
    username      varchar(100)  not null comment '用户名',
    password      varchar(255)  not null comment '密码',
    password_v    int default 1 null comment '密码版本',
    nick_name     varchar(255)  not null comment '昵称',
    head_img      varchar(255)  null comment '头像',
    phone         varchar(255)  null comment '手机号',
    email         varchar(255)  null comment '邮箱',
    remark        varchar(255)  null comment '备注',
    status        int default 1 null comment '状态 0:禁用 1：启用',
    socket_id     varchar(255)  null comment 'socketId',
    constraint auto_idx_base_sys_user_username
        unique (username)
)
    comment '系统用户表';

create index auto_idx_base_sys_user_department_id
    on base_sys_user (department_id);

create index auto_idx_base_sys_user_tenant_id
    on base_sys_user (tenant_id);

create table base_sys_user_role
(
    id          bigint auto_increment
        primary key,
    create_time datetime null comment '创建时间',
    update_time datetime null comment '更新时间',
    user_id     bigint   null comment '用户ID',
    role_id     bigint   null comment '角色ID'
)
    comment '系统用户角色表';

create index auto_idx_base_sys_user_role_role_id
    on base_sys_user_role (role_id);

create index auto_idx_base_sys_user_role_user_id
    on base_sys_user_role (user_id);

create table dict_info
(
    id          bigint auto_increment
        primary key,
    create_time datetime                       null comment '创建时间',
    update_time datetime                       null comment '更新时间',
    type_id     bigint                         not null comment '类型ID',
    parent_id   bigint                         null comment '父ID',
    name        varchar(255)                   not null comment '名称',
    value       varchar(255)                   null comment '值',
    type        varchar(255) default 'primary' null comment '字典信息样式（primary、success、info、warning、danger）',
    order_num   int          default 0         null comment '排序',
    remark      varchar(255)                   null comment '备注'
)
    comment '字典信息';

create table dict_type
(
    id          bigint auto_increment
        primary key,
    create_time datetime     null comment '创建时间',
    update_time datetime     null comment '更新时间',
    name        varchar(255) not null comment '名称',
    `key`       varchar(255) not null comment '标识',
    constraint auto_idx_dict_type_key
        unique (`key`)
)
    comment '字典类型';

create table hospital_department
(
    id             bigint auto_increment
        primary key,
    create_time    datetime      null comment '创建时间',
    update_time    datetime      null comment '更新时间',
    name           varchar(255)  null comment '名称',
    code           varchar(255)  null comment '编码',
    type           int default 0 null comment '类型 0-临床 1-医技 2-辅助',
    head_doctor_id bigint        null comment '负责人ID',
    status         int default 1 null comment '状态 0-禁用 1-启用',
    hospital_id    bigint        null comment '医院ID',
    constraint auto_idx_hospital_department_code
        unique (code)
)
    comment '科室信息';

create table hospital_doctor
(
    id            bigint auto_increment
        primary key,
    create_time   datetime      null comment '创建时间',
    update_time   datetime      null comment '更新时间',
    name          varchar(255)  null comment '姓名',
    job_code      varchar(255)  null comment '工号',
    title         varchar(255)  null comment '职称',
    hospital_id   bigint        null comment '医院ID',
    department_id bigint        null comment '科室ID（关联科室）',
    specialty     varchar(255)  null comment '专长',
    status        int default 1 null comment '状态 0-禁用 1-启用',
    constraint auto_idx_hospital_doctor_job_code
        unique (job_code)
)
    comment '医生信息';

create index auto_idx_hospital_doctor_department_id
    on hospital_doctor (department_id);

create index auto_idx_hospital_doctor_hospital_id
    on hospital_doctor (hospital_id);

create table hospital_info
(
    id          bigint auto_increment
        primary key,
    create_time datetime      null comment '创建时间',
    update_time datetime      null comment '更新时间',
    name        varchar(255)  null comment '名称',
    code        varchar(255)  null comment '编码',
    address     varchar(255)  null comment '地址',
    phone       varchar(255)  null comment '联系电话',
    status      int default 1 null comment '状态 0-禁用 1-启用',
    constraint auto_idx_hospital_info_code
        unique (code)
)
    comment '医院信息';

create table hospital_schedule
(
    id              bigint auto_increment
        primary key,
    create_time     datetime      null comment '创建时间',
    update_time     datetime      null comment '更新时间',
    doctor_id       bigint        null comment '医生ID（关联医生）',
    department_id   bigint        null comment '科室ID（关联科室）',
    schedule_date   varchar(255)  null comment '排班日期',
    time_slot       varchar(255)  null comment '时段（示例：08:00-12:00）',
    total_capacity  int           null comment '号源总数',
    booked_capacity int           null comment '已约数量',
    status          int default 1 null comment '状态 0-停诊 1-正常',
    constraint auto_idx_hospital_schedule_schedule_date
        unique (schedule_date),
    constraint auto_idx_hospital_schedule_time_slot
        unique (time_slot)
)
    comment '排班信息';

create index auto_idx_hospital_schedule_department_id
    on hospital_schedule (department_id);

create index auto_idx_hospital_schedule_doctor_id
    on hospital_schedule (doctor_id);

create table leaf_alloc
(
    id          bigint auto_increment
        primary key,
    create_time datetime           null comment '创建时间',
    update_time datetime           null comment '更新时间',
    `key`       varchar(20)        not null comment '业务key ，比如orderId',
    max_id      bigint default 1   not null comment '当前最大id',
    step        int    default 500 not null comment '步长',
    description varchar(255)       null comment '描述',
    constraint auto_idx_uk_key
        unique (`key`)
)
    comment '唯一id分配';

create table meal_category
(
    id          bigint auto_increment
        primary key,
    create_time datetime      null comment '创建时间',
    update_time datetime      null comment '更新时间',
    name        varchar(255)  null comment '名称',
    status      int default 1 null comment '状态 0-禁用 1-启用',
    sort        int           null comment '排序',
    icon        varchar(255)  null comment '图标',
    constraint auto_idx_uni_name
        unique (name)
)
    comment '套餐分类表';

create table meal_info
(
    id            bigint auto_increment
        primary key,
    create_time   datetime      null comment '创建时间',
    update_time   datetime      null comment '更新时间',
    name          varchar(255)  null comment '名称',
    price         double(6, 2)  null comment '价格',
    duration      int           null comment '时长',
    status        int default 1 null comment '状态 0-禁用 1-启用',
    category_id   bigint        null comment '分类ID',
    intro         varchar(255)  null comment '简介',
    cover         varchar(255)  null comment '封面图',
    service_count int           null comment '服务次数',
    service_area  json          null comment '服务范围'
)
    comment '套餐信息表';

create index auto_idx_idx_name
    on meal_info (name);

create table medical_record
(
    id           bigint auto_increment
        primary key,
    create_time  datetime       null comment '创建时间',
    update_time  datetime       null comment '更新时间',
    visit_date   datetime       null comment '就诊日期',
    hospital_id  int            null comment '医院 ID',
    doctor_id    varchar(255)   null comment '医生 ID',
    hospital     varchar(255)   null comment '医院',
    doctor_name  varchar(255)   null comment '医生姓名',
    diagnosis    varchar(255)   null comment '诊断结果',
    prescription varchar(255)   null comment '处方内容',
    cost         decimal(10, 4) null comment '费用',
    patient_id   bigint         null comment '患者ID'
)
    comment '就诊记录';

create index auto_idx_medical_record_visit_date
    on medical_record (visit_date);

create table order_detail
(
    id                bigint auto_increment
        primary key,
    create_time       datetime      null comment '创建时间',
    update_time       datetime      null comment '更新时间',
    goods_id          bigint        null comment '商品ID',
    quantity          int           null comment '数量',
    price             double(6, 2)  null comment '单价',
    total_price       double(6, 2)  null comment '总价',
    discount_amount   double(6, 2)  null comment '优惠金额',
    actual_amount     double(6, 2)  null comment '实付金额',
    logistics_number  varchar(255)  null comment '物流单号',
    after_sale_status int default 0 null comment '售后状态 0-无售后 1-申请中 2-处理完成',
    order_id          bigint        null comment '订单ID'
)
    comment '订单详情';

create index auto_idx_order_detail_order_id
    on order_detail (order_id);

create table order_info
(
    id           bigint auto_increment
        primary key,
    create_time  datetime      null comment '创建时间',
    update_time  datetime      null comment '更新时间',
    order_number varchar(255)  null comment '编号',
    status       int default 0 null comment '状态 0-待支付 1-已支付 2-配送中 3-已完成 4-已取消 5-退款中',
    total_amount double(6, 2)  null comment '总金额',
    user_id      bigint        null comment '用户ID',
    pay_type     int default 0 null comment '支付方式 0-微信 1-支付宝 2-银行卡',
    address      varchar(255)  null comment '收货地址',
    remark       varchar(255)  null comment '备注',
    constraint auto_idx_order_info_order_number
        unique (order_number)
)
    comment '订单信息';

create table order_log
(
    id                bigint auto_increment
        primary key,
    create_time       datetime      null comment '创建时间',
    update_time       datetime      null comment '更新时间',
    operation_type    int default 0 null comment '操作类型 0-创建 1-支付 2-发货 3-退款 4-备注更新',
    operation_content varchar(255)  null comment '操作内容',
    operator_id       bigint        null comment '操作人员ID',
    operation_time    datetime      null comment '操作时间',
    order_id          bigint        null comment '订单ID'
)
    comment '订单日志';

create index auto_idx_order_log_operation_time
    on order_log (operation_time);

create table order_statistics
(
    id                bigint auto_increment
        primary key,
    create_time       datetime     null comment '创建时间',
    update_time       datetime     null comment '更新时间',
    total_orders      int          null comment '订单总数',
    total_amount      double(6, 2) null comment '总金额',
    refund_count      int          null comment '退款数',
    completed_count   int          null comment '完成数',
    cancelled_count   int          null comment '取消数',
    paid_orders       int          null comment '支付订单数',
    delivering_orders int          null comment '配送订单数',
    statistics_date   datetime     null comment '统计日期'
)
    comment '订单统计';

create table patient_info
(
    id                    bigint auto_increment
        primary key,
    create_time           datetime      null comment '创建时间',
    update_time           datetime      null comment '更新时间',
    name                  varchar(255)  null comment '姓名',
    gender                int default 0 null comment '性别 0-未知 1-男 2-女',
    birthday              datetime      null comment '生日',
    phone                 varchar(255)  null comment '电话',
    address               varchar(255)  null comment '地址',
    type                  int default 0 null comment '类型 0=正常 1=沟通不便 2=行动不便 3=其他不便',
    medical_record_number varchar(255)  null comment '病历号',
    medical_history       varchar(255)  null comment '病史',
    allergy_history       varchar(255)  null comment '过敏史',
    remark                varchar(255)  null comment '备注',
    height                int           null comment '身高(cm)',
    weight                int           null comment '体重(kg)',
    systolic_pressure     int           null comment '收缩压(mmHg)',
    diastolic_pressure    int           null comment '舒张压(mmHg)',
    user_id               bigint        null comment '关联用户ID',
    constraint auto_idx_patient_info_medical_record_number
        unique (medical_record_number),
    constraint auto_idx_patient_info_phone
        unique (phone)
)
    comment '患者档案';

create index auto_idx_patient_info_user_id
    on patient_info (user_id);

create table plugin_info
(
    id          bigint auto_increment
        primary key,
    create_time datetime      null comment '创建时间',
    update_time datetime      null comment '更新时间',
    name        varchar(255)  null comment '名称',
    description varchar(255)  null comment '简介',
    `key`       varchar(255)  null comment '实例对象',
    hook        varchar(50)   null comment 'Hook',
    readme      text          null comment '描述',
    version     varchar(255)  null comment '版本',
    logo        text          not null comment 'Logo(base64)',
    author      varchar(255)  null comment '作者',
    status      int default 1 null comment '状态 0-禁用 1-启用',
    plugin_json json          not null comment '插件的plugin.json',
    config      json          null comment '配置',
    constraint auto_idx_plugin_info_key
        unique (`key`)
)
    comment '插件信息';

create index auto_idx_plugin_info_hook
    on plugin_info (hook);

create table recycle_data
(
    id          bigint auto_increment
        primary key,
    create_time datetime      null comment '创建时间',
    update_time datetime      null comment '更新时间',
    entity_info json          null comment '表信息',
    user_id     bigint        not null comment '操作人',
    data        json          null comment '被删除的数据',
    url         varchar(255)  not null comment '请求的接口',
    params      json          not null comment '请求参数',
    count       int default 1 null comment '删除数据条数'
)
    comment '数据回收站表';

create index auto_idx_recycle_data_user_id
    on recycle_data (user_id);

create table space_info
(
    id          bigint auto_increment
        primary key,
    create_time datetime         null comment '创建时间',
    update_time datetime         null comment '更新时间',
    url         varchar(255)     not null comment '地址',
    type        varchar(255)     not null comment '类型',
    classify_id int              null comment '分类ID',
    file_id     varchar(255)     null comment '文件id',
    name        varchar(255)     null comment '文件名',
    size        int              null comment '文件大小',
    version     bigint default 1 null comment '文档版本',
    file_path   varchar(255)     null comment '文件位置'
)
    comment '文件空间信息';

create index auto_idx_space_info_file_id
    on space_info (file_id);

create table space_type
(
    id          bigint auto_increment
        primary key,
    create_time datetime     null comment '创建时间',
    update_time datetime     null comment '更新时间',
    name        varchar(255) not null comment '类别名称',
    parent_id   int          null comment '父分类ID'
)
    comment '图片空间信息分类';

create table task_info
(
    id            bigint auto_increment
        primary key,
    create_time   datetime      null comment '创建时间',
    update_time   datetime      null comment '更新时间',
    name          varchar(255)  not null comment '名称',
    job_id        varchar(255)  null comment '任务ID',
    repeat_count  int           null comment '最大执行次数 不传为无限次',
    every         int           null comment '每间隔多少毫秒执行一次 如果cron设置了 这项设置就无效',
    status        int default 1 not null comment '状态 0:停止 1：运行',
    service       varchar(255)  null comment '服务实例名称',
    task_type     int default 0 null comment '状态 0:cron 1：时间间隔',
    type          int default 0 null comment '状态 0:系统 1：用户',
    data          varchar(255)  null comment '任务数据',
    remark        varchar(255)  null comment '备注',
    cron          varchar(255)  null comment 'cron',
    next_run_time datetime      null comment '下一次执行时间',
    start_date    datetime      null comment '开始时间',
    end_date      datetime      null comment '结束时间'
)
    comment '任务信息';

create table task_log
(
    id          bigint auto_increment
        primary key,
    create_time datetime      null comment '创建时间',
    update_time datetime      null comment '更新时间',
    task_id     bigint        not null comment '任务ID',
    status      int default 0 null comment '状态 0：失败 1：成功',
    detail      text          null comment '详情'
)
    comment '任务日志';

create index auto_idx_task_log_task_id
    on task_log (task_id);

create table user_address
(
    id          bigint auto_increment
        primary key,
    create_time datetime         null comment '创建时间',
    update_time datetime         null comment '更新时间',
    user_id     bigint           not null comment '用户ID',
    contact     varchar(255)     not null comment '联系人',
    phone       varchar(11)      not null comment '手机号',
    province    varchar(255)     not null comment '省',
    city        varchar(255)     not null comment '市',
    district    varchar(255)     not null comment '区',
    address     varchar(255)     not null comment '地址',
    is_default  bit default b'0' null comment '是否默认'
)
    comment '用户模块-收货地址';

create index auto_idx_user_address_phone
    on user_address (phone);

create index auto_idx_user_address_user_id
    on user_address (user_id);

create table user_info
(
    id          bigint auto_increment
        primary key,
    create_time datetime      null comment '创建时间',
    update_time datetime      null comment '更新时间',
    unionid     varchar(255)  null comment '登录唯一ID',
    avatar_url  varchar(255)  null comment '头像',
    nick_name   varchar(255)  null comment '昵称',
    phone       varchar(255)  null comment '手机号',
    gender      int default 0 null comment '性别 0-未知 1-男 2-女',
    status      int default 0 null comment '状态 0-正常 1-禁用 2-已注销',
    role        int default 0 null comment '用户角色 0-未知 1-患者 2-陪诊人员',
    login_type  int default 0 null comment '登录方式 0-小程序 1-公众号 2-H5',
    password    varchar(255)  null comment '密码',
    constraint auto_idx_user_info_phone
        unique (phone),
    constraint auto_idx_user_info_unionid
        unique (unionid)
)
    comment '用户信息';

create table user_wx
(
    id          bigint auto_increment
        primary key,
    create_time datetime      null comment '创建时间',
    update_time datetime      null comment '更新时间',
    unionid     varchar(255)  null comment '微信unionid',
    openid      varchar(255)  not null comment '微信openid',
    avatar_url  varchar(255)  null comment '头像',
    nick_name   varchar(255)  null comment '昵称',
    gender      int default 0 null comment '性别 0-未知 1-男 2-女',
    language    varchar(255)  null comment '语言',
    city        varchar(255)  null comment '城市',
    province    varchar(255)  null comment '省份',
    country     varchar(255)  null comment '国家',
    role        int default 0 null comment '用户角色 0-未知 1-患者 2-陪诊人员',
    type        int default 0 null comment '类型 0-小程序 1-公众号 2-H5 3-APP',
    constraint auto_idx_user_wx_openid
        unique (openid)
)
    comment '微信用户';

create index auto_idx_user_wx_unionid
    on user_wx (unionid);


