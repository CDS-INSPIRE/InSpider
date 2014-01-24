delete from inspire.habitat;

insert into inspire.habitat(
    id,
    job_id,
    gfid,
    inspire_id_namespace,
    inspire_id_local_id,
	habitat_reference_habitat_type_id_code,
	habitat_reference_habitat_type_id_codespace,
	habitat_reference_habitat_type_scheme_code,
	habitat_reference_habitat_type_name,
	local_habitat_name_local_scheme,
	local_habitat_name_local_name_code,
	local_habitat_name_local_name_codespace,
	local_habitat_name_local_name,
	local_habitat_name_qualifier_local_name,
	habitat_area_covered,
	geometry,
	geom_simple
	)
select
    id,
    job_id,
    gfid,
    'NL.' || inspire_id_dataset_code,
    inspire_id_local_id,
	habitat_reference_habitat_type_id_code,
	habitat_reference_habitat_type_id_codespace,
	habitat_reference_habitat_type_scheme_code,
	habitat_reference_habitat_type_name,
	local_habitat_name_local_scheme,
	local_habitat_name_local_name_code,
	local_habitat_name_local_name_codespace,
	local_habitat_name_local_name,
	local_habitat_name_qualifier_local_name,
	habitat_area_covered,
	geometry,
	ST_SimplifyPreserveTopology(geometry,10)
from bron.habitat;