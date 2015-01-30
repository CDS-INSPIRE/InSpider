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

CREATE TABLE manager.job(
  job_type text NOT NULL,
  id bigint NOT NULL,
  priority integer NOT NULL,
  result text,
  status text
)
;

CREATE TABLE manager.etljob(
  id bigint NOT NULL,
  parameters character varying(250)
)
;

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
		'"{"tag":"testTag","thema":"LandelijkGebiedBeheer"}"' 	
);		







