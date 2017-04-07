-- The production database apparently uses TEXT, which causes jobs not being able to be created since the UUID does not fit in their database column.
-- If this statement fails to execute, make sure your do not have manager.dataset.uuid values that are longer than 255 characters.
alter table manager.dataset alter column uuid set data type varchar(255);
