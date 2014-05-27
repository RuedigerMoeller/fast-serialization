package de.ruedigermoeller.serialization.testclasses;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 07.10.13
 * Time: 23:41
 * To change this template use File | Settings | File Templates.
 */
public class AsciiCharter extends HtmlCharter {

    public AsciiCharter(String file) {
        super(file);
    }

    public void openDoc() {
    }

    public void text(String s) {
        s = s.replace("<i>","");
        s = s.replace("</i>","");
        s = s.replace("</p>","");
        s = s.replace("<p>","");
        s = s.replace("<br>","\n");
        s = s.replace("<b>","*");
        s = s.replace("</b>","*");
        out.println(s);
    }

    public void heading(String title) {
        out.println();
        out.println("====================================================================================================".substring(0,title.length()));
        out.println(title);
        out.println("====================================================================================================".substring(0,title.length()));
    }

    public void openChart(String s) {
        s = s.replace("<i>","");
        s = s.replace("</i>","");
        s = s.replace("</p>","");
        s = s.replace("<p>","");
        out.println(s);
        out.println();
    }

    public void chartBar(String text, int value, int div, String cl) {
        color = cl;
        if ( value == 0 ) {
            out.println("- FAILURE - "+text);
            return;
        }
        int i1 = value / div;
        if ( i1 > 130 ) {
            text = "...[bar cut, result is out of scale] "+text;
            i1 = 130;
        }
        for (int i=0; i< i1;i++) {
            out.print("=");
        }
        out.print(" - "+text+" ("+value+")");
        out.println();
    }

    public String getChartColor() {
        return color;
    }

    public void closeChart() {
        out.println();
    }

    public void closeDoc() {
    }

}
