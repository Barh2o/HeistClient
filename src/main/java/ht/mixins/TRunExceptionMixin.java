package ht.heist.mixins;

import ht.heist.Logger;
import ht.heist.injector.annotations.Disable;
import ht.heist.injector.annotations.Mixin;
import ht.heist.util.ExceptionUtil;
import ht.heist.util.StackTraceUtil;

@Mixin("Client")
public class TRunExceptionMixin {
//    @Disable("RunException_sendStackTrace")
//    public static boolean sendStackTrace(String message, Throwable throwable) {
//        if(throwable != null)
//        {
//            Logger.error(message);
//            Logger.error(ExceptionUtil.formatException(throwable));
//        }
//        return false;
//    }

    @Disable("newRunException")
    public static boolean newRunException(Throwable throwable, String message) {
        if((message != null && message.equals("be.av()")) || throwable instanceof NullPointerException)
            return true;

        if(throwable != null)
        {
            Logger.error(message);
            Logger.error(ExceptionUtil.formatException(throwable));
            System.err.println(StackTraceUtil.getStackTrace("CLIENT_ERROR"));
        }
        return false;
    }
}
