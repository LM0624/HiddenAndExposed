package Messages;

public class ExposeMsg extends Message{
    public MsgType type = MsgType.EXPOSE;

    public ExposeMsg(int fromPort, int toPort, MsgType type, String text) {
        super(fromPort, toPort, type, text);
    }
}
