--
-- ProductionInstallation
--
-- Names must match constants defined corresponding ThemeConfig
insert into manager.thema (id, naam) values ((select nextval('manager.hibernate_sequence')), 'ProductionInstallation');
	
insert into manager.datasettype (id, naam, thema_id) values ((select nextval('manager.hibernate_sequence')), 'lgr-installaties', (select id from manager.thema t where t.naam = 'ProductionInstallation'));
insert into manager.datasettype (id, naam, thema_id) values ((select nextval('manager.hibernate_sequence')), 'rk-installaties', (select id from manager.thema t where t.naam = 'ProductionInstallation'));	

insert into manager.bronhouder (id, code, contact_naam, contact_adres, contact_postcode, contact_plaats, naam, contact_emailadres, contact_telefoonnummer, common_name) select nextval('manager.hibernate_sequence'), 'lgr', 'LandelijkGrondwaterRegister', '', '', '', 'LandelijkGrondwaterRegister', 'inspire@gbo-provincies.nl', '', 'landelijkgrondwaterregister' where not exists (select * from manager.bronhouder where code = 'lgr');	
	
insert into manager.bronhouderthema (thema_id, bronhouder_id) values
	( (select id from manager.thema where naam = 'ProductionInstallation'), (select id from manager.bronhouder where code = 'lgr') )
	;
--
