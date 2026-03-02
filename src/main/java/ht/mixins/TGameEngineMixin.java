package ht.heist.mixins;

import ht.heist.Static;
import ht.heist.api.TGameEngine;
import ht.heist.data.LoginMessage;
import ht.heist.data.LoginResponse;
import ht.heist.injector.annotations.*;
import org.slf4j.Logger;

@Mixin("GameEngine")
public class TGameEngineMixin implements TGameEngine {
    @Shadow("logger")
    public static Logger logger;

    @Shadow("graphicsGuard")
    public static boolean graphicsGuard;

    @FieldHook("graphicsGuard")
    public boolean onGuardSet(boolean bool) {
        // if(Static.isHeadless())
        // {
        // graphicsGuard = false;
        // return false;
        // }
        return true;
    }

    @Disable("processError")
    public static boolean processError(String message, Throwable error) {
        Throwable var3 = error;
        if (error instanceof Iterable && "".equals(error.getMessage())) {
            var3 = error.getCause();
        }

        if (message == null) {
            logger.error("Client error", var3);
            ht.heist.Logger.warn("Client error: " + var3.getMessage());
        } else {
            logger.error("Client error: {}", message, var3);
            ht.heist.Logger.warn("Client error: " + message);
        }
        return false;
    }

    @MethodHook("getLoginError")
    public static void onGetLoginError(int code) {
        Static.post(LoginResponse.fromIndex(code));
    }

    @MethodHook("setLoginResponse")
    public static void onSetLoginResponse(String line1, String line2, String line3) {
        Static.post(new LoginMessage(line1, line2, line3));
    }
}
