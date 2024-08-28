package com.ericsson.de.tools.http.impl;

/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import static java.lang.String.format;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

public class IPv6ResolverTest {
    static final String HOST = "enmapache.athtem.eei.ericsson.se";
    static final InetAddress IPV6 = getByName("2001:1b70:82a1:103:0:0:0:80");

    static final InetAddress IPV4_1 = getByName("127.0.0.1");
    static final InetAddress IPV4_2 = getByName("192.168.0.80");

    @Test
    public void testResolve() throws Exception {
        IPv6Resolver resolver = spy(new IPv6Resolver());
        doReturn(new InetAddress[]{IPV4_1, IPV6, IPV4_2})
                .when(resolver).getAllIps(HOST);

        InetAddress[] result = resolver.resolve(HOST);

        assertThat(result).containsExactly(IPV6);
    }

    @Test
    public void testNotResolve() throws Exception {
        IPv6Resolver resolver = spy(new IPv6Resolver());
        doReturn(new InetAddress[]{IPV4_1, IPV4_2})
                .when(resolver).getAllIps(HOST);

        try {
            InetAddress[] result = resolver.resolve(HOST);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    format("Unable to get IPv6 for host `%s`. Resolved IPs: %s %s ",
                            HOST,
                            IPV4_1.getHostAddress(),
                            IPV4_2.getHostAddress()));
        }
    }

    private static InetAddress getByName(String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

}