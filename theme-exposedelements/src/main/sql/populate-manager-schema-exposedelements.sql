--
-- ExposedElements
--
-- Names must match constants defined corresponding ThemeConfig
insert into manager.thema (id, naam) values ((select nextval('manager.hibernate_sequence')), 'ExposedElements');
	
insert into manager.datasettype (id, naam, thema_id) values ((select nextval('manager.hibernate_sequence')), 'ROR Blootgestelde element', (select id from manager.thema t where t.naam = 'ExposedElements'));

insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), 'ror', 'Richtlijn Overstromingsrisico''s ', '', '', '', 'Richtlijn Overstromingsrisico''s', 'inspire@gbo-provincies.nl', '', 'ror' where not exists (select * from manager.bronhouder where code = 'ror');

insert into manager.bronhouderthema (thema_id, bronhouder_id) values
	( (select id from manager.thema where naam = 'ExposedElements'), (select id from manager.bronhouder where code = 'ror') );
--
