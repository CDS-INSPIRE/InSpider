--
-- HazardArea
--
-- Names must match constants defined corresponding ThemeConfig
insert into manager.thema (id, naam) values ((select nextval('manager.hibernate_sequence')), 'HazardArea');
	
insert into manager.datasettype (id, naam, thema_id) values ((select nextval('manager.hibernate_sequence')), 'Natuurbranden', (select id from manager.thema t where t.naam = 'HazardArea'));
insert into manager.datasettype (id, naam, thema_id) values ((select nextval('manager.hibernate_sequence')), 'ROR Overstroomd gebied', (select id from manager.thema t where t.naam = 'HazardArea'));

insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), 'rk', 'Risicokaart', '', '', '', 'Risicokaart', 'inspire@gbo-provincies.nl', '', 'risicokaart' where not exists (select * from manager.bronhouder where code = 'rk');
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), 'ror', 'Richtlijn Overstromingsrisico''s ', '', '', '', 'Richtlijn Overstromingsrisico''s', 'inspire@gbo-provincies.nl', '', 'ror' where not exists (select * from manager.bronhouder where code = 'ror');

insert into manager.themabronhouderauthorization (thema_id, bronhouder_id) values
	( (select id from manager.thema where naam = 'HazardArea'), (select id from manager.bronhouder where code = 'rk') ),
	( (select id from manager.thema where naam = 'HazardArea'), (select id from manager.bronhouder where code = 'ror') )
	;
--
