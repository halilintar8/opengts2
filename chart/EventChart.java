package org.opengts.war.track.page;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import javax.servlet.http.HttpServletResponse;
import org.opengts.db.tables.EventData;
import org.opengts.util.HTMLTools;
import org.opengts.util.I18N;
import org.opengts.war.report.*;
import org.opengts.war.tools.CommonServlet;
import org.opengts.war.tools.RequestProperties;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.war.track.*;
import org.opengts.db.tables.EventData;
public class EventChart
{
    public static void _writeReportGraph(HttpServletResponse response, final RequestProperties reqState, final ReportData report, final I18N i18ln)
    throws ReportException, IOException {
    String[] boxName = {"Speed", "Altitude", "BatVolts", "Thermo1", "Fuel%"};       //TODO from report
    String[] param   = reqState.getHttpServletRequest().getParameterValues("param");    
    if (param == null)  {           //first load 
        param = new String[1];
        param[0] = boxName[0];
    }
    Arrays.sort(param);
    String graphURL = TimeChart( report, param, boxName );  //draw a chart, get url

    /* write frame */
    CommonServlet.setResponseContentType(response, HTMLTools.MIME_HTML());
    PrintWriter pw = response.getWriter();    
    // HTML start
    // HTML header
    // HTML body
    pw.write("<body>\n");
    pw.write(" <FORM ACTION=\"\" METHOD=POST>\n");      //form for check boxes
    String boxChecked;
    for(int i = 0 ; i < boxName.length; i++ )  {
        boxChecked = ( Arrays.binarySearch( param, boxName[i]) >= 0 )?  "checked" : "";
        pw.write("  <INPUT TYPE=\"checkbox\" NAME=\"param\" VALUE=\""+ boxName[i]+"\" " + boxChecked+">" + boxName[i]+"\n");
    }
    
    pw.write("  <INPUT TYPE=\"submit\" VALUE=\"Generate Chart\">\n");
    pw.write(" </FORM>\n");  
    pw.write("<img src='../track/gtsecharts/"+graphURL+"'/>\n");
    pw.write("</body>\n");
    
    // HTML end
    pw.write("</html>\n");
    pw.close();
}

    private static String TimeChart(ReportData report, String[] param, String[] boxName )
        throws ReportException, IOException{
        TimeSeries seriesSpeed    = new TimeSeries( boxName[0]);
        TimeSeries seriesAltitude = new TimeSeries( boxName[1]);            
//      TimeSeries seriesBatVolts = new TimeSeries( boxName[2]);     //may not present in your report 
//      TimeSeries seriesThermo1  = new TimeSeries( boxName[3]);       
//      TimeSeries seriesFuel     = new TimeSeries( boxName[4]);
        TimeSeriesCollection dataset0 = new TimeSeriesCollection();
        TimeSeriesCollection dataset1 = new TimeSeriesCollection();
        
        for (DBDataIterator dbi = report.getBodyDataIterator(); dbi.hasNext();) {
            Object ev = dbi.next().getRowObject();
            if (ev instanceof EventData) {
                EventData ed = (EventData)ev;
                FixedMillisecond timestamp = new FixedMillisecond(1000*ed.getTimestamp()); //1000 
                
                seriesSpeed.addOrUpdate   ( timestamp , ed.getSpeedKPH());
                seriesAltitude.addOrUpdate( timestamp , ed.getAltitude());  
//              seriesBatVolts.addOrUpdate( timestamp , ed.getBatteryVolts());
//              seriesThermo1.addOrUpdate ( timestamp , ed.getThermoAverage(0)); 
//              seriesFuel.addOrUpdate  (timestamp, ed.getFuelLevel()); 
           } 
        }       //end of for 
        
        if (Arrays.binarySearch( param, boxName[0])>=0) {   //check if box0 is checked
            dataset0.addSeries(seriesSpeed);
        }                
        if (Arrays.binarySearch( param, boxName[1])>=0) {   //check if box1 is checked
            dataset1.addSeries(seriesAltitude);
        }         
//      if (Arrays.binarySearch( param,boxName[2] )>=0) {   //may not present in your report
//          dataset0.addSeries(seriesBatVolts);
//       }
        
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                report.getReportSubtitle(),             //title
                "",             //x-axis label
                "",             //y-axis label
                dataset0, true, true, true
                );
        XYPlot plot = chart.getXYPlot();
        plot.getRenderer().setSeriesStroke(0, new BasicStroke(2f));   
        plot.getRenderer().setSeriesPaint(0, Color.black);
        
        NumberAxis axis1 = new NumberAxis("");
        axis1.setTickLabelPaint(Color.red);             
        plot.setRangeAxis(1, axis1);                //add 2nd axis
        plot.setDataset(1, dataset1);
        plot.mapDatasetToRangeAxis(1, 1);
        plot.setRenderer(1, new StandardXYItemRenderer());
        plot.getRenderer(1).setSeriesStroke(0, new BasicStroke(3f));   
        String prefix = "/usr/local/tomcat/webapps/track/gtsecharts/";
	    String graphURL = "chart_"+ report.getFirstDeviceID()+".png";        
          
       try {
            File file = new File(prefix + graphURL);
            ChartUtilities.saveChartAsPNG(file, chart, 960, 520);  
       
        } 
        catch (IOException e) {
                Print.logException("Error creating file: "+ prefix+graphURL + " "+e.getLocalizedMessage(), e);
                
        }
       
        return graphURL;
}
}


