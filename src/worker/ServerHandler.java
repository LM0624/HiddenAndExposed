package worker;

import Messages.*;
import Stations.*;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerHandler implements Runnable {
    private Socket socket;
    private SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");

    public ServerHandler(Socket socket) {
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
                switch(getMsg.type) {
                    case PROBE:
                        int state = Main.localStation.getState();
                        dataOutputStream.writeBytes(state + System.getProperty("line.separator"));
                        break;
                    case SILENT:
                        //静默消息本地显示即可
                        if (getMsg.toPort == Main.localStation.port) {
                            Main.showConsole(nowTime + " | [" + Main.localStation.port + "] 收到来自 " + getMsg.fromPort + " 的信息.");
                        }
                        break;
                    case NORMAL:
                        //普通消息要本地显示，作出回应，设置信道繁忙时间
                        if (getMsg.toPort == Main.localStation.port) {
                            //是发给我的消息，就要检查隐蔽站问题
                            Main.showConsole(nowTime + " | [" + Main.localStation.port + "] 收到来自 " + getMsg.fromPort + " 的信息: " + getMsg.text);
                            if (Main.localStation.isBusy()) {
                                //如果当前信道忙，则发生隐蔽站问题，且中断连接
                                Main.showConsole(nowTime + " | [" + Main.localStation.port + "] 当前信道正忙！ ");
                                Main.showConsole("【发生隐蔽站问题】");
                                Main.sendHid();
                                //回应ERROR
                                String echoWord = "ERROR";
                                //设置站点状态
                                Main.localStation.state = 2;
                                dataOutputStream.writeBytes(echoWord + System.getProperty("line.separator"));
                                quitFlag = true;
                            } else {
                                Main.localStation.setLastBusyDate(((NormalMsg)getMsg).lastBusyDate);    //更新繁忙时间
                                //信道不忙则回应ACK
                                String echoWord = "ACK";
                                dataOutputStream.writeBytes(echoWord + System.getProperty("line.separator"));
                            }
                        } else {
                            Main.localStation.setLastBusyDate(((NormalMsg)getMsg).lastBusyDate);    //更新繁忙时间
                            //不是发给我的消息，回应QUIT
                            String echoWord = "QUIT";
                            dataOutputStream.writeBytes(echoWord + System.getProperty("line.separator"));
                        }
                        break;
                    case RTS:
                        //收到RTS，则预约信道：信道繁忙则不回复，不繁忙则回复CTS
                        if (Main.localStation.isBusy()) {
                            //TODO: 应该在信道忙的时候回复？
                        } else {
                            String echoWord = "CTS";
                            dataOutputStream.writeBytes(echoWord + System.getProperty("line.separator"));
                        }
                        break;
                    case COORDINATE:
                        for (Station s:Main.stations)
                        {
                            if(s.port==getMsg.fromPort)
                            {
                                s.location_x=Integer.parseInt(getMsg.text.split(",")[0]);
                                s.location_y=Integer.parseInt(getMsg.text.split(",")[1]);
                            }
                        }
                        switch (getMsg.fromPort)
                        {
                            case 8081:
                                Main.showConsole("收到A坐标："+getMsg.text);
                                break;
                            case 8082:
                                Main.showConsole("收到B坐标："+getMsg.text);
                                break;
                            case 8083:
                                Main.showConsole("收到C坐标："+getMsg.text);
                                break;
                            case 8084:
                                Main.showConsole("收到D坐标："+getMsg.text);
                                break;
                        }
                        String echoWord = "ACK";
                        dataOutputStream.writeBytes(echoWord + System.getProperty("line.separator"));
                        break;
                    case QUIT:
                        //退出消息则设置退出条件为 true
                        quitFlag = true;
                        break;
                    default:
                        quitFlag = true;
                        break;
                }
            }
            bufferedReader.close();
            // 关闭包装类，会自动关闭包装类中所包装的底层类。所以不用调用ips.close()
            dataOutputStream.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
