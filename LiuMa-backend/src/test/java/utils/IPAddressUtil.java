package utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPAddressUtil {

    public static String getCurrentIPAddress() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            return localhost.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String ipAddress = getCurrentIPAddress();
        System.out.println("Current IP Address: " + ipAddress);
    }
}

