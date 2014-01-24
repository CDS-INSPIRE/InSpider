delete from inspire.production_installation_part;

insert into inspire.production_installation_part (
	id,
 	job_id,
	gfid,
	inspire_id_namespace,
	inspire_id_local_id,
	production_installation_id,
	"name",
	status_nil_reason,	
	status_xsi_nil,
	status_type,
	status_description,
	"type",
	type_codespace,
	technique,
	point_geometry)
select
    id,
    job_id,
    gfid,
    'NL.' || inspire_id_dataset_code inspire_id_namespace,
	inspire_id_local_id,
	production_installation_id,
	"name",
    CASE WHEN status_type IS NULL THEN 'UNKNOWN'
    END status_nil_reason,
    CASE WHEN status_type IS NULL THEN 'true'
    END status_xsi_nil,
	status_type,
	status_description,
	"type",
	type_codespace,
	technique,
	point_geometry
 from bron.production_installation_part;
