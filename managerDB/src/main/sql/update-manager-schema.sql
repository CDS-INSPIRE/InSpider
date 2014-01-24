-- update table structure for job and dataset tables

-- add actief to datatset

ALTER TABLE manager.dataset ADD COLUMN actief boolean DEFAULT true;
ALTER TABLE manager.dataset ADD COLUMN status varchar(255) DEFAULT 'GELADEN';

-- add uuid, bronhouder, datasettype to job
ALTER TABLE manager.etljob ADD COLUMN uuid varchar(255);
ALTER TABLE manager.etljob ADD COLUMN wfsurl varchar(255);
ALTER TABLE manager.etljob ADD COLUMN bronhouder_id int8;
ALTER TABLE manager.etljob ADD COLUMN datasettype_id int8;

-- add fk relations to job
alter table manager.etljob
add constraint job_bronhouder
foreign key (bronhouder_id)
references manager.Bronhouder;

alter table manager.etljob
add constraint job_datasettype
foreign key (datasettype_id)
references manager.DatasetType;

-- copy uuid, bronhouder, datasettype from dataset to job
UPDATE manager.etljob
  SET  uuid  = ds.uuid from manager.dataset as ds
  WHERE dataset_id = ds.id;

UPDATE manager.etljob
  SET  bronhouder_id  = ds.bronhouder_id from manager.dataset as ds
  WHERE dataset_id = ds.id;

UPDATE manager.etljob
  SET  datasettype_id  = ds.type_id from manager.dataset as ds
  WHERE dataset_id = ds.id;

-- drop dataset from job
ALTER TABLE manager.etljob DROP COLUMN dataset_id ;
-- drop annex from Thema
ALTER TABLE manager.thema DROP COLUMN annex_id ;

-- add jobtype REMOVE
INSERT INTO manager.jobtype(id, naam, prioriteit) VALUES ( nextval('manager.hibernate_sequence'), 'REMOVE', 250);

-- change all timestamps from without timezone to timezone 
ALTER TABLE manager.job 
    ALTER COLUMN createtime TYPE timestamp with time zone , 
    ALTER COLUMN starttime TYPE timestamp with time zone , 
    ALTER COLUMN finishtime TYPE timestamp with time zone ;
ALTER TABLE manager.joblog 
    ALTER COLUMN "time" TYPE timestamp with time zone ;

SET TIME ZONE UTC;
-- ---------------------------------
