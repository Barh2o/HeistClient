package ht.heist.mixins;

import ht.heist.Logger;
import ht.heist.injector.annotations.FieldHook;
import ht.heist.injector.annotations.Mixin;
import ht.heist.injector.annotations.Shadow;
import ht.heist.services.proxy.ProxyManager;
import ht.heist.services.proxy.ProxyMetrics;
import net.runelite.api.Client;

import java.io.DataInputStream;
import java.net.*;

@Mixin("Task")
public class TTaskMixin
{
    @Shadow("client")
    public static Client client;

    @Shadow("result")
    public Object result;

    @Shadow("objectArgument")
    public Object objectArgument;

    @FieldHook("result")
    public boolean onResultSet(Object object)
    {
//        if(object instanceof Socket)
//        {
//            try
//            {
//                ProxyMetrics metrics = ProxyManager.getProxy();
//
//                if(metrics == null || metrics.getProxy() == null)
//                    return true;
//
//                System.out.println("[*] Rerouting socket to use proxy");
//
//                Socket socket = (Socket) object;
//
//                Socket newSocket = new Socket(metrics.getProxy());
//                newSocket.connect(new InetSocketAddress(socket.getInetAddress(), socket.getPort()), 10000);
//                socket.close();
//                result = newSocket;
//                Logger.info("[+] Socket<" + socket.getInetAddress() + ":" + socket.getPort() + "> rerouted to use proxy");
//                return false;
//            }
//            catch (Exception e)
//            {
//                Logger.error(e);
//            }
//        }
//        else if(object instanceof DataInputStream)
//        {
//            try
//            {
//                ProxyMetrics metrics = ProxyManager.getProxy();
//
//                if(metrics == null || metrics.getProxy() == null)
//                    return true;
//
//                URL url = (URL) objectArgument;
//                URLConnection connection = url.openConnection(metrics.getProxy());
//                result = new DataInputStream(connection.getInputStream());
//                Logger.info("[+] DataInputStream<" + url + "> rerouted to use proxy");
//                return false;
//            }
//            catch (Exception e)
//            {
//                Logger.error(e);
//            }
//        }
        return true;
    }
}
