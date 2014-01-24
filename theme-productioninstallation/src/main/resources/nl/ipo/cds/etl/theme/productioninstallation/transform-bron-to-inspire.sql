delete from inspire.production_installation;

insert into inspire.production_installation (
	id,
 	job_id,
	gfid,
	inspire_id_namespace,
	inspire_id_local_id,
	production_facility_id,
	production_installation_id,
	thematic_identifier,
	thematic_identifier_scheme,
	"name",
	description,
	status_nil_reason,	
	status_xsi_nil,
	status_type,
	status_description,
	point_geometry,
	surface_geometry,
	line_geometry,
	"type")
select
    id,
    job_id,
    gfid,
    'NL.' || inspire_id_dataset_code inspire_id_namespace,
	inspire_id_local_id,
	production_facility_id,
	production_installation_id,
	thematic_identifier,
	thematic_identifier_scheme,
	"name",
	description,
    CASE WHEN status_type IS NULL THEN 'UNKNOWN'
    END status_nil_reason,
    CASE WHEN status_type IS NULL THEN 'true'
    END status_xsi_nil,
	status_type,
	status_description,
	point_geometry,
	surface_geometry,
	line_geometry,
	"type"
 from bron.production_installation;
