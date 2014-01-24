begin;

create table metadata.ExtendedCapabilities (
	id int8 not null,
        metadataUrl text not null,
        code text,
        namespace text,
        primary key (id)
);

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON metadata.ExtendedCapabilities TO inspire;

alter table metadata.Service add column extendedCapabilities_id int8;

alter table metadata.Service 
        add constraint FKD97C5E957FD37AC1 
        foreign key (extendedCapabilities_id) 
        references metadata.ExtendedCapabilities;

insert into metadata.ExtendedCapabilities(id, metadataUrl)
select service_id, extendedcapability 
from metadata.service_extendedcapability;

update metadata.Service
set extendedCapabilities_id = id;

-- drop table metadata.service_extendedcapability;

alter table metadata.serviceprovider drop constraint serviceprovider_providername_key;

-- -----------------------------
-- -- metadata serviceidentification servicePath: --
-- -----------------------------
-- Add the servicepath column:
alter table metadata.serviceidentification add column servicepath text not null default 'ProtectedSites/services';

commit;