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


