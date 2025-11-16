create table dorm
(
    id       int auto_increment comment '宿舍 ID'
        primary key,
    no       varchar(255)  not null comment '宿舍编号',
    building varchar(255)  not null comment '宿舍楼栋',
    setting  int default 0 not null comment '已住人数',
    people   int           not null comment '宿舍容量',
    status   char          not null comment '宿舍状态（0=未住满，1=已住满）'
)
    charset = utf8mb3;

create table dorm_bill
(
    id             int auto_increment
        primary key,
    water_no       varchar(20)    not null,
    electricity_no varchar(20)    not null,
    dorm_id        int            null,
    amount         decimal(10, 2) null
);

create table dorm_fix
(
    id          int auto_increment comment 'ID'
        primary key,
    type        char         not null comment '保修类型(0=水,1=电,2=墙装,3=物品,4=其他)',
    description varchar(255) not null comment '描述',
    image       varchar(255) not null comment '报修图片',
    create_time datetime     not null comment '手机',
    update_time datetime     null,
    status      char         not null comment '保修处理状态(0=待接收,1=已接收,2=已处理,3=无法处理,4=已撤销)',
    dorm_id     int          not null,
    constraint dorm_fix_dorm_id_fk
        foreign key (dorm_id) references dorm (id)
)
    charset = utf8mb3;

create table dorm_ranking
(
    id               int auto_increment
        primary key,
    dorm_id          int      not null,
    health_score     int      not null comment '卫生分',
    beauty_score     int      not null comment '美观分',
    safety_score     int      not null comment '安全分',
    atmosphere_score int      not null comment '氛围分',
    create_time      datetime not null,
    constraint dorm_ranking_dorm_id_fk
        foreign key (dorm_id) references dorm (id)
)
    comment '宿舍评分';

create table user
(
    id       int auto_increment comment 'ID'
        primary key,
    username varchar(20)                               not null comment '用户名',
    password varchar(100)                              not null comment '密码',
    email    varchar(255)                              null comment '邮箱',
    phone    varchar(11)                               not null comment '手机',
    avatar   varchar(255) default '23455570244200.jpg' not null comment '头像',
    role     char                                      not null comment '角色（0=admin，1=学生）',
    constraint user_pk
        unique (username)
)
    charset = utf8mb3;

create table board
(
    id              int auto_increment comment 'ID'
        primary key,
    content         varchar(255) not null,
    user_id         int          not null,
    create_time     datetime     not null,
    parent_board_id int          null,
    root_board_id   int          null,
    constraint board_user_id_fk
        foreign key (user_id) references user (id)
)
    charset = utf8mb3;

create table notice
(
    id          int auto_increment comment 'id'
        primary key,
    user_id     int          not null comment '发送人ID',
    title       varchar(255) not null comment '标题',
    content     varchar(255) not null comment '内容',
    create_time datetime     not null,
    constraint notice_user_id_fk
        foreign key (user_id) references user (id)
)
    charset = utf8mb3;

create table user_card_manager
(
    id      int auto_increment
        primary key,
    no      varchar(20)  not null comment '校园卡管理员编号',
    name    varchar(255) not null,
    sex     char         not null,
    age     int          null,
    user_id int          not null,
    constraint user_card_manager_pk_2
        unique (no),
    constraint user_card_manager_user_id_fk
        foreign key (user_id) references user (id)
)
    comment '校园卡（一卡通和电话卡）管理员';

create table user_serviceman
(
    id      int auto_increment
        primary key,
    no      varchar(20)  not null,
    name    varchar(255) not null,
    sex     char         not null,
    age     int          null,
    user_id int          not null,
    constraint user_serviceman_pk
        unique (no),
    constraint user_serviceman_pk_2
        unique (no),
    constraint user_serviceman_user_id_fk
        foreign key (user_id) references user (id)
)
    comment '维修人员';

create table user_student
(
    id      int auto_increment
        primary key,
    no      varchar(20)  not null comment '学号',
    name    varchar(255) not null,
    sex     char         not null,
    age     int          null,
    major   varchar(255) not null,
    college varchar(255) not null,
    user_id int          not null,
    dorm_id int          null comment '宿舍 ID',
    constraint user_student_pk
        unique (no),
    constraint user_student_user_id_fk
        foreign key (user_id) references user (id)
);

