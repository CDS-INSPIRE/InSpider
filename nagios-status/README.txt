==================================
    Nagios Status server
==================================
De Nagios status server checkt regelmatig de status.dat file van Nagios.
De inhoud daarvan wordt geparsed en via RMI worden HostStatusNode en ServiceStatusNode naar buiten gebracht.

Bij het bouwen van dit project wordt de nagios-status-<VERSION>-server.jar in /target geplaatst.

Opstarten van de Status server op dezelfde machine waar de Nagios status.dat file staat:
sudo -u nagios java -jar -Djava.rmi.server.hostname=repos.idgis.eu -DCONFIGDIR=/etc/cds-inspire/configdir nagios-status-0.0.1-SNAPSHOT-server.jar -f /var/cache/nagios3/status.dat -H inspire-host-a,inspire-host-b -s Aborted ETL jobs,Availability of updates,CPU usage,Current Load,Database replication,Disk Space,ETL Job age,Memory usage,Service JARS,WFS availability,WMS availability,WMS performance
                                                    ==============
Opstart argumenten:
--------------------
-f <locatie status.dat>
-H komma gescheiden lijst van Nagios hostnodes  
-s komma gescheiden lijst van Nagios services

Config dir
-----------
zorg dat de file monitoring.properties via argument -DCONFIGDIR bereikbaar is.
Zie cds-parent subproject resources
De volgende properties zijn van belang voor de rmi server resp rmi client (de admin applicatie): 
nagiosStatusRegistryPort=8765  ## de poort voor rmi Registry van de nagiosStatus server
nagiosStatusServiceUrl=rmi://repos.idgis.eu:8765/nagios-status ## adres tbv rmi client
                             ============== 
!! Let op dat host in nagiosStatusServiceUrl klopt met -Djava.rmi.server.hostname. !!


Gebruik van shell script
-------------------
-rwxr-xr-x 1 idgis idgis 437 2012-02-17 15:29 startNagiosRMI.sh*
#!/bin/sh
java -jar -Djava.rmi.server.hostname=repos.idgis.eu -DCONFIGDIR=/etc/cds-inspire/configdir nagios-status-0.0.1-SNAPSHOT-server.jar -f /var/cache/nagios3/status.dat -H inspire-host-a,inspire-host-b -s "Aborted ETL jobs","Availability of updates","CPU usage","Current Load","Database replication","Disk Space","ETL Job age","Memory usage","Service JARS","WFS availability","WMS availability","WMS performance"  >> /dev/null &

start als:
sudo -u nagios ./startNagiosRMI.sh


TODO:

- Hoe wordt de nagios status server opnieuw opgestart bij uitval proces of machine. 
  Zelfde mechanisme als op de inspire host machines?
  Ook laten monitoren door Nagios?

