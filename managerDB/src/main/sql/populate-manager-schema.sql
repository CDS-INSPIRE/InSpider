-- INITIALIZE INSPIRE DB manager schema
delete from manager.joblog;
delete from manager.etljob;
delete from manager.job;
delete from manager.jobtype;
delete from manager.dataset;
delete from manager.datasetType;
delete from manager.bronhouder;
delete from manager.thema;

insert into manager.jobtype (id, naam, prioriteit) select nextval('manager.hibernate_sequence'), 'VALIDATE', '300';
insert into manager.jobtype (id, naam, prioriteit) select nextval('manager.hibernate_sequence'), 'REMOVE', '200';
insert into manager.jobtype (id, naam, prioriteit) select nextval('manager.hibernate_sequence'), 'IMPORT', '200';
insert into manager.jobtype (id, naam, prioriteit) select nextval('manager.hibernate_sequence'), 'TRANSFORM', '100';
