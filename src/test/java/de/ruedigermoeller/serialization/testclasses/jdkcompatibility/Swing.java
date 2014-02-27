package de.ruedigermoeller.serialization.testclasses.jdkcompatibility;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 14.11.12
 * Time: 19:59
 * To change this template use File | Settings | File Templates.
 */
public class Swing extends JPanel {

    boolean init = false;
    InnerTest test = new InnerTest();
    int outerInt = 10;

    public void init() {
        if ( ! init ) {
            setLayout( new BorderLayout());
            add( "East", new JButton("East") );
            init = true;
            JEditorPane comp = new JEditorPane();
            comp.setEditorKit(new HTMLEditorKit());
            comp.setText("<html><h1>Heading</h1><p>Some <b>bold</b> things goin on ...</p></html>");
            add("West", comp);
            add( "Center", new JTextField("This is text"));
            add("South", new JColorChooser());
//            add("South", new JLabel("jajajajaja"));
            add("North", new JTree());
        }
    }

    public static void main( String arg[] ) {
        new Swing().showInFrame("test");
    }

    class InnerTest implements Serializable {
        class InnerInnerTest implements Serializable {
            private void readObject(ObjectInputStream s)
                    throws IOException, ClassNotFoundException
            {
                s.defaultReadObject();
                outerInt = 101;
            }
        }

//        Object inin = new InnerInnerTest(); JDK fails on this also ...

        private void readObject(ObjectInputStream s)
                throws IOException, ClassNotFoundException
        {
            s.defaultReadObject();
            outerInt = 101;
        }
    }

    public void showInFrame( String title ) {
        JFrame jFrame = new JFrame();
        jFrame.getContentPane().setLayout(new BorderLayout());
        jFrame.getContentPane().add("Center", this);
        init();
        jFrame.pack();
        jFrame.setTitle(title);
        jFrame.setVisible(true);
    }
}
