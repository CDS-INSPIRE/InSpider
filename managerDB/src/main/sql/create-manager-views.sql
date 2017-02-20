
-- CREATE VIEWS MANAGER SCHEMA
create view manager.joblog_overview as
select job_id, key, max(message) message, count(*)
from manager.joblog jl
group by job_id, key;

--create view manager.job_info as
--select j.id job_id, j.starttijd job_starttijd, j.metadata_update_datum job_metadata_update_datum, 
--	d.id dataset_id, dt.id datasettype_id, b.id bronhouder_id,dt.naam datasettype_naam, 
--	b.provincie bronhouder_provincie
--from manager.job j
--join manager.dataset d on d.id = j.dataset_id
--join manager.datasettype dt on dt.id = d.type_id
--join manager.bronhouder b on b.id = d.bronhouder_id;
--
--create view manager.joblog_report as
--select o.job_id, job_starttijd, job_metadata_update_datum, bronhouder_provincie, 
--datasettype_naam, key, count, message
--from manager.joblog_overview o
--join manager.job_info i on i.job_id = o.job_id
--where key != 'HAS_MORE_EVENTS'
--order by bronhouder_provincie, datasettype_naam;

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

--create view manager.joblog_report_last_validate as
--select r.* from manager.joblog_report r
--join manager.job_last_validate l on l.id = r.job_id;
--
--create view manager.joblog_report_last_import as
--select r.* from manager.joblog_report r
--join manager.job_last_import l on l.id = r.job_id;
--
--create view manager.joblog_report_last as
--select r.* from manager.joblog_report r
--join manager.job_last l on l.id = r.job_id;
-- ----------------------------------

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

create view manager.themabronhouderauthorization as select * from manager.bronhouderthema;