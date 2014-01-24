create table bron.protected_site (
	id serial,
 	job_id bigint,
 	legal_foundation_date text,
 	legal_foundation_document text, 
 	inspire_id text, 
 	site_name text, 
 	site_designation text, 
 	site_protection_classification text,
 	gfid text,
 	primary key (id),
 	constraint fk_job_id foreign key (job_id) references manager.job (id)
);
SELECT AddGeometryColumn ('bron','protected_site','geometry',28992,'GEOMETRY',2);

-- ---------------------------------
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON bron.protected_site TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE bron.protected_site_id_seq TO inspire;
