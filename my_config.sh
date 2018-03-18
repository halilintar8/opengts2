#!/bin/bash
#create config from jinja templates

j2  $GTS_HOME/build.properties.j2 > $GTS_HOME/build.properties
j2  $GTS_HOME/config.conf.j2 > $GTS_HOME/config.conf

# prepare compilation for Tomcat-8
# $GTS_HOME/src/org/opengts/war/tools/BufferedHttpServletResponse.java
#        // -- Comment for Tomcat-7, uncomment for Tomcat-8
#        //**/ public void setWriteListener(WriteListener wl) {/*NO-OP*/}

sed -i 's/\/\/\*\*\/ public void setWriteListener(WriteListener wl) {\/\*NO-OP\*\/}/public void setWriteListener(WriteListener wl) {\/\*NO-OP\*\/}/g' $GTS_HOME/src/org/opengts/war/tools/BufferedHttpServletResponse.java


#vi ./src/org/opengts/war/maps/jsmap/OpenLayers.java
#boolean seaOverlay = rtp.getBoolean(PROP_OPENSEAMAP_showOverlay,true);

sed -i 's/boolean seaOverlay = rtp.getBoolean(PROP_OPENSEAMAP_showOverlay,false)/boolean seaOverlay = rtp.getBoolean(PROP_OPENSEAMAP_showOverlay,true)/g' $GTS_HOME/src/org/opengts/war/maps/jsmap/OpenLayers.java


cd $GTS_HOME; ant all
cp $GTS_HOME/build/*.war $CATALINA_HOME/webapps/

#update database
until
    bin/dbAdmin.pl -user=gts -tables=tca
do
 printf "."
 sleep 20
done
# create sysadmin account
$GTS_HOME/bin/admin.pl Account -account=sysadmin -pass=$SYSADMIN_PASSWORD -create;

apt-get update && apt-get upgrade -y && apt-get dist-upgrade -y && apt-get autoremove -y
apt install vim -y
cd $GTS_HOME; bash sampleData/loadSampleData.sh
ant all && ant track.deploy && ant gprmc.deploy && ant events.deploy

