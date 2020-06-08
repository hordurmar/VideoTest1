package com.example.videotest1;

import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class Internet {

    static Inet4Address ip() throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        NetworkInterface ni;
        if (nis == null) {
            return ((Inet4Address) Inet4Address.getByAddress(new byte[]{0, 0, (byte) 255, 127}));
        }
        while (nis.hasMoreElements()) {
            ni = nis.nextElement();
            if (!ni.isLoopback()/*not loopback*/ && ni.isUp()/*it works now*/) {
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    //filter for ipv4/ipv6
                    if (ia.getAddress().getAddress().length == 4) {
                        //4 for ipv4, 16 for ipv6
                        return (Inet4Address) ia.getAddress();
                    }
                }
            }
        }
        return null;
    }
}
