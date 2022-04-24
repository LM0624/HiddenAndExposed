package Messages;

public class CorMsg extends Message{
    public MsgType type = MsgType.COORDINATE;

    public CorMsg(int fromPort, int toPort, MsgType type, String text) {
        super(fromPort, toPort, type, text);
    }

}
