package UI;

import Stations.Station;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class UI extends JFrame {

    public static UIServer server;
    public static int port;
    public static ArrayList<Station> stations = new ArrayList<>();  //其他站点的信息表

    public UI(int port)
    {
        this.port=port;
        server=new UIServer(port);
        setSize(800, 800);
        new Thread(server).start();
    }

    public UI()
    {
    }

    public static class newPanel extends JPanel {

        public static ArrayList<Station> stations = new ArrayList<>();  //其他站点的信息表
        int hide;
        int expose;
        public void paint(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D)g;
            g2d.setStroke(new BasicStroke(3f));
            g2d.setFont(getFont().deriveFont(Font.ITALIC, 20f));
            g2d.setBackground(Color.GRAY);
            for (Station s : stations)
            {
                int radius = s.range*25;
                int xCircle=s.location_x*25+radius;
                int yCircle=s.location_y*25+radius;
                switch (s.port)
                {
                    case 8081:
                        if(hide==8081)
                        {
                            g2d.setColor(Color.RED);
                            g2d.drawString("A(H)",xCircle,yCircle);
                        }
                        else if(expose==8081)
                        {
                            g2d.setColor(Color.RED);
                            g2d.drawString("A(E)",xCircle,yCircle);
                        }
                        else
                        {
                            g2d.setColor(Color.BLUE);
                            g2d.drawChars(new char[]{'A'},0,1,xCircle,yCircle);
                        }
                        g2d.drawOval(s.location_x*25,s.location_y*25,s.range*50,s.range*50);
                        break;
                    case 8082:
                        if(hide==8082)
                        {
                            g2d.setColor(Color.RED);
                            g2d.drawString("B(H)",xCircle,yCircle);
                        }
                        else if(expose==8082)
                        {
                            g2d.setColor(Color.RED);
                            g2d.drawString("B(E)",xCircle,yCircle);
                        }
                        else
                        {
                            g2d.setColor(Color.MAGENTA);
                            g2d.drawChars(new char[]{'B'},0,1,xCircle,yCircle);
                        }
                        g2d.drawOval(s.location_x*25,s.location_y*25,s.range*50,s.range*50);
                        break;
                    case 8083:
                        if(hide==8083)
                        {
                            g2d.setColor(Color.RED);
                            g2d.drawString("C(H)",xCircle,yCircle);
                        }
                        else if(expose==8083)
                        {
                            g2d.setColor(Color.RED);
                            g2d.drawString("C(E)",xCircle,yCircle);
                        }
                        else
                        {
                            g2d.setColor(Color.GREEN);
                            g2d.drawChars(new char[]{'C'},0,1,xCircle,yCircle);
                        }
                        g2d.drawOval(s.location_x*25,s.location_y*25,s.range*50,s.range*50);
                        break;
                    case 8084:
                        if(hide==8084)
                        {
                            g2d.setColor(Color.RED);
                            g2d.drawString("D(H)",xCircle,yCircle);
                        }
                        else if(expose==8084)
                        {
                            g2d.setColor(Color.RED);
                            g2d.drawString("D(E)",xCircle,yCircle);
                        }
                        else
                        {
                            g2d.setColor(Color.BLACK);
                            g2d.drawChars(new char[]{'D'},0,1,xCircle,yCircle);
                        }
                        g2d.drawOval(s.location_x*25,s.location_y*25,s.range*50,s.range*50);
                        break;
                }
            }
        }
    }

    public static void main(String[] args) {
        int[]x=new int[]{1,2,3,4};
        int[]y=new int[]{1,2,3,4};
        JFrame f = new UI();
        f.setTitle("Test");
        f.setSize(800, 800);
        f.setVisible(true);
        f.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
