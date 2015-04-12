package cn.vimer.kpush;

/**
 * Created by dantezhu on 15-4-13.
 */
public class Proto {
    public static final int CMD_REGISTER = 1;
    public static final int CMD_LOGIN = 2;
    public static final int CMD_HEARTBEAT = 3;
    public static final int CMD_SET_ALIAS_AND_TAGS = 4;

    public static final int EVT_NOTIFICATION = 1000;
    public static final int EVT_MESSAGE = 1001;
}