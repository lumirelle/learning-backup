create table big_java_ee.retailer
(
    retailerid varchar(255) not null
        primary key,
    name       varchar(255) null,
    telephone  varchar(255) null,
    address    varchar(255) null,
    status     int          null,
    createtime timestamp    null
);

