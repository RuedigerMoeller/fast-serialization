package de.ruedigermoeller.serialization.testclasses;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 01.12.12
 * Time: 01:18
 * To change this template use File | Settings | File Templates.
 */
public class HtmlCharter {
    protected PrintStream out;
    String color = "#a04040";
    AsciiCharter asc;

    public HtmlCharter(String file) {
        try {
            out = new PrintStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void openDoc() {
        out.println("<html>");
        if ( asc != null )
            asc.openDoc();
    }

    public void text(String s) {
        out.println(s+"<br>");
        if ( asc != null )
            asc.text(s);
    }

    public void heading(String title) {
        out.println("<br><h4>" + title + "</h4>");
        if ( asc != null )
            asc.heading(title);
    }

    public void openChart(String title) {
        out.println("<b>"+title+"</b>");
        out.println("<table border=0 cellpadding=0 cellspacing=0>");
        if ( asc != null )
            asc.openChart(title);
    }

    public void gChart(List<Integer> hivalues,List<Integer> lovalues, String title, String names[]) {
        int chartSize = Math.min(18, hivalues.size() ); // more bars aren't possible with gcharts
        int max = Collections.max( hivalues );
        String res = "https://chart.googleapis.com/chart?cht=bhs&chs=600x"+(chartSize *20+16); // html: finally a device independent technology
//        res+="&chtt="+URLEncoder.encode(title);
        res+="&chd=t:";
        for (int i = 0; i < chartSize; i++) {
            int val = lovalues.get(i);
            res+= val +((i< chartSize -1) ? ",":"|");
        }
        for (int i = 0; i < chartSize; i++) {
            int valLower = lovalues.get(i);
            int val = hivalues.get(i);
            val -= valLower;
            res+=val+((i< chartSize -1) ? ",":"");
        }
        res += "&chco=5d99f9,4d89f9";
        res += "&chdlp=t";
        res += "&chbh=15";
        res += "&chds=0,"+max;
        res += "&chxr=1,0,"+max;
        res += "&chxt=y,x&chxl=0:|";
        for (int i = 0; i < chartSize; i++) {
            res+= URLEncoder.encode(names[chartSize-i-1]+((i< chartSize -1) ? "|":"") );
        }
        out.println( "<b>"+title+"</b><br><img src='"+res+"'/><br>" );
    }
    
    public void chartBar(String text, int value, int div, String cl) {
        color = cl;
        if ( value == 0 ) {
            out.println("<tr><td><font color=red size=2><b>FAILURE</b> "+text+"</font></td></tr>");
            return;
        }
        out.println("<tr><td><font size=1 color="+ getChartColor() +">");
        int i1 = value / div;
        if ( i1 > 130 ) {
            text = "<b>...<font color=red>[bar cut, result is out of scale]</font></b> "+text;
            i1 = 130;
        }
        for (int i=0; i< i1;i++) {
            out.print("&#9608;");
        }
        if ( value%div >= div/2 ) {
            out.print("&#9612;");
        }
        out.print("</font>&nbsp;<font size=2><b>"+text+" ("+value+")</b></font>");
        out.println("</td></tr>");
        if ( asc != null )
            asc.chartBar(text, value, div, cl);
    }

    public String getChartColor() {
        return color;
    }

    public void closeChart() {
        out.println("</table><br>");
        if ( asc != null )
            asc.closeChart();
    }

    public void closeDoc() {
        out.println("</html>");
        if ( asc != null )
            asc.closeDoc();
    }

    public void setAsc(AsciiCharter asc) {
        this.asc = asc;
    }

    public static void main( String arg[] ) {
        HtmlCharter charter = new HtmlCharter("f:\\tmp\\test.html");
        charter.openDoc();
        charter.openChart("Serialization Size" );
        charter.chartBar("FST", 99, 3, "#a04040");
        charter.chartBar("Kry", 100, 3, "#a04040");
        charter.chartBar("Def", 221, 3, "#a04040");
        charter.closeChart();
        charter.closeDoc();
    }

}
