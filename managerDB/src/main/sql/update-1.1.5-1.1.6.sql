begin;

-- -----------------------------
-- -- Etljob force_execution: --
-- -----------------------------
-- Add the force_execution column:
alter table manager.etljob add column force_execution boolean not null default false;

-- -----------------------------
-- -- Dataset name: --
-- -----------------------------
-- Add the naam column:
alter table manager.dataset add column naam varchar(255);

-- -----------------------------
-- -- Datasettype refreshpolicy: --
-- -----------------------------
-- Add the refreshpolicy column:
alter table manager.datasettype add column refreshPolicy text default 'IF_MODIFIED_METADATA' not null;

-- -----------------------------
-- -- Bronhouder common name: --
-- -----------------------------
-- Add the common_name column, minus the not null constraint:
alter table manager.bronhouder add column common_name varchar(255) unique;

-- Populate the common_name column for existing bronhouders:
update manager.bronhouder set common_name = 'groningen' where code = '9920';
update manager.bronhouder set common_name = 'friesland' where code = '9921';
update manager.bronhouder set common_name = 'drenthe' where code = '9922';
update manager.bronhouder set common_name = 'overijssel' where code = '9923';
update manager.bronhouder set common_name = 'flevoland' where code = '9924';
update manager.bronhouder set common_name = 'gelderland' where code = '9925';
update manager.bronhouder set common_name = 'utrecht' where code = '9926';
update manager.bronhouder set common_name = 'noordholland' where code = '9927';
update manager.bronhouder set common_name = 'zuidholland' where code = '9928';
update manager.bronhouder set common_name = 'zeeland' where code = '9929';
update manager.bronhouder set common_name = 'noordbrabant' where code = '9930';
update manager.bronhouder set common_name = 'limburg' where code = '9931';

-- Add not-null constraint to the common_name column:
alter table manager.bronhouder alter column common_name set not null;

-- ---------------------------------------------
-- -- Bronhouder introductie contact_ prefix: --
-- ---------------------------------------------
alter table manager.bronhouder rename column naam to contact_naam;
alter table manager.bronhouder rename column adres to contact_adres;
alter table manager.bronhouder rename column plaats to contact_plaats;
alter table manager.bronhouder rename column postcode to contact_postcode;
alter table manager.bronhouder rename column telefoonnummer to contact_telefoonnummer;
alter table manager.bronhouder rename column emailadres to contact_emailadres;

-- -----------------------------------
-- -- Bronhouder provincie -> naam: --
-- -----------------------------------
alter table manager.bronhouder rename column provincie to naam;

-- -------------------------------------
-- -- Bronhouder code -> varchar(64): --
-- -------------------------------------
alter table manager.bronhouder alter column "code" set data type character varying (64);

-- ------------------------------
-- -- Bronhouder add new items --
-- ------------------------------
-- Populate with extra bronhouders:
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name)  select nextval('manager.hibernate_sequence'), 'zwr', 'ZwemwaterRegister', '', '', '', 'ZwemwaterRegister', 'zwemwaterregister@idgis.nl', '', 'zwemwaterregister';
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name)  select nextval('manager.hibernate_sequence'), 'rk', 'Risicokaart', '', '', '', 'Risicokaart', 'risicokaart@idgis.nl', '', 'risicokaart';
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name)  select nextval('manager.hibernate_sequence'), 'lgr', 'LandelijkGrondwaterRegister', '', '', '', 'LandelijkGrondwaterRegister', 'landelijkgrondwaterregister@idgis.nl', '', 'landelijkgrondwaterregister';
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name)  select nextval('manager.hibernate_sequence'), 'habitat', 'Habitat', '', '', '', 'Habitat', 'habitat@idgis.nl', '', 'habitat';

-- -------------------------------------
-- -- Bronhouder thema authorization: --
-- -------------------------------------
-- Create table:
create table manager.ThemaBronhouderAuthorization (
	thema_id int8 not null,
	bronhouder_id int8 not null,
	primary key (thema_id, bronhouder_id)
);

insert into manager.themabronhouderauthorization (thema_id, bronhouder_id) values
	( (select id from manager.thema where naam = 'Protected sites'), (select id from manager.bronhouder where code = '9920') ),
	( (select id from manager.thema where naam = 'Protected sites'), (select id from manager.bronhouder where code = '9921') ),
	( (select id from manager.thema where naam = 'Protected sites'), (select id from manager.bronhouder where code = '9922') ),
	( (select id from manager.thema where naam = 'Protected sites'), (select id from manager.bronhouder where code = '9923') ),
	( (select id from manager.thema where naam = 'Protected sites'), (select id from manager.bronhouder where code = '9924') ),
	( (select id from manager.thema where naam = 'Protected sites'), (select id from manager.bronhouder where code = '9925') ),
	( (select id from manager.thema where naam = 'Protected sites'), (select id from manager.bronhouder where code = '9926') ),
	( (select id from manager.thema where naam = 'Protected sites'), (select id from manager.bronhouder where code = '9927') ),
	( (select id from manager.thema where naam = 'Protected sites'), (select id from manager.bronhouder where code = '9928') ),
	( (select id from manager.thema where naam = 'Protected sites'), (select id from manager.bronhouder where code = '9929') ),
	( (select id from manager.thema where naam = 'Protected sites'), (select id from manager.bronhouder where code = '9930') ),
	( (select id from manager.thema where naam = 'Protected sites'), (select id from manager.bronhouder where code = '9931') );

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.ThemaBronhouderAuthorization TO inspire;

-- -------------------------
-- -- Code list mappings: --
-- -------------------------
create table manager.CodeListMapping (
    codeSpace varchar(255) not null,
    url varchar(255),
    primary key (codeSpace)
);

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.CodeListMapping TO inspire;

-- -------------------------
-- -- Meta Data Document: --
-- -------------------------

    create table manager.MetadataDocument (
        id int8 not null,
        documentName varchar(255) not null,
        documentType varchar(255) not null,
        thema_id int8,
        primary key (id)
    );

    alter table manager.MetadataDocument 
        add constraint FK870E558AEFA56D42 
        foreign key (thema_id) 
        references manager.Thema;
        
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.MetadataDocument TO inspire;

create view manager.metadatadocument_update_datum as
select md.documentname, md.documenttype, max(ej.metadata_update_datum) update_datum 
from manager.metadatadocument md
join manager.datasettype dt on dt.thema_id = md.thema_id
join manager.etljob ej on ej.datasettype_id = dt.id
join manager.job j on j.id = ej.id
where j.job_type = 'IMPORT' and j.status = 'FINISHED'
and j.finishtime > (
	select max(finishtime)
	from manager.job
	where job_type = 'TRANSFORM'
	and status = 'FINISHED')
group by md.documentname, md.documenttype;  

GRANT SELECT ON TABLE manager.metadatadocument_update_datum TO inspire;

commit;