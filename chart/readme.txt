https://sourceforge.net/p/opengts/discussion/579834/thread/58f8ffdf/

1. Assum that we already have some thing to draw a chart: Speed, BatteryVolts, Temperature, analog inputs, digital inputs or something in Event Detail Report. In this example I use Speed and Altitude.

2. Download jfreechart-1.0.13.jar and jcommon-1.0.16.jar, copy these 2 files into C:\OpenGTS_2.3.8\lib and into TomCat lib folder C:\Program Files\Apache Software Foundation\Tomcat 7.0\lib. Restart TomCat.
/usr/local/gts/lib/
/usr/local/tomcat/lib/

3. In file EventDetailReport.java add getSupportsGraphDisplay() method, you can put at the end of this file, just before } character. This is to enable the link to Graph

    public boolean getSupportsGraphDisplay()    //add by vtt
            {
          return true;
            }

find /usr/local/gts/ -name "EventDetailReport.java"
/usr/local/gts/src/org/opengts/war/report/event/EventDetailReport.java


4. In file ReportDisplay.java  modify one line of method writePage.
Line

this._writeReportGraph(response, reqState, report, i18n);

should be modify to
                //this._writeReportGraph(response, reqState, reportDta, i18n);
                EventChart._writeReportGraph(response, reqState, reportDta, i18nn);

find /usr/local/gts/ -name "ReportDisplay.java"
/usr/local/gts/src/org/opengts/war/track/page/ReportDisplay.java


5. In C:\OpenGTS_2.3.8\src\org\opengts\war\track\page create new file  EventChart.java, codes below:

mkdir /usr/local/tomcat/webapps/track/gtsecharts



//grant permission to opengts chart temp storage location
grant  {
    permission java.io.FilePermission "${catalina.home}/webapps/gtsecharts/-","read,write,delete";
};


vi /usr/local/tomcat/conf/catalina.policy

grant codeBase "file:${catalina.home}/webapps/manager/-" {
    permission java.lang.RuntimePermission "accessClassInPackage.org.apache.catalina";
    permission java.lang.RuntimePermission "accessClassInPackage.org.apache.catalina.ha.session";
    permission java.lang.RuntimePermission "accessClassInPackage.org.apache.catalina.manager";
    permission java.lang.RuntimePermission "accessClassInPackage.org.apache.catalina.manager.util";
    permission java.lang.RuntimePermission "accessClassInPackage.org.apache.catalina.util";
    permission org.apache.catalina.security.DeployXmlPermission "manager";
    permission java.io.FilePermission "${catalina.base}/webapps/gtsecharts/-","read,write,delete";

};

bash /usr/local/tomcat/bin/shutdown.sh
bash /usr/local/tomcat/bin/startup.sh
ant all && ant track.deploy && ant gprmc.deploy && ant events.deploy


