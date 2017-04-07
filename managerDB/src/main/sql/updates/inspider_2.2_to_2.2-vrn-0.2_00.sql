
-- Authorization changes.
ALTER TABLE manager.themabronhouderauthorization RENAME TO BronhouderThema;

create table manager.gebruiker (
    gebruikersnaam varchar(255) not null,
    superuser bool not null,
    primary key (gebruikersnaam)
);


create table manager.GebruikerThemaAutorisatie (
    typeGebruik int4 not null,
    gebruiker_gebruikersnaam varchar(255) not null,
    bronhouderThema_thema_id int8 not null,
    bronhouderThema_bronhouder_id int8 not null,
    primary key (gebruiker_gebruikersnaam, bronhouderThema_thema_id, bronhouderThema_bronhouder_id)
);



alter table manager.GebruikerThemaAutorisatie
    add constraint GebruikerThemaAutorisatie_Gebruiker
    foreign key (gebruiker_gebruikersnaam)
    references manager.gebruiker;

  alter table manager.GebruikerThemaAutorisatie
      add constraint GebruikerThemaAutorisatie_BronhouderThema
      foreign key (bronhouderThema_thema_id, bronhouderThema_bronhouder_id)
      references manager.BronhouderThema;

-- Add parameters column to etljob for storing job parameters (used by TagJob for example).
ALTER TABLE manager.etljob ADD COLUMN parameters TEXT;

insert into manager.jobtype (id, naam, prioriteit) select nextval('manager.hibernate_sequence'), 'TAG', '200';
