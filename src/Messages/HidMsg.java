package Messages;

public class HidMsg extends Message{
    public MsgType type = MsgType.HIDE;

    public HidMsg(int fromPort, int toPort, MsgType type, String text) {
        super(fromPort, toPort, type, text);
    }

}