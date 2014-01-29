
create table bron.hydro_node (
	id serial,
 	job_id bigint,
	gfid text,

	inspire_id_dataset_code text not null,
	inspire_id_local_id text not null,
	name text not null,
	category text not null,

	primary key (id),
 	constraint fk_job_id foreign key (job_id) references manager.job (id)
);
SELECT AddGeometryColumn ('bron','hydro_node','geometry',28992,'GEOMETRY',2);
ALTER TABLE bron.hydro_node ALTER COLUMN "geometry" SET NOT NULL;

CREATE INDEX idx_bron_hydro_node_geometry ON bron.hydro_node USING GIST (geometry);

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON bron.hydro_node TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE bron.hydro_node_id_seq TO inspire;
----------------------------------
