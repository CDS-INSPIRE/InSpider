--
-- RiskZone
--
-- Names must match constants defined corresponding ThemeConfig
insert into manager.thema (id, naam) values ((select nextval('manager.hibernate_sequence')), 'RiskZone');
	
insert into manager.datasettype (id, naam, thema_id) values ((select nextval('manager.hibernate_sequence')), 'ROR Risicogebied', (select id from manager.thema t where t.naam = 'RiskZone'));

insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), 'ror', 'Richtlijn Overstromingsrisico''s ', '', '', '', 'Richtlijn Overstromingsrisico''s', 'inspire@gbo-provincies.nl', '', 'ror' where not exists (select * from manager.bronhouder where code = 'ror');

insert into manager.themabronhouderauthorization (thema_id, bronhouder_id) values
	( (select id from manager.thema where naam = 'RiskZone'), (select id from manager.bronhouder where code = 'ror') );
--
