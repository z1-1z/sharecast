/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package org.fourthline.cling.android;

import org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl;
import org.fourthline.cling.transport.spi.InitializationException;

import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This factory tries to work around and patch some Android bugs.
 *
 * @author Michael Pujos
 * @author Christian Bauer
 */
public class AndroidNetworkAddressFactory extends NetworkAddressFactoryImpl {

//    final private static Logger log = Logger.getLogger(AndroidUpnpServiceConfiguration.class.getName());
//
//    public AndroidNetworkAddressFactory(int streamListenPort) {
//        super(streamListenPort);
//    }
//
//    @Override
//    protected boolean requiresNetworkInterface() {
//        return false;
//    }
//
//    @Override
//    protected boolean isUsableAddress(NetworkInterface networkInterface, InetAddress address) {
//        boolean result = super.isUsableAddress(networkInterface, address);
//        if (result) {
//            // TODO: Workaround Android DNS reverse lookup issue, still a problem on ICS+?
//            // http://4thline.org/projects/mailinglists.html#nabble-td3011461
//            String hostName = address.getHostAddress();
//            try {
//                Field field = InetAddress.class.getDeclaredField("hostName");
//                field.setAccessible(true);
//                field.set(address, hostName);
//            } catch (Exception ex) {
//                log.log(Level.SEVERE,
//                    "Failed injecting hostName to work around Android InetAddress DNS bug: " + address,
//                    ex
//                );
//                return false;
//            }
//        }
//        return result;
//    }
//
//    @Override
//    public InetAddress getLocalAddress(NetworkInterface networkInterface, boolean isIPv6, InetAddress remoteAddress) {
//        // TODO: This is totally random because we can't access low level InterfaceAddress on Android!
//        for (InetAddress localAddress : getInetAddresses(networkInterface)) {
//            if (isIPv6 && localAddress instanceof Inet6Address)
//                return localAddress;
//            if (!isIPv6 && localAddress instanceof Inet4Address)
//                return localAddress;
//        }
//        throw new IllegalStateException("Can't find any IPv4 or IPv6 address on interface: " + networkInterface.getDisplayName());
//    }
//
//    @Override
//    protected void discoverNetworkInterfaces() throws InitializationException {
//        try {
//            super.discoverNetworkInterfaces();
//        } catch (Exception ex) {
//            // TODO: ICS bug on some models with network interface disappearing while enumerated
//            // http://code.google.com/p/android/issues/detail?id=33661
//            log.warning("Exception while enumerating network interfaces, trying once more: " + ex);
//            super.discoverNetworkInterfaces();
//        }
//    }

    private static final Logger log = Logger.getLogger(AndroidUpnpServiceConfiguration.class.getName());

    public AndroidNetworkAddressFactory(int streamListenPort) {
        super(streamListenPort);
    }

    protected boolean requiresNetworkInterface() {
        return false;
    }

    protected boolean isUsableAddress(NetworkInterface networkInterface, InetAddress address) {
        boolean result = super.isUsableAddress(networkInterface, address);
        if (result) {
            String hostName = address.getHostAddress();
            Field field0 = null;
            Object target = null;

            try {
                try {
                    field0 = InetAddress.class.getDeclaredField("holder");
                    field0.setAccessible(true);
                    target = field0.get(address);
                    field0 = target.getClass().getDeclaredField("hostName");
                } catch (NoSuchFieldException var8) {
                    field0 = InetAddress.class.getDeclaredField("hostName");
                    target = address;
                }

                if (field0 == null || target == null || hostName == null) {
                    return false;
                }

                field0.setAccessible(true);
                field0.set(target, hostName);
            } catch (Exception var9) {
                log.log(Level.SEVERE, "Failed injecting hostName to work around Android InetAddress DNS bug: " + address, var9);
                return false;
            }
        }

        return result;
    }

    public InetAddress getLocalAddress(NetworkInterface networkInterface, boolean isIPv6, InetAddress remoteAddress) {
        Iterator var5 = this.getInetAddresses(networkInterface).iterator();

        InetAddress localAddress;
        do {
            if (!var5.hasNext()) {
                throw new IllegalStateException("Can't find any IPv4 or IPv6 address on interface: " + networkInterface.getDisplayName());
            }

            localAddress = (InetAddress)var5.next();
            if (isIPv6 && localAddress instanceof Inet6Address) {
                return localAddress;
            }
        } while(isIPv6 || !(localAddress instanceof Inet4Address));

        return localAddress;
    }

    protected void discoverNetworkInterfaces() throws InitializationException {
        try {
            super.discoverNetworkInterfaces();
        } catch (Exception var2) {
            log.warning("Exception while enumerating network interfaces, trying once more: " + var2);
            super.discoverNetworkInterfaces();
        }

    }
}