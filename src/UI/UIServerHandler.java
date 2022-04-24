package UI;

import Messages.Message;
import Stations.Main;
import Stations.Station;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UIServerHandler implements Runnable {
    private Socket socket;
    private SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");
    public int[] xCord;
    public int[] yCord;

    public UIServerHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            boolean quitFlag = false;
            while (!quitFlag) {
                String nowTime = sdf.format(new Date());
                String getWord = bufferedReader.readLine();
                Message getMsg = Message.parse(getWord);    //从得到的 getWord 中解析出 Message
                switch (getMsg.type) {

                    case COORDINATE:
                        Main.view.setVisible(false);
                        Main.view.remove(Main.panel);
                        Main.panel.stations = Main.stations;
                        for (Station s : Main.panel.stations) {
                            if (s.port == getMsg.fromPort) {
                                s.location_x = Integer.parseInt(getMsg.text.split(",")[0]);
                                s.location_y = Integer.parseInt(getMsg.text.split(",")[1]);
                            }
                        }
                        Main.view.add(Main.panel);
                        Main.view.setVisible(true);
                        break;
                    case HIDE:
                        Main.view.setVisible(false);
                        Main.view.remove(Main.panel);
                        Main.panel.hide = getMsg.fromPort;
                        Main.view.add(Main.panel);
                        Main.view.setVisible(true);
                        break;
                    case EXPOSE:
                        Main.view.setVisible(false);
                        Main.view.remove(Main.panel);
                        Main.panel.expose = getMsg.fromPort;
                        Main.view.add(Main.panel);
                        Main.view.setVisible(true);
                        break;
                    case RESET:
                        Main.view.setVisible(false);
                        Main.view.remove(Main.panel);
                        Main.panel.hide = 0;
                        Main.panel.expose = 0;
                        Main.view.add(Main.panel);
                        Main.view.setVisible(true);
                    default:
                        quitFlag = true;
                        break;
                }
                String echoWord = "ACK";
                dataOutputStream.writeBytes(echoWord + System.getProperty("line.separator"));
            }
            bufferedReader.close();
            // 关闭包装类，会自动关闭包装类中所包装的底层类。所以不用调用ips.close()
            dataOutputStream.close();
            socket.close();
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }
}
