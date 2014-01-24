insert into inspire.dt_site_protection_classification(site_protection_classification)
select distinct regexp_split_to_table(site_protection_classification, e'\\|') 
from bron.protected_site;

insert into inspire.dt_site_designation(site_designation_schema, site_designation)
with site_designations as (
	select regexp_split_to_array(regexp_split_to_table(site_designation, e'\\|'), ':') site_designation
	from bron.protected_site
), site_designations_split as (
	select site_designation[1] site_designation_schema,
		case when array_length(site_designation, 1) = 1
		then
			site_designation[1]
		else
			site_designation[2]
		end site_designation
	from site_designations
)
select site_designation_schema, site_designation
from site_designations_split
group by site_designation_schema, site_designation;

insert into inspire.protected_site(geometry, legal_foundation_date, legal_foundation_document, 
	inspire_id_namespace, inspire_id_local_id)
select st_reverse(st_forcerhr(geometry)) geometry, legal_foundation_date::timestamp, legal_foundation_document, 
	substring(inspire_id from e'(.*?\\..*?\\..*?)\\.') inspire_id_namespace, 
	substring(inspire_id from e'.*?\\..*?\\..*?\\.(.*$)') inspire_id_local_id
from bron.protected_site;

insert into inspire.site_name(fk_protected_site, site_name)
with site_name as (
	select inspire_id, regexp_split_to_table(nullif(site_name, ''), e'\\|') site_name 
	from bron.protected_site
)
select ips.id, sn.site_name
from site_name sn
join inspire.protected_site ips on ips.inspire_id_namespace || '.' || ips.inspire_id_local_id = sn.inspire_id;

insert into inspire.jt_site_protection_classification(fk_site_protection_classification, fk_protected_site)
with site_protection_classification as (
	select distinct on (inspire_id, site_protection_classification) 
		inspire_id, 
		regexp_split_to_table(site_protection_classification, e'\\|') as site_protection_classification
	from bron.protected_site
)
select ispc.id as fk_site_protection_classification, ips.id as fk_protected_site from site_protection_classification spc
join inspire.dt_site_protection_classification ispc on ispc.site_protection_classification = spc.site_protection_classification
join inspire.protected_site ips on ips.inspire_id_namespace || '.' || ips.inspire_id_local_id = spc.inspire_id;

insert into inspire.jt_site_designation(fk_site_designation, fk_protected_site, percentage_under_designation)
with site_designations as (
	select inspire_id, regexp_split_to_array(regexp_split_to_table(site_designation, e'\\|'), ':') site_designation
	from bron.protected_site
), site_designations_split as (
	select inspire_id, site_designation[1] site_designation_schema,
		case when array_length(site_designation, 1) = 1
		then
			site_designation[1]
		else
			site_designation[2]
		end site_designation,
		case when array_length(site_designation, 1) = 3			
		then
			site_designation[3]			
		else
			'100'
		end percentage_under_designation
	from site_designations
)
select isd.id as fk_site_designation, ips.id as fk_protected_site, percentage_under_designation 
from site_designations_split spc
join inspire.dt_site_designation isd 
	on isd.site_designation = spc.site_designation
	and isd.site_designation_schema = spc.site_designation_schema
join inspire.protected_site ips 
	on ips.inspire_id_namespace || '.' || ips.inspire_id_local_id = spc.inspire_id;

