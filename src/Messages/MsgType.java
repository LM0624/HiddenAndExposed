package Messages;

public enum MsgType {
    PROBE,      //探查消息：接收端会发回当前站点的端口、繁忙时间等消息
    SILENT,     //静默消息：接收方无需作出回应、也无需设置繁忙时间的消息
    NORMAL,     //普通消息：接收方对消息作出回应，并根据消息携带的繁忙时间更新自身的繁忙时间
    QUIT,       //退出消息：接收方接收后断开与发送方的链接
    RTS,         //RTS，预约信道
    COORDINATE,   //向图形界面传输坐标
    HIDE,           //隐蔽站，发送给图形界面
    EXPOSE,         //暴露站，发送给图形界面
    RESET,          //重置，发送给图形界面
}
