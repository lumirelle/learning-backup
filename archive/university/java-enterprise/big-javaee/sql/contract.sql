create table big_java_ee.contract
(
    contractid varchar(255) not null
        primary key,
    barcode    varchar(255) null,
    type       int          null,
    retailerid varchar(255) null,
    createtime timestamp    null
);

