--
-- WatercourseLink
--
-- Names must match constants defined corresponding ThemeConfig
insert into manager.thema (id, naam) values ((select nextval('manager.hibernate_sequence')), 'WatercourseLink');
	
insert into manager.datasettype (id, naam, thema_id) values ((select nextval('manager.hibernate_sequence')), 'WatercourseLinksNL', (select id from manager.thema t where t.naam = 'WatercourseLink'));

insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), 'elf', 'ELF Project', '', '', '', 'ELF Project', 'inspire@idgis.nl', '', 'elfproject' where not exists (select * from manager.bronhouder where code = 'elf');

insert into manager.themabronhouderauthorization (thema_id, bronhouder_id) values ( (select id from manager.thema where naam = 'WatercourseLink'), (select id from manager.bronhouder where code = 'elf') );
--
