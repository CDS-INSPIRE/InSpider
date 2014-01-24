-- table bronhouder_geometry
create table manager.bronhouder_geometry (
	bronhouder_id int8 not null,
	primary key (bronhouder_id)
);

alter table manager.bronhouder_geometry
	add constraint bronhouder_geometry_bronhouder_id
	foreign key (bronhouder_id)
	references manager.Bronhouder;
	
select AddGeometryColumn ('manager', 'bronhouder_geometry', 'geom', 28992, 'MULTIPOLYGON', 2);
-- -------------------------