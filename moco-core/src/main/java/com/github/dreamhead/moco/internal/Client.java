package com.github.dreamhead.moco.internal;

import java.net.InetSocketAddress;

public record Client(String address, int port) {
    public Client(final InetSocketAddress address) {
        this(address.getAddress().getHostAddress(), address.getPort());
    }
}
