delete from inspire.production_facility;

insert into inspire.production_facility (
    id,
    job_id,
    gfid,
    inspire_id_namespace,
    inspire_id_local_id,
    production_facility_id,
    thematic_identifier,
    thematic_identifier_scheme,
    function_activity,
    function_input,
    function_output,
    function_description,    
    name,
    status_nil_reason,
    status_xsi_nil,
    status_type,
    status_description,
    geometry,
    surface_geometry
    )
select
    id,
    job_id,
    gfid,
    'NL.' || inspire_id_dataset_code,
    inspire_id_local_id,
    production_facility_id,
    thematic_identifier,
    thematic_identifier_scheme,
    function_activity,
    function_input,
    function_output,
    function_description,
    "name",
    CASE WHEN status_type IS NULL THEN 'UNKNOWN' END,
    CASE WHEN status_type IS NULL THEN 'true' END,
    status_type,
    status_description,
    geometry,
    surface_geometry
from bron.production_facility;
