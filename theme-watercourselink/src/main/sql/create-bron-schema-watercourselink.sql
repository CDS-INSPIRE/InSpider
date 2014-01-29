
create table bron.watercourse_link (
	id serial,
 	job_id bigint,
	gfid text,

	inspire_id_namespace text not null,
	inspire_id_local_id text not null,
	name text not null,
	end_node_local_id text not null,
	start_node_local_id text not null,
	
	primary key (id),
 	constraint fk_job_id foreign key (job_id) references manager.job (id)
);
SELECT AddGeometryColumn ('bron','watercourse_link','geometry',28992,'GEOMETRY',2);
ALTER TABLE bron.watercourse_link ALTER COLUMN "geometry" SET NOT NULL;

CREATE INDEX idx_bron_watercourse_link_geometry ON bron.watercourse_link USING GIST (geometry);

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON bron.watercourse_link TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE bron.watercourse_link_id_seq TO inspire;
----------------------------------
