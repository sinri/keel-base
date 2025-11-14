package io.github.sinri.keel.utils;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * @since 2.8
 */
public class NetUtils {

    private NetUtils() {
    }


    @Nullable
    public static Long convertIPv4ToNumber(@Nullable String ipv4) {
        //Converts a String that represents an IP to an int.
        try {
            InetAddress i = InetAddress.getByName(ipv4);
            //System.out.println(i);
            byte[] address = i.getAddress();
            //System.out.println(address.length);
            var p1 = Byte.toUnsignedLong(address[0]);
            var p2 = Byte.toUnsignedLong(address[1]);
            var p3 = Byte.toUnsignedLong(address[2]);
            var p4 = Byte.toUnsignedLong(address[3]);
            //System.out.println(p1+"."+p2+"."+p3+"."+p4);
            return ((p1 << 24) + (p2 << 16) + (p3 << 8) + p4);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    @Nullable
    public static String convertNumberToIPv4(long number) {
        //This converts an int representation of ip back to String
        try {
            InetAddress i = InetAddress.getByName(String.valueOf(number));
            return i.getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    @NotNull
    public static byte[] convertIPv4ToAddressBytes(long ipv4AsLong) {
        return new byte[]{
                (byte) (ipv4AsLong >> 24),
                (byte) ((ipv4AsLong >> 16) & 0xFF),
                (byte) ((ipv4AsLong >> 8) & 0xFF),
                (byte) (ipv4AsLong & 0xFF),
        };
    }

    @NotNull
    public static byte[] convertIPv4ToAddressBytes(@Nullable String ipv4) {
        long x = Objects.requireNonNull(convertIPv4ToNumber(ipv4));
        return convertIPv4ToAddressBytes(x);
    }

    /**
     * @return like "127.0.0.1"; If the local host name could not be resolved into an address, null.
     * @since 2.9.1
     */
    @Nullable
    public static String getLocalHostAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * @return like "SinriMacInLeqee.local"; If the local host name could not be resolved into an address, null.
     * @since 2.9.1
     */
    @Nullable
    public static String getLocalHostName() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * @return like "localhost"; If the local host name could not be resolved into an address, null.
     * @since 2.9.1
     */
    @Nullable
    public static String getLocalHostCanonicalName() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getCanonicalHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
