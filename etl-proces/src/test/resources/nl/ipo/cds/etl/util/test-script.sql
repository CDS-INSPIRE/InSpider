-- test script

select * from inspire.protected_site 
limit 10;

select * from bron.protected_site where id = 100;

select * 
from inspire.site_name 
where site_name = 'Bla;Bla';

update inspire.job
set status = 'CREATED';