create table big_java_ee.commodities
(
    fruitid    varchar(255) not null
        primary key,
    name       varchar(255) null,
    price      double       null,
    locality   varchar(255) null,
    createtime timestamp    null
);

