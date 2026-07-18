package com.orchestra.executor.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OutboundUrlPolicy {

    private final Set<String> allowedHosts;
    private final boolean allowPrivateNetworks;

    public OutboundUrlPolicy(
            @Value("${orchestra.executor.http.allowed-hosts:}") String allowedHosts,
            @Value("${orchestra.executor.http.allow-private-networks:false}") boolean allowPrivateNetworks) {
        this.allowedHosts = Arrays.stream(allowedHosts.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        this.allowPrivateNetworks = allowPrivateNetworks;
    }

    public void validate(String rawUrl) {
        final URI uri;
        try {
            uri = URI.create(rawUrl);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("HTTP step URL is invalid", exception);
        }

        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("HTTP steps support only HTTP and HTTPS URLs");
        }
        if (host == null || host.isBlank() || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("HTTP step URL must contain a host and no embedded credentials");
        }
        if (!allowedHosts.contains(host.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("HTTP step host is not in the configured allowlist");
        }
        if (!allowPrivateNetworks) {
            rejectPrivateAddresses(host);
        }
    }

    private void rejectPrivateAddresses(String host) {
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                byte[] bytes = address.getAddress();
                boolean uniqueLocalIpv6 = bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
                if (address.isAnyLocalAddress()
                        || address.isLoopbackAddress()
                        || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress()
                        || uniqueLocalIpv6) {
                    throw new IllegalArgumentException("HTTP step host resolves to a private or local address");
                }
            }
        } catch (UnknownHostException exception) {
            throw new IllegalArgumentException("HTTP step host cannot be resolved", exception);
        }
    }
}
