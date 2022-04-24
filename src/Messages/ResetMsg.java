package Messages;

public class ResetMsg extends Message{

    public MsgType type = MsgType.RESET;

    public ResetMsg(int fromPort, int toPort, MsgType type, String text) {
        super(fromPort, toPort, type, text);
    }
}
