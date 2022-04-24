package Stations;

import Messages.*;
import UI.UI;
import worker.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.*;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

public class Main {
    public static int[] DEFAULT_PORT = new int[]{8081, 8082, 8083, 8084};
    public static int VIEW_PORT = 8085;
    public static int[] DEFAULT_LOC_X = new int[]{0, 2, 4, 8};
    public static int[] DEFAULT_LOC_Y = new int[]{0, 0, 0, 0};
    public static Date[] DEFAULT_DATE = new Date[]{new Date(), new Date(), new Date(), new Date()};
    public static int[] DEFAULT_RANGE = new int[]{5, 5, 5, 5};
    public static boolean RTS_OPEN = false;    //RTS功能是否开启
    public static boolean RANDOM_OPEN = false;    //是否允许随机发送状态

    public static Station localStation;     //当前站点
    public static ArrayList<Station> stations = new ArrayList<>();  //其他站点的信息表
    public static Server server;
    public static Clint clint;
    public static JFrame f;
    public static JTextArea consoleArea;
    private static int target;
    private static int min;
    private static int max;
    private static String text = "abc";
    private static long time = 3000;
    public static UI view;
    public static UI.newPanel panel;


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("无效参数！");
            return;
        }
        if (args[0].equals("V")) {
            Station a = new Station(DEFAULT_PORT[0], DEFAULT_LOC_X[0], DEFAULT_LOC_Y[0], DEFAULT_DATE[0], DEFAULT_RANGE[0]);
            Station b = new Station(DEFAULT_PORT[1], DEFAULT_LOC_X[1], DEFAULT_LOC_Y[1], DEFAULT_DATE[1], DEFAULT_RANGE[1]);
            Station c = new Station(DEFAULT_PORT[2], DEFAULT_LOC_X[2], DEFAULT_LOC_Y[2], DEFAULT_DATE[2], DEFAULT_RANGE[2]);
            Station d = new Station(DEFAULT_PORT[3], DEFAULT_LOC_X[3], DEFAULT_LOC_Y[3], DEFAULT_DATE[3], DEFAULT_RANGE[3]);
            Main.stations.add(a);
            Main.stations.add(b);
            Main.stations.add(c);
            Main.stations.add(d);
            view = new UI(VIEW_PORT);


            view.setTitle("Test");
            view.setSize(800, 800);
            panel = new UI.newPanel();
            panel.stations = stations;
            view.setDefaultCloseOperation(EXIT_ON_CLOSE);
        } else {
            configureStation(args);
            //输入指令
            boolean continueInput = true;
            while (continueInput) {
                System.out.print(">>> ");
                Scanner scan = new Scanner(System.in);
                String instruction = scan.next();
                doInstruction(instruction, scan);
            }
        }

    }

    public static void sendHid() {
        clint.send(new HidMsg(localStation.port, VIEW_PORT, MsgType.HIDE, "HID"), VIEW_PORT);
    }

    public static void sendReset() {
        clint.send(new ResetMsg(localStation.port, VIEW_PORT, MsgType.RESET, "RESET"), VIEW_PORT);
    }

    public static void senExpose() {
        clint.send(new ExposeMsg(localStation.port, VIEW_PORT, MsgType.EXPOSE, "EXPOSE"), VIEW_PORT);
    }

    public static void doInstruction(String instruction, Scanner scan) {
        int target, min, max;
        String text;
        long time;
        try {
            switch (instruction) {
                case "send":
                    //指定接收端口，会对所有范围内的站点广播
                    //send 8081 abc 5000
                    target = scan.nextInt();
                    text = scan.next();
                    time = scan.nextLong();
                    if (RTS_OPEN) {
                        sendWithRTS(target, text, time);
                    } else {
                        sendNormal(target, text, time);
                    }
                    break;
                case "sendRandom":
                    //指定端口，间隔随机时间发送一次数据
                    //sendRandom 8081 abc 2000 2000 5000
                    target = scan.nextInt();
                    text = scan.next();
                    time = scan.nextLong();
                    min = scan.nextInt();
                    max = scan.nextInt();
                    RANDOM_OPEN = true;
                    //另开线程去执行发送循环
                    new Thread(() -> {
                        if (RTS_OPEN) {
                            sendWithRTSRandom(target, text, time, min, max);
                        } else {
                            sendRandom(target, text, time, min, max);
                        }
                    }).start();
                    break;
                case "stop":
                    if (localStation.state >= 2) {
                        localStation.state = 0;
                    }
                    RANDOM_OPEN = false;
                    break;
                case "state":
                    //打印当前站点的信息
                    localStation.printState();
                    break;
                case "RTS":
                    if (RTS_OPEN) {
                        RTS_OPEN = false;
                        showConsole("关闭RTS");
                    } else {
                        RTS_OPEN = true;
                        showConsole("开启RTS");
                    }
                    break;
                case "cord":
                    sendCord();
                    break;
                default:
                    System.out.println("非法指令，请重新输入！");
                    break;
            }
        } catch (InputMismatchException e) {
            System.out.println("非法指令，请重新输入！");
        }
    }

    public static void doInstruction(String instruction) {
        try {
            switch (instruction) {
                case "cord":
                    sendCord();
                    break;
                case "send":
                    //指定接收端口，会对所有范围内的站点广播
                    //send 8081 abc 5000
                    if (RTS_OPEN) {
                        sendWithRTS(target, text, time);
                    } else {
                        sendNormal(target, text, time);
                    }
                    break;
                case "state":
                    //打印当前站点的信息
                    localStation.printState();
                    break;
                case "RTS":
                    if (RTS_OPEN) {
                        RTS_OPEN = false;
                        showConsole("关闭RTS");
                    } else {
                        RTS_OPEN = true;
                        showConsole("开启RTS");
                    }
                    break;
                default:
                    System.out.println("非法指令，请重新输入！");
                    break;
            }
        } catch (InputMismatchException e) {
            System.out.println("非法指令，请重新输入！");
        }
    }

    public static void showConsole(String text) {

        System.out.println(text);
        Main.consoleArea.append(text + "\n");
    }

    /***
     * 广播发送，但是只有指定的端口会处理信息，其他的会更新信道繁忙时间
     * @param target    目标端口
     * @param text    发送文本
     * @param busyTimeMillis    占用信道持续时间
     * @return 是否发送成功
     */
    public static boolean sendNormal(int target, String text, long busyTimeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");
        if (Main.localStation.isBusy()) {
            String nowTime = sdf.format(new Date());
            showConsole(nowTime + " | 信道正忙，不发送消息");
            //检测一下目标真实信道状态
            String realState = clint.send(new ProbeMsg(Main.localStation.port, target, MsgType.PROBE, text), target);
            if (realState.equals("0")) {
                showConsole(nowTime + " | " + target + " 实际上并没有繁忙，发生暴露站问题");
                Main.senExpose();
                Main.localStation.state = 3;
            }
            return false;
        } else {
            for (Station station : Main.stations) {
                if (Math.pow((station.location_x - Main.localStation.location_x), 2) +
                        Math.pow((station.location_y - Main.localStation.location_y), 2) >
                        Math.pow(Main.localStation.range, 2)) {
                    continue;   //如果所选的站点在广播范围外，则不对他发送
                }
                clint.send(new NormalMsg(Main.localStation.port, target, MsgType.NORMAL, text, busyTimeMillis), station.port);
                if (target != station.port) {
                    station.state = 3;
                }
            }
            return true;
        }
    }


    /***
     * RTS发送：先发RTS预约信道，预约成功再发信息
     * @param target    目标端口
     * @param text    发送文本
     * @param busyTimeMillis    占用信道持续时间
     * @return 是否发送成功
     */
    public static boolean sendWithRTS(int target, String text, long busyTimeMillis) {
        int timeout = 500;
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");
        RTSMsg rtsMsg = new RTSMsg(Main.localStation.port, target, MsgType.RTS, text, timeout);
        Future<String> echoFuture = sendRTS(rtsMsg, target);
        try {
            String echoWord = echoFuture.get(timeout, TimeUnit.MILLISECONDS);   //在规定时间内接收消息
            if (echoWord.equals("CTS")) {
                //如果收到回复 CTS ，则发送接下来的信息
                String nowTime = sdf.format(new Date());
                showConsole(nowTime + " | [" + Main.localStation.port + "收到回复CTS，继续发送数据帧");
                NormalMsg normalMsg = new NormalMsg(Main.localStation.port, target, MsgType.NORMAL, text, busyTimeMillis);
                clint.send(normalMsg, target);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            //超出时间，表明信道预约失败
            String nowTime = sdf.format(new Date());
            showConsole(nowTime + " | [" + Main.localStation.port + "] RTS预约失败：服务端无回复");
            clint.close();
            return false;
        }
    }

    public static void sendWithRTSRandom(int target, String text, long busyTimeMillis, int min, int max) {
        int timeout = 500;
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");
        RTSMsg rtsMsg = new RTSMsg(Main.localStation.port, target, MsgType.RTS, text, timeout);
        while (RANDOM_OPEN) {
            double random = Math.random() * (max - min) + min;  //min~max秒的随机发送间隔
            try {
                Thread.sleep(Math.round(random));
            } catch (Exception e) {
                e.printStackTrace();
            }

            Future<String> echoFuture = sendRTS(rtsMsg, target);    //发一个RTS
            try {
                String echoWord = echoFuture.get(timeout, TimeUnit.MILLISECONDS);   //在规定时间内接收消息
                if (echoWord.equals("CTS")) {
                    //如果收到回复 CTS ，则发送接下来的信息
                    NormalMsg normalMsg = new NormalMsg(Main.localStation.port, target, MsgType.NORMAL, text, busyTimeMillis);
                    clint.send(normalMsg, target);
                } else {
                    continue;
                }
            } catch (Exception e) {
                //超出时间，表明信道预约失败
                String nowTime = sdf.format(new Date());
                System.out.println(nowTime + " | [" + Main.localStation.port + "] RTS预约失败：服务端无回复");
                clint.close();  //手动关闭一下clint
                continue;
            }
        }
        System.out.println("随机发送结束");
    }

    public static Future<String> sendRTS(Message msg, int target) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(() -> {
            return clint.send(msg, target);
        });
    }

    public static void sendRandom(int target, String text, long busyTimeMillis, int min, int max) {
        while (RANDOM_OPEN) {
            sendNormal(target, text, busyTimeMillis);
            double random = Math.random() * (max - min) + min;  //min~max秒的随机发送间隔
            try {
                Thread.sleep(Math.round(random));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("随机发送结束");
    }

    public static void sendCord() {
        for (Station s : stations) {
            clint.send(new CorMsg(server.port, s.port, MsgType.COORDINATE,
                    localStation.location_x + "," + localStation.location_y), s.port);
        }
        clint.send(new CorMsg(server.port, VIEW_PORT, MsgType.COORDINATE,
                localStation.location_x + "," + localStation.location_y), VIEW_PORT);
    }

    public static void configureStation(String[] args) {
        Station a = new Station(DEFAULT_PORT[0], DEFAULT_LOC_X[0], DEFAULT_LOC_Y[0], DEFAULT_DATE[0], DEFAULT_RANGE[0]);
        Station b = new Station(DEFAULT_PORT[1], DEFAULT_LOC_X[1], DEFAULT_LOC_Y[1], DEFAULT_DATE[1], DEFAULT_RANGE[1]);
        Station c = new Station(DEFAULT_PORT[2], DEFAULT_LOC_X[2], DEFAULT_LOC_Y[2], DEFAULT_DATE[2], DEFAULT_RANGE[2]);
        Station d = new Station(DEFAULT_PORT[3], DEFAULT_LOC_X[3], DEFAULT_LOC_Y[3], DEFAULT_DATE[3], DEFAULT_RANGE[3]);
        Main.stations.add(a);
        Main.stations.add(b);
        Main.stations.add(c);
        Main.stations.add(d);

        //设置前端界面
        f = new JFrame();
        f.setTitle(args[0]);
        f.setSize(500, 500);
        f.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        JLabel XLabel = new JLabel();
        XLabel.setText("X:");
        XLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 15));
        JTextField XText = new JTextField();
        XText.setPreferredSize(new Dimension(100, 30));

        JLabel YLabel = new JLabel();
        YLabel.setText("Y:");
        YLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 15));
        JTextField YText = new JTextField();
        YText.setPreferredSize(new Dimension(100, 30));

        consoleArea = new JTextArea();
        consoleArea.setLineWrap(true);
        consoleArea.setPreferredSize(new Dimension(400, 300));

        JButton confirmBtn = new JButton("确认");
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取到的事件源就是按钮本身
                // JButton btn = (JButton) e.getSource();

                System.out.println("按钮被点击");
                localStation.location_x = Integer.parseInt(XText.getText());
                localStation.location_y = Integer.parseInt(YText.getText());
                doInstruction("cord");
            }
        });


        JLabel localPortLabel = new JLabel();
        localPortLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 15));
        JLabel portLabel = new JLabel();
        portLabel.setText("发送端口:");
        portLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 15));
        JTextField portText = new JTextField();
        portText.setPreferredSize(new Dimension(100, 30));

        JButton sendBtn = new JButton("发送");
        sendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("发送按钮被点击");
                target = Integer.parseInt(portText.getText());
                doInstruction("send");
            }
        });

        JButton showBtn = new JButton("显示信息");
        showBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doInstruction("state");
            }
        });

        JButton rtsBtn = new JButton("打开RTS");
        rtsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (rtsBtn.getText() == "打开RTS")
                    rtsBtn.setText("关闭RTS");
                else
                    rtsBtn.setText("打开RTS");
                doInstruction("RTS");
            }
        });

        JButton clearBtn = new JButton("重置");
        clearBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取到的事件源就是按钮本身
                // JButton btn = (JButton) e.getSource();
                sendReset();

            }
        });


        f.add(XLabel);
        f.add(XText);
        f.add(YLabel);
        f.add(YText);
        f.add(confirmBtn);
        f.add(localPortLabel);
        f.add(portLabel);
        f.add(portText);
        f.add((sendBtn));
        f.add(showBtn);
        f.add(rtsBtn);
        f.add(clearBtn);
        f.add(consoleArea);


        switch (args[0]) {
            case "A":
                Main.localStation = a;
                Main.stations.remove(0);
                break;
            case "B":
                Main.localStation = b;
                Main.stations.remove(1);
                break;
            case "C":
                Main.localStation = c;
                Main.stations.remove(2);
                break;
            case "D":
                Main.localStation = d;
                Main.stations.remove(3);
                break;
            default:
                break;
        }

        localPortLabel.setText("此端口:" + localStation.port);
        f.setDefaultCloseOperation(EXIT_ON_CLOSE);
        f.setVisible(true);
        Main.server = new Server(Main.localStation.port);
        new Thread(server).start();    //服务线程开启
        Main.clint = new Clint();

    }
}
