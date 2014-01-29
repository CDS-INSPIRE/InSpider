delete from inspire.watercourse_link;

insert into inspire.watercourse_link (
    id,
    job_id,
    gfid,
    inspire_id_namespace,
    inspire_id_local_id,
    name,
    end_node_href,
    start_node_href,    
    geometry
    )
select
    id,
    job_id,
    gfid,
    'NL.' || inspire_id_dataset_code,
    inspire_id_local_id,
    "name",
    '#HY_N_HYDRO_NODE_' || end_node_local_id,
    '#HY_N_HYDRO_NODE_' || start_node_local_id,
    ST_LineMerge(geometry)
from bron.watercourse_link;
