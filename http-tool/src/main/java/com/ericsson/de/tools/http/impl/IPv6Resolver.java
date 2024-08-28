package com.ericsson.de.tools.http.impl;/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.ericsson.de.tools.http.DnsResolver;

public class IPv6Resolver implements DnsResolver {
    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        InetAddress[] ips = getAllIps(host);
        InetAddress[] ipv6 = new InetAddress[ips.length];

        int j = 0;
        for (InetAddress ip : ips) {
            if (Inet6Address.class.isAssignableFrom(ip.getClass())) {
                ipv6[j++] = ip;
            }
        }

        if (j == 0) {
            throw getException(host, ips);
        }

        return Arrays.copyOf(ipv6, j);
    }

    protected InetAddress[] getAllIps(String host) throws UnknownHostException {
        return InetAddress.getAllByName(host);
    }

    protected IllegalArgumentException getException(String host, InetAddress[] ips) {
        StringBuilder message = new StringBuilder("Unable to get IPv6 for host `");
        message.append(host).append("`. Resolved IPs: ");
        for (InetAddress ip : ips) {
            message.append(ip.getHostAddress()).append(" ");
        }

        return new IllegalArgumentException(message.toString());
    }
}
