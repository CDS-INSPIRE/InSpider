 #!/bin/sh
cd /tmp/
mkdir inspire-schemas
cd inspire-schemas

wget -r -np -nH --cut-dirs=0 -R index.html* http://inspire.ec.europa.eu/schemas/
wget -r -np -nH --cut-dirs=0 -R index.html* http://inspire.ec.europa.eu/draft-schemas/

# make INSPIRE schema imports relative
find schemas -iname "*.xsd" -exec sed -i 's schemaLocation="http://inspire\.ec\.europa\.eu/schemas/ schemaLocation="../../ g' {} \;

find draft-schemas -iname "*.xsd" -exec sed -i 's schemaLocation="http://inspire\.ec\.europa\.eu/schemas/ schemaLocation="../../../schemas/ g' {} \;
find draft-schemas -iname "*.xsd" -exec sed -i 's schemaLocation="http://inspire\.ec\.europa\.eu/draft-schemas/ schemaLocation="../../ g' {} \;

# remove unnecessary schema imports
find draft-schemas -iname "*.xsd" -exec sed -i 's#<import namespace="http://www\.interactive-instruments\.de/ShapeChange/AppInfo" .*/>##g' {} \;
