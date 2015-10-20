package ru.ds.AndroidHttpServer;

import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Created by dmitrijslobodchikov on 19.10.15.
 */
public class NetUtils {
    public static InetAddress getIPAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress() && (addr instanceof Inet4Address)) {
                        return addr;
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return null;
    }
}
