package de.ruedigermoeller.serialization.testclasses;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

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
