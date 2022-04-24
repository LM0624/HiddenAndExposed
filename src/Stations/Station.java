package Stations;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Station {
    public int port;            //所在端口
    public int location_x;        //所在位置(横坐标)
    public int location_y;        //所在位置(横纵坐标)
    public Date lastBusyDate;   //繁忙期限(即过了这个时间就是空闲状态)
    public int range;           //广播范围
    public int state = 0;       //0 正常-空闲，1 正常-繁忙，2 隐蔽站节点，3 暴露站节点
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

    public Station(int port, int location_x,int location_y, Date lastBusyDate, int range) {
        this.port = port;
        this.location_x = location_x;
        this.location_y = location_y;
        this.lastBusyDate = lastBusyDate;
        this.range = range;
    }

    public Station(int port) {
        this.port = port;
    }

    public void setLastBusyDate(Date lastBusyDate) {
        this.lastBusyDate = lastBusyDate;
    }
    public boolean isBusy() {

        System.out.println(lastBusyDate.getTime()+","+System.currentTimeMillis());
        return lastBusyDate.getTime() > System.currentTimeMillis();
    }
    public int getState() {
        //返回0-空闲；1-繁忙；2-隐蔽站；3-暴露站
        if (state == 0) {
            if (isBusy()) {
                return 1;
            }
            return 0;
        }
        return state;
    }
    public void printState() {
        Main.showConsole("所在端口： " + port);
        Main.showConsole("所在位置： " + location_x + "," + location_y);
        Main.showConsole("状态编号： " + state);
        if (isBusy()) {
            Main.showConsole("繁忙状态： " + "繁忙    (持续至" + sdf.format(lastBusyDate) + ")");
        } else {
            Main.showConsole("繁忙状态： " + "空闲");
        }
        Main.showConsole("广播范围： " + range);
    }
}
