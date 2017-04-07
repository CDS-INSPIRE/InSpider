DROP ALL OBJECTS;
-- CREATE VRN SCHEMA
create schema vrn;


-- Create simplified table, geometry can not be inserted in H2, so complete table will not work anyway
CREATE TABLE vrn.gebiedbeheer_landelijk_tagged ( 
	id serial NOT NULL,
	identificatie text NOT NULL,
	tag text NOT NULL,
	job_id bigint
)
;

INSERT INTO vrn.gebiedbeheer_landelijk_tagged 
            (id, 
             identificatie, 
             tag,
             job_id) 
VALUES      ('1', 
             'landelijkGebiedBeheer',
             'TestTag', 
             1 )
;

create schema manager;

    create table manager.job (
        job_type varchar(20) not null,
        id int8 not null,
        createTime timestamp,
        finishTime timestamp,
        priority int4 not null,
        result text,
        startTime timestamp,
        status varchar(255),
        primary key (id)
    );

    create table manager.etljob (
        dataset_url varchar(255),
        feature_count int4,
        force_execution bool default false ,
        geometry_error_count int4,
        metadata_update_datum timestamp,
        metadata_url varchar(255),
        parameters TEXT,
        uuid varchar(255),
        verversen bool default false ,
        wfsUrl varchar(255),
        bronhouder_id int8,
        datasettype_id int8,
        id int8 not null,
        primary key (id)
    );

INSERT INTO manager.job(
		job_type,
		id,
		priority,
		result,
		status)
VALUES  ('TAG',
		 12,
		 200,
		 'result',
		 'PREPARED'
);

INSERT INTO manager.etljob(
		id,
		parameters)
VALUES	(12,
		'{"tag":"testTag","thema":"LandelijkGebiedBeheer"}'
);		







