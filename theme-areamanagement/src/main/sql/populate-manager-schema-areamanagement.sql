--
-- AreaManagement
--
-- Names must match constants defined corresponding ThemeConfig
insert into manager.thema (id, naam) values ((select nextval('manager.hibernate_sequence')), 'AreaManagement');
	
insert into manager.datasettype (id, naam, thema_id) values ((select nextval('manager.hibernate_sequence')), 'GeluidWegen', (select id from manager.thema t where t.naam = 'AreaManagement'));
insert into manager.datasettype (id, naam, thema_id) values ((select nextval('manager.hibernate_sequence')), 'GeluidBedrijvenTerreinen', (select id from manager.thema t where t.naam = 'AreaManagement'));
insert into manager.datasettype (id, naam, thema_id) values ((select nextval('manager.hibernate_sequence')), 'GeluidVliegvelden', (select id from manager.thema t where t.naam = 'AreaManagement'));
insert into manager.datasettype (id, naam, thema_id) values ((select nextval('manager.hibernate_sequence')), 'Ontgrondingen', (select id from manager.thema t where t.naam = 'AreaManagement'));
insert into manager.datasettype (id, naam, thema_id) values ((select nextval('manager.hibernate_sequence')), 'GrondwaterBeschermingsGebieden', (select id from manager.thema t where t.naam = 'AreaManagement'));
insert into manager.datasettype (id, naam, thema_id) values((select nextval('manager.hibernate_sequence')), 'ZwemwaterLocaties', (select id from manager.thema t where t.naam = 'AreaManagement'));

insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), '9920', 'G. Roningen', '', '', '', 'Groningen', 'inspire@idgis.nl', '', 'groningen' where not exists (select * from manager.bronhouder where code = '9920');
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), '9921', 'F. Riesland', '', '', '', 'Friesland', 'inspire@idgis.nl', '', 'friesland'  where not exists (select * from manager.bronhouder where code = '9921');
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), '9922', 'D. Rent', '', '', '', 'Drenthe', 'inspire@idgis.nl', '', 'drenthe' where not exists (select * from manager.bronhouder where code = '9922');
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), '9923', 'O.V. Rijssel', '', '', '', 'Overijssel', 'inspire@idgis.nl', '', 'overijssel'  where not exists (select * from manager.bronhouder where code = '9923');
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), '9924', 'F. Levoland', '', '', '', 'Flevoland', 'inspire@idgis.nl', '', 'flevoland' where not exists (select * from manager.bronhouder where code = '9924');
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), '9925', 'G. Elderland', '', '', '', 'Gelderland', 'inspire@idgis.nl', '', 'gelderland' where not exists (select * from manager.bronhouder where code = '9925');
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), '9926', 'U. Trecht', '', '', '', 'Utrecht', 'inspire@idgis.nl', '', 'utrecht' where not exists (select * from manager.bronhouder where code = '9926');
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), '9927', 'N. Holland', '', '', '', 'Noord-Holland', 'inspire@idgis.nl', '', 'noordholland' where not exists (select * from manager.bronhouder where code = '9927');
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), '9928', 'Z. Holland', '', '', '', 'Zuid-Holland', 'inspire@idgis.nl', '', 'zuidholland' where not exists (select * from manager.bronhouder where code = '9928');
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), '9929', 'Z. Eland', '', '', '', 'Zeeland', 'inspire@idgis.nl', '', 'zeeland' where not exists (select * from manager.bronhouder where code = '9929');
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), '9930', 'N. Brabant', '', '', '', 'Noord-Brabant', 'inspire@idgis.nl', '', 'noordbrabant' where not exists (select * from manager.bronhouder where code = '9930');
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), '9931', 'L. Imburg', '', '', '', 'Limburg', 'inspire@idgis.nl', '', 'limburg' where not exists (select * from manager.bronhouder where code = '9931');
insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), 'zwr', 'ZwemwaterRegister', '', '', '', 'ZwemwaterRegister', 'inspire@gbo-provincies.nl', '', 'zwemwaterregister' where not exists (select * from manager.bronhouder where code = 'zwr');
	
insert into manager.themabronhouderauthorization (thema_id, bronhouder_id) values
	( (select id from manager.thema where naam = 'AreaManagement'), (select id from manager.bronhouder where code = 'zwr') ),
	( (select id from manager.thema where naam = 'AreaManagement'), (select id from manager.bronhouder where code = '9920') ),
	( (select id from manager.thema where naam = 'AreaManagement'), (select id from manager.bronhouder where code = '9921') ),
	( (select id from manager.thema where naam = 'AreaManagement'), (select id from manager.bronhouder where code = '9922') ),
	( (select id from manager.thema where naam = 'AreaManagement'), (select id from manager.bronhouder where code = '9923') ),
	( (select id from manager.thema where naam = 'AreaManagement'), (select id from manager.bronhouder where code = '9924') ),
	( (select id from manager.thema where naam = 'AreaManagement'), (select id from manager.bronhouder where code = '9925') ),
	( (select id from manager.thema where naam = 'AreaManagement'), (select id from manager.bronhouder where code = '9926') ),
	( (select id from manager.thema where naam = 'AreaManagement'), (select id from manager.bronhouder where code = '9927') ),
	( (select id from manager.thema where naam = 'AreaManagement'), (select id from manager.bronhouder where code = '9928') ),
	( (select id from manager.thema where naam = 'AreaManagement'), (select id from manager.bronhouder where code = '9929') ),
	( (select id from manager.thema where naam = 'AreaManagement'), (select id from manager.bronhouder where code = '9930') ),
	( (select id from manager.thema where naam = 'AreaManagement'), (select id from manager.bronhouder where code = '9931') )	
	;
--
	