create table card_all_in_one
(
    id         int auto_increment
        primary key,
    student_id int            not null,
    no         char(20)       not null,
    amount     decimal(10, 2) not null,
    password   varchar(255)   not null,
    status     char           not null comment '(0=正常,1=申请中,2=挂失,3=注销)',
    constraint card_all_in_one_pk
        unique (no),
    constraint card_all_in_one_user_student_id_fk
        foreign key (student_id) references user_student (id)
);

create table card_all_in_one_bill
(
    id                 int auto_increment
        primary key,
    no                 varchar(20)    not null comment '流水编号',
    all_in_one_card_id int            not null,
    change_amount      decimal(10, 2) not null comment '变动金额',
    use_case           char           not null comment '发生场景(0=其他)',
    create_time        datetime       not null,
    constraint card_all_in_one_bill_card_all_in_one_id_fk
        foreign key (all_in_one_card_id) references card_all_in_one (id)
);

create table card_telephone
(
    id          int auto_increment
        primary key,
    telephone   varchar(20)    not null,
    student_id  int            not null,
    create_time datetime       not null,
    operator    char           not null comment '运营商(0=移动，1=联通，2=电信，3=广电)',
    meal_price  decimal(10, 2) not null,
    status      char           not null comment '0=正常使用 1=欠费 2=注销 3=停机',
    constraint card_telephone_pk_2
        unique (telephone),
    constraint card_telephone_user_student_id_fk
        foreign key (student_id) references user_student (id)
);

create table card_bandwidth
(
    id                int auto_increment
        primary key,
    speed             int not null,
    telephone_card_id int not null,
    constraint card_bandwidth_card_telephone_id_fk
        foreign key (telephone_card_id) references card_telephone (id)
);

create table dorm_access
(
    id          int auto_increment
        primary key,
    student_id  int          not null,
    type        char         not null comment '(0=出校,1=入校)',
    reason      char(255)    not null,
    source      varchar(255) null,
    destination varchar(255) null comment '出校去往的地点',
    create_time datetime     not null,
    update_time datetime     null,
    status      int          not null comment '0=待审核 1=已通过 2=已拒绝',
    constraint dorm_access_user_student_id_fk
        foreign key (student_id) references user_student (id)
);

create table dorm_move
(
    id           int auto_increment comment 'ID'
        primary key,
    type         char         not null comment '0 换宿舍 1 走读 2 退学或毕业',
    student_id   int          not null,
    from_dorm_id int          null,
    to_dorm_id   int          null,
    prove        varchar(255) not null comment '证明材料',
    status       char         not null comment '搬迁状态(0=待辅导员审核,1=待学办审核,2=待学工部审核,3=通过,4=拒绝,5=已撤销)',
    create_time  datetime     not null,
    update_time  datetime     null,
    constraint dorm_move_user_student_id_fk
        foreign key (student_id) references user_student (id)
)
    charset = utf8mb3;

create index dorm_move_dorm_id_fk
    on dorm_move (from_dorm_id);

create index dorm_move_dorm_id_fk_2
    on dorm_move (to_dorm_id);

create table dorm_objects
(
    id          int auto_increment
        primary key,
    no          varchar(20)  not null,
    type        char         not null comment '0=行李,1=快递,2=其他',
    description varchar(255) not null,
    student_id  int          not null,
    create_time datetime     not null,
    constraint dorm_objects_user_student_id_fk
        foreign key (student_id) references user_student (id)
);

create index user_student_dorm_id_fk
    on user_student (dorm_id);

create table user_supervisor
(
    id       int auto_increment comment 'ID'
        primary key,
    no       varchar(255) not null comment '宿管编号',
    name     varchar(255) not null comment '用户名',
    sex      char         not null comment '密码',
    age      int          null comment '邮箱',
    building varchar(255) not null comment '负责的楼栋',
    user_id  int          not null,
    constraint user_supervisor_pk
        unique (no),
    constraint user_supervisor_user_id_fk
        foreign key (user_id) references user (id)
)
    charset = utf8mb3;

create table user_teacher
(
    id           int auto_increment
        primary key,
    no           varchar(20)  not null,
    name         varchar(255) not null,
    sex          char         not null,
    age          int          null,
    major        varchar(255) not null,
    college      varchar(255) not null,
    teacher_type char         not null comment '教师类型(0=辅导员,1=学办,2=学工部)',
    user_id      int          not null,
    constraint user_teacher_pk
        unique (no),
    constraint user_teacher_user_id_fk
        foreign key (user_id) references user (id)
);


