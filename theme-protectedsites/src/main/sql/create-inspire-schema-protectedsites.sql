
-- CREATE INSPIRE SCHEMA

create table inspire.dt_site_protection_classification(
	id serial primary key,
	site_protection_classification text
);

create table inspire.dt_site_designation(
	id serial primary key,
	site_designation_schema text,
	site_designation text
);

create table inspire.protected_site(    
	id serial primary key,
	geometry geometry,
	legal_foundation_date timestamp,
	legal_foundation_document text,
	inspire_id_local_id text,
	inspire_id_namespace text	
);

create table inspire.site_name(
	id serial primary key,
	fk_protected_site bigint references inspire.protected_site(id),
	site_name text
);

create table inspire.jt_site_protection_classification(
	fk_site_protection_classification bigint references inspire.dt_site_protection_classification(id),
	fk_protected_site bigint references inspire.protected_site(id),

	primary key (fk_site_protection_classification, fk_protected_site)
);

create table inspire.jt_site_designation(
	fk_site_designation bigint references inspire.dt_site_designation(id),
	fk_protected_site bigint references inspire.protected_site(id),
	percentage_under_designation text,

	primary key (fk_site_designation, fk_protected_site)
);

create view inspire.site_protection_classification as
select fk_protected_site, dt.site_protection_classification from inspire.jt_site_protection_classification jt
join inspire.dt_site_protection_classification dt on dt.id = jt.fk_site_protection_classification;

create view inspire.site_designation as
select fk_protected_site, dt.site_designation_schema, dt.site_designation, jt.percentage_under_designation 
from inspire.jt_site_designation jt
join inspire.dt_site_designation dt on dt.id = fk_site_designation;

create view inspire.named_place as
select id, geometry, inspire_id_local_id, inspire_id_namespace 
from inspire.protected_site
where id in (select fk_protected_site from inspire.site_name);

create table inspire.protected_site_nature_conservation(
	id serial primary key,
	geometry geometry,
	fk_site_designation bigint references inspire.dt_site_designation(id)
);

create table inspire.protected_site_archaeological(
	id serial primary key,
	geometry geometry,
	fk_site_designation bigint references inspire.dt_site_designation(id)
);

create table inspire.protected_site_cultural(
	id serial primary key,
	geometry geometry,
	fk_site_designation bigint references inspire.dt_site_designation(id)
);

create table inspire.protected_site_ecological(
	id serial primary key,
	geometry geometry,
	fk_site_designation bigint references inspire.dt_site_designation(id)
);

create table inspire.protected_site_landscape(
	id serial primary key,
	geometry geometry,
	fk_site_designation bigint references inspire.dt_site_designation(id)
);

create table inspire.protected_site_environment(
	id serial primary key,
	geometry geometry,
	fk_site_designation bigint references inspire.dt_site_designation(id)
);

create table inspire.protected_site_geological(
	id serial primary key,
	geometry geometry,
	fk_site_designation bigint references inspire.dt_site_designation(id)
);

create table inspire.protected_site_aardkundige_waarden(
	id serial primary key,
	geometry geometry
);

create table inspire.protected_site_ecologische_hoofdstructuur(
	id serial primary key,
	geometry geometry
);

create table inspire.protected_site_nationale_landschappen(
	id serial primary key,
	geometry geometry
);

create table inspire.protected_site_provinciale_monumenten(
	id serial primary key,
	geometry geometry
);

create table inspire.protected_site_stilte_gebieden(
	id serial primary key,
	geometry geometry
);

create table inspire.protected_site_wav_gebieden(
	id serial primary key,
	geometry geometry
);
-- -----------------------------------

-- CREATE INSPIRE INDEXES
create index protected_site_designation_idx on inspire.jt_site_designation(fk_protected_site);
create index site_designation_idx on inspire.jt_site_designation(fk_site_designation);

create index protected_site_protection_classification_idx on inspire.jt_site_protection_classification(fk_protected_site);
create index site_protection_classification_idx on inspire.jt_site_protection_classification(fk_site_protection_classification);

create index protected_size_name_idx on inspire.site_name(fk_protected_site);

create index protected_site_namespace_idx on inspire.protected_site(inspire_id_namespace);
create index protected_site_local_id_idx on inspire.protected_site(inspire_id_local_id);

create index protected_site_geometry_idx on inspire.protected_site using gist(geometry);
create index protected_site_aardkundige_waarden_geometry_idx on inspire.protected_site_aardkundige_waarden using gist(geometry);
create index protected_site_archaeological_geometry_idx on inspire.protected_site_archaeological using gist(geometry);
create index protected_site_cultural_geometry_idx on inspire.protected_site_cultural using gist(geometry);
create index protected_site_ecological_geometry_idx on inspire.protected_site_ecological using gist(geometry);
create index protected_site_ecologische_hoofdstructuur_geometry_idx on inspire.protected_site_ecologische_hoofdstructuur using gist(geometry);
create index protected_site_environment_geometry_idx on inspire.protected_site_environment using gist(geometry);
create index protected_site_geological_geometry_idx on inspire.protected_site_geological using gist(geometry);
create index protected_site_landscape_geometry_idx on inspire.protected_site_landscape using gist(geometry);
create index protected_site_nationale_landschappen_geometry_idx on inspire.protected_site_nationale_landschappen using gist(geometry);
create index protected_site_nature_conservation_geometry_idx on inspire.protected_site_nature_conservation using gist(geometry);
create index protected_site_provinciale_monumenten_geometry_idx on inspire.protected_site_provinciale_monumenten using gist(geometry);
create index protected_site_stilte_gebieden_geometry_idx on inspire.protected_site_stilte_gebieden using gist(geometry);
create index protected_site_wav_gebieden_geometry_idx on inspire.protected_site_wav_gebieden using gist(geometry);
-- ----------------------------------------

-- GRANT INSPIRE SCHEMA
GRANT USAGE ON SCHEMA inspire TO inspire;

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.dt_site_protection_classification TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.dt_site_designation TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.protected_site TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.site_name TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.jt_site_protection_classification TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.jt_site_designation TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.protected_site_nature_conservation TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.protected_site_archaeological TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.protected_site_cultural TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.protected_site_ecological TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.protected_site_landscape TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.protected_site_environment TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.protected_site_geological TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.protected_site_aardkundige_waarden TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.protected_site_ecologische_hoofdstructuur TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.protected_site_nationale_landschappen TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.protected_site_provinciale_monumenten TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.protected_site_stilte_gebieden TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.protected_site_wav_gebieden TO inspire;

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.site_protection_classification TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.site_designation TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.named_place TO inspire;

GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.dt_site_designation_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.dt_site_protection_classification_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.site_name_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.protected_site_aardkundige_waarden_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.protected_site_archaeological_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.protected_site_cultural_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.protected_site_ecological_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.protected_site_ecologische_hoofdstructuur_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.protected_site_environment_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.protected_site_geological_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.protected_site_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.protected_site_landscape_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.protected_site_nationale_landschappen_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.protected_site_nature_conservation_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.protected_site_provinciale_monumenten_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.protected_site_stilte_gebieden_id_seq TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.protected_site_wav_gebieden_id_seq TO inspire;
-- ---------------------------------------
