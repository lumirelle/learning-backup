create schema cainiao collate utf8mb4_0900_ai_ci;

create table cainiao.logistics_progress
(
    id       int auto_increment
        primary key,
    datetime varchar(255)                                                                                not null,
    status   enum ('AWAITING_PICKUP', 'ON_DELIVERY', 'IN_TRANSIT', 'UNSHIPPED', 'HAS_SIGNED', 'UNKNOWN') null,
    message  varchar(255)                                                                                not null
);

create table cainiao.parcel_stakeholder
(
    id        int auto_increment
        primary key,
    name      varchar(255) not null,
    telephone varchar(255) not null,
    address   varchar(255) not null
);

create table cainiao.parcel
(
    id                int auto_increment
        primary key,
    title             varchar(255)                                                                                          not null,
    image_url         varchar(255)                                                                                          not null,
    logistics_id      varchar(255)                                                                                          not null,
    logistics_status  enum ('AWAITING_PICKUP', 'ON_DELIVERY', 'IN_TRANSIT', 'UNSHIPPED', 'HAS_SIGNED', 'UNKNOWN')           not null,
    logistics_company enum ('ZHONG_TONG', 'YUAN_TONG', 'SHEN_TONG', 'YUN_DA', 'JI_TU', 'SHUN_FENG', 'CHINA_POST', 'DEPPON') not null,
    receiver_id       int                                                                                                   not null,
    sender_id         int                                                                                                   not null,
    constraint parcel_parcel_stakeholder_id_fk
        foreign key (receiver_id) references parcel_stakeholder (id),
    constraint parcel_parcel_stakeholder_id_fk_2
        foreign key (sender_id) references parcel_stakeholder (id)
);

create table cainiao.parcel_logistics_progress
(
    id                    int auto_increment
        primary key,
    parcel_id             int not null,
    logistics_progress_id int not null,
    constraint parcel_logistics_progress_logistics_progress_id_fk
        foreign key (logistics_progress_id) references logistics_progress (id),
    constraint parcel_logistics_progress_parcel_id_fk
        foreign key (parcel_id) references parcel (id)
);
