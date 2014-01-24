-- CREATE JOB SCHEDULER
drop function if exists manager.schedule_jobs();
create function manager.schedule_jobs() returns void as $$
DECLARE
    datasetresults RECORD;
begin
	FOR datasetresults IN SELECT * FROM manager.dataset where dataset.actief=true LOOP
		-- insert an import job for each active dataset
		insert into manager.job(id, createtime, job_type, priority, status)
			select nextval('manager.hibernate_sequence') id, now() createtime, 
				'IMPORT' job_type,
				(select prioriteit from manager.jobtype where naam = 'IMPORT') priority,
				'CREATED' status;
				
		insert into manager.etljob(id, verversen, datasettype_id, uuid, bronhouder_id )
			select currval('manager.hibernate_sequence') id,  
				false verversen, 
				datasetresults.type_id datasettype_id, datasetresults.uuid uuid, datasetresults.bronhouder_id bronhouder_id;
	end loop;
	
	-- insert a transform job to run after all import jobs have been processed
	insert into manager.job (id, createtime, job_type, priority, status)
		select nextval('manager.hibernate_sequence') id, now() createtime,
		'TRANSFORM' job_type,
		(select prioriteit from manager.jobtype where naam = 'TRANSFORM') priority, 
		'CREATED' status;
	insert into manager.etljob (id, verversen)
		select currval('manager.hibernate_sequence') id,  
		false verversen;
end;
$$ language plpgsql;
-- ----------------------
