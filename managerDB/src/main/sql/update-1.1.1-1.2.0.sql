begin;

-- Create table manager.etljob:
create table manager.etljob (
	dataset_url varchar(255),
	feature_count int4,
	geometry_error_count int4,
	metadata_update_datum timestamp,
	metadata_url varchar(255),
	uuid varchar(255),
	verversen bool,
	wfsUrl varchar(255),
	bronhouder_id int8,
	datasettype_id int8,
	id int8 not null,
	primary key (id)
);

alter table manager.etljob 
	add constraint FKB2EED100EB458D53 
	foreign key (id) 
	references manager.job;

alter table manager.etljob 
	add constraint FKB2EED100CD434FD2 
	foreign key (bronhouder_id) 
	references manager.Bronhouder;

alter table manager.etljob 
	add constraint FKB2EED100E09E2042 
	foreign key (datasettype_id) 
	references manager.DatasetType;

-- Copy relevant data from job to etljob:
insert into manager.etljob (dataset_url, feature_count, geometry_error_count, metadata_update_datum, metadata_url, uuid, verversen, wfsUrl, bronhouder_id, datasettype_id, id)
	select dataset_url, feature_count, geometry_error_count, metadata_update_datum, metadata_url, uuid, verversen, wfsUrl, bronhouder_id, datasettype_id, id from manager.job;

-- Add job_type field to the job table:
ALTER TABLE manager.job ADD COLUMN job_type character varying(20);

-- Populate the job_type field:
update manager.job set job_type = (select naam from manager.jobtype jt where jt.id = jobtype_id);

ALTER TABLE manager.job ALTER COLUMN job_type SET NOT NULL;

-- Rename fields in the job table:
alter table manager.job rename column creatietijd to createtime;
alter table manager.job rename column eindtijd to finishtime;
alter table manager.job rename column prioriteit to priority;
alter table manager.job rename column resultaat to result;
alter table manager.job rename column starttijd to starttime;

-- Drop views:
drop view if exists manager.job_last_import;
drop view if exists manager.job_last_validate;
drop view if exists manager.job_last;
drop view if exists manager.job_validated;
drop view if exists manager.joblog_overview;

-- Drop unused columns:
alter table manager.job drop column jobtype_id;
alter table manager.job drop column dataset_url;
alter table manager.job drop column feature_count;
alter table manager.job drop column geometry_error_count;
alter table manager.job drop column metadata_update_datum;
alter table manager.job drop column metadata_url;
alter table manager.job drop column uuid;
alter table manager.job drop column verversen;
alter table manager.job drop column wfsUrl;
alter table manager.job drop column bronhouder_id;
alter table manager.job drop column datasettype_id;

-- Add column to joblog
alter table manager.joblog add column context character varying(255);

-- Create views:
create view manager.joblog_overview as
select job_id, key, max(message) message, count(*)
from manager.joblog jl
group by job_id, key;

create view manager.job_validated as
select * from manager.job job
join manager.etljob etljob using (id)
where job.status = 'FINISHED' and (
	job.job_type = 'VALIDATE'
	or (job.job_type = 'IMPORT' and etljob.verversen)
);

create view manager.job_last as
select * from manager.job_validated j0
where status = 'FINISHED' and not exists (
	select * from manager.job_validated j1
	join manager.etljob j1_etl using (id)
	where status = 'FINISHED'
	and j1_etl.datasettype_id = j0.datasettype_id	
	and j1_etl.bronhouder_id = j0.bronhouder_id	
	and j1.createtime > j0.createtime	
);

create view manager.job_last_validate as
select * from manager.job_validated j0
where status = 'FINISHED' and not exists (
	select * from manager.job_validated j1
	join manager.etljob j1_etl using (id)
	where status = 'FINISHED'
	and j1_etl.datasettype_id = j0.datasettype_id	
	and j1_etl.bronhouder_id = j0.bronhouder_id	
	and j1.job_type = j0.job_type
	and j1.createtime > j0.createtime	
) and job_type = 'VALIDATE';

create view manager.job_last_import as
select * from manager.job_validated j0
where status = 'FINISHED' and not exists (
	select * from manager.job_validated j1
	join manager.etljob j1_etl using (id)
	where status = 'FINISHED'
	and j1_etl.datasettype_id = j0.datasettype_id	
	and j1_etl.bronhouder_id = j0.bronhouder_id	
	and j1.job_type = j0.job_type
	and j1.createtime > j0.createtime
	 and j1.verversen
) and job_type = 'IMPORT';

commit;