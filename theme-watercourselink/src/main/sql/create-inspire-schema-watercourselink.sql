
create table inspire.watercourse_link (
	id serial,
 	job_id bigint,
	gfid text,

	inspire_id_namespace text not null,
	inspire_id_local_id text not null,
	name text not null,
	end_node_href text not null,
	start_node_href text not null,
	
	primary key (id),
 	constraint fk_job_id foreign key (job_id) references manager.job (id)
);
SELECT AddGeometryColumn ('inspire','watercourse_link','geometry',28992,'GEOMETRY',2);
CREATE INDEX watercourse_link_geometry_idx ON inspire.watercourse_link USING gist(geometry);

ALTER TABLE inspire.watercourse_link ALTER COLUMN "geometry" SET NOT NULL;

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON inspire.watercourse_link TO inspire;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE inspire.watercourse_link_id_seq TO inspire;
----------------------------------
