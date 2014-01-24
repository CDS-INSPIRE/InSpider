begin;
    create table manager.AttributeMapping (
        id int8 not null,
        attributeName varchar(255),
        valid bool not null,
        dataset_id int8,
        rootOperation_id int8,
        primary key (id)
    );

    create table manager.MappingOperation (
        id int8 not null,
        inputAttributeType varchar(255),
        operation_index int4 not null,
        operationName varchar(255),
        operationType varchar(255),
        properties text,
        parent_id int8,
        primary key (id)
    );

    alter table manager.AttributeMapping 
        add constraint FK194562B2AA794262 
        foreign key (dataset_id) 
        references manager.Dataset;

    alter table manager.AttributeMapping 
        add constraint FK194562B2E08F4A66 
        foreign key (rootOperation_id) 
        references manager.MappingOperation;

    alter table manager.MappingOperation 
        add constraint FK55992859AB6E61 
        foreign key (parent_id) 
        references manager.MappingOperation;

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.AttributeMapping TO inspire;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON manager.MappingOperation TO inspire;

GRANT SELECT ON manager.AttributeMapping TO nagios;
GRANT SELECT ON manager.MappingOperation TO nagios;


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

commit;