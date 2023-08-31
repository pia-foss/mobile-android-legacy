/*
 *  Copyright (c) 2020 Private Internet Access, Inc.
 *
 *  This file is part of the Private Internet Access Android Client.
 *
 *  The Private Internet Access Android Client is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  The Private Internet Access Android Client is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with the Private
 *  Internet Access Android Client.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.privateinternetaccess.android.wireguard.config;

import androidx.annotation.Nullable;

import com.privateinternetaccess.android.wireguard.config.BadConfigException.Location;
import com.privateinternetaccess.android.wireguard.config.BadConfigException.Reason;
import com.privateinternetaccess.android.wireguard.config.BadConfigException.Section;
import com.privateinternetaccess.android.wireguard.crypto.Key;
import com.privateinternetaccess.android.wireguard.crypto.KeyFormatException;
import com.privateinternetaccess.android.wireguard.crypto.KeyPair;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Represents the configuration for a WireGuard interface (an [Interface] block). Interfaces must
 * have a private key (used to initialize a {@code KeyPair}), and may optionally have several other
 * attributes.
 * <p>
 * Instances of this class are immutable.
 */
public final class Interface {
    private static final int MAX_UDP_PORT = 65535;
    private static final int MIN_UDP_PORT = 0;

    private final Set<InetNetwork> addresses;
    private final Set<InetAddress> dnsServers;
    private final String gateway;
    private final Set<String> excludedApplications;
    private final KeyPair keyPair;
    private final Optional<Integer> listenPort;
    private final Optional<Integer> mtu;

    private Interface(final Builder builder) {
        // Defensively copy to ensure immutability even if the Builder is reused.
        addresses = Collections.unmodifiableSet(new LinkedHashSet<>(builder.addresses));
        dnsServers = Collections.unmodifiableSet(new LinkedHashSet<>(builder.dnsServers));
        gateway = builder.gateway;
        excludedApplications = Collections.unmodifiableSet(new LinkedHashSet<>(builder.excludedApplications));
        keyPair = Objects.requireNonNull(builder.keyPair, "Interfaces must have a private key");
        listenPort = builder.listenPort;
        mtu = builder.mtu;
    }

    /**
     * Parses an series of "KEY = VALUE" lines into an {@code Interface}. Throws
     * {@link ParseException} if the input is not well-formed or contains unknown attributes.
     *
     * @param lines An iterable sequence of lines, containing at least a private key attribute
     * @return An {@code Interface} with all of the attributes from {@code lines} set
     */
    public static Interface parse(final Iterable<? extends CharSequence> lines) throws Throwable {
        final Builder builder = new Builder();
        for (final CharSequence line : lines) {
            final Attribute attribute = Attribute.parse(line).orElseThrow(() ->
                    new BadConfigException(Section.INTERFACE, Location.TOP_LEVEL,
                            Reason.SYNTAX_ERROR, line));
            switch (attribute.getKey().toLowerCase(Locale.ENGLISH)) {
                case "address":
                    builder.parseAddresses(attribute.getValue());
                    break;
                case "dns":
                    builder.parseDnsServers(attribute.getValue());
                    break;
                case "listenport":
                    builder.parseListenPort(attribute.getValue());
                    break;
                case "mtu":
                    builder.parseMtu(attribute.getValue());
                    break;
                case "privatekey":
                    builder.parsePrivateKey(attribute.getValue());
                    break;
                case "gateway":
                    builder.parseGateway(attribute.getValue());
                    break;
                default:
                    throw new BadConfigException(Section.INTERFACE, Location.TOP_LEVEL,
                            Reason.UNKNOWN_ATTRIBUTE, attribute.getKey());
            }
        }
        return builder.build();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Interface))
            return false;
        final Interface other = (Interface) obj;
        return addresses.equals(other.addresses)
                && dnsServers.equals(other.dnsServers)
                && excludedApplications.equals(other.excludedApplications)
                && keyPair.equals(other.keyPair)
                && listenPort.equals(other.listenPort)
                && mtu.equals(other.mtu);
    }

    /**
     * Returns the set of IP addresses assigned to the interface.
     *
     * @return a set of {@link InetNetwork}s
     */
    public Set<InetNetwork> getAddresses() {
        // The collection is already immutable.
        return addresses;
    }

    /**
     * Returns the set of DNS servers associated with the interface.
     *
     * @return a set of {@link InetAddress}es
     */
    public Set<InetAddress> getDnsServers() {
        // The collection is already immutable.
        return dnsServers;
    }

    /**
     * Returns the tunnels gateway associated with the interface.
     *
     * @return and endpoint, or null if missing
     */
    public String getGateway() {
        return gateway;
    }

    /**
     * Returns the set of applications excluded from using the interface.
     *
     * @return a set of package names
     */
    public Set<String> getExcludedApplications() {
        // The collection is already immutable.
        return excludedApplications;
    }

    /**
     * Returns the public/private key pair used by the interface.
     *
     * @return a key pair
     */
    public KeyPair getKeyPair() {
        return keyPair;
    }

    /**
     * Returns the UDP port number that the WireGuard interface will listen on.
     *
     * @return a UDP port number, or {@code Optional.empty()} if none is configured
     */
    public Optional<Integer> getListenPort() {
        return listenPort;
    }

    /**
     * Returns the MTU used for the WireGuard interface.
     *
     * @return the MTU, or {@code Optional.empty()} if none is configured
     */
    public Optional<Integer> getMtu() {
        return mtu;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + addresses.hashCode();
        hash = 31 * hash + dnsServers.hashCode();
        hash = 31 * hash + excludedApplications.hashCode();
        hash = 31 * hash + keyPair.hashCode();
        hash = 31 * hash + listenPort.hashCode();
        hash = 31 * hash + mtu.hashCode();
        return hash;
    }

    /**
     * Converts the {@code Interface} into a string suitable for debugging purposes. The {@code
     * Interface} is identified by its public key and (if set) the port used for its UDP socket.
     *
     * @return A concise single-line identifier for the {@code Interface}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("(Interface ");
        sb.append(keyPair.getPublicKey().toBase64());
        listenPort.ifPresent(lp -> sb.append(" @").append(lp));
        sb.append(')');
        return sb.toString();
    }

    /**
     * Serializes the {@code Interface} for use with the WireGuard cross-platform userspace API.
     * Note that not all attributes are included in this representation.
     *
     * @return the {@code Interface} represented as a series of "KEY=VALUE" lines
     */
    public String toWgUserspaceString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("private_key=").append(keyPair.getPrivateKey().toHex()).append('\n');
        listenPort.ifPresent(lp -> sb.append("listen_port=").append(lp).append('\n'));
        return sb.toString();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static final class Builder {
        // Defaults to an empty set.
        private final Set<InetNetwork> addresses = new LinkedHashSet<>();
        // Defaults to an empty set.
        private final Set<InetAddress> dnsServers = new LinkedHashSet<>();
        // Defaults to an empty set.
        private final Set<String> excludedApplications = new LinkedHashSet<>();
        // No default; must be provided before building.
        @Nullable
        private KeyPair keyPair;
        // Defaults to not present.
        private Optional<Integer> listenPort = Optional.empty();
        // Defaults to not present.
        private Optional<Integer> mtu = Optional.empty();
        // No default.
        @Nullable
        private String gateway;

        public Builder addAddress(final InetNetwork address) {
            addresses.add(address);
            return this;
        }

        public Builder addAddresses(final Collection<InetNetwork> addresses) {
            this.addresses.addAll(addresses);
            return this;
        }

        public Builder addDnsServer(final InetAddress dnsServer) {
            dnsServers.add(dnsServer);
            return this;
        }

        public Builder addDnsServers(final Collection<? extends InetAddress> dnsServers) {
            this.dnsServers.addAll(dnsServers);
            return this;
        }

        public Interface build() throws BadConfigException {
            if (keyPair == null)
                throw new BadConfigException(Section.INTERFACE, Location.PRIVATE_KEY,
                        Reason.MISSING_ATTRIBUTE, null);
            return new Interface(this);
        }

        public Builder excludeApplication(final String application) {
            excludedApplications.add(application);
            return this;
        }

        public Builder excludeApplications(final Collection<String> applications) {
            excludedApplications.addAll(applications);
            return this;
        }

        public Builder parseAddresses(final CharSequence addresses) throws BadConfigException {
            try {
                for (final String address : Attribute.split(addresses))
                    addAddress(InetNetwork.parse(address));
                return this;
            } catch (final ParseException e) {
                throw new BadConfigException(Section.INTERFACE, Location.ADDRESS, e);
            }
        }

        public Builder parseDnsServers(final CharSequence dnsServers) throws BadConfigException {
            try {
                for (final String dnsServer : Attribute.split(dnsServers))
                    addDnsServer(InetAddresses.parse(dnsServer));
                return this;
            } catch (final ParseException e) {
                throw new BadConfigException(Section.INTERFACE, Location.DNS, e);
            }
        }

        public Builder parseListenPort(final String listenPort) throws BadConfigException {
            try {
                return setListenPort(Integer.parseInt(listenPort));
            } catch (final NumberFormatException e) {
                throw new BadConfigException(Section.INTERFACE, Location.LISTEN_PORT, listenPort, e);
            }
        }

        public Builder parseMtu(final String mtu) throws BadConfigException {
            try {
                return setMtu(Integer.parseInt(mtu));
            } catch (final NumberFormatException e) {
                throw new BadConfigException(Section.INTERFACE, Location.MTU, mtu, e);
            }
        }

        public Builder parsePrivateKey(final String privateKey) throws BadConfigException {
            try {
                return setKeyPair(new KeyPair(Key.fromBase64(privateKey)));
            } catch (final KeyFormatException e) {
                throw new BadConfigException(Section.INTERFACE, Location.PRIVATE_KEY, e);
            }
        }

        public Builder parseGateway(final String gateway) {
            this.gateway = gateway;
            return this;
        }

        public Builder setKeyPair(final KeyPair keyPair) {
            this.keyPair = keyPair;
            return this;
        }

        public Builder setListenPort(final int listenPort) throws BadConfigException {
            if (listenPort < MIN_UDP_PORT || listenPort > MAX_UDP_PORT)
                throw new BadConfigException(Section.INTERFACE, Location.LISTEN_PORT,
                        Reason.INVALID_VALUE, String.valueOf(listenPort));
            this.listenPort = listenPort == 0 ? Optional.empty() : Optional.of(listenPort);
            return this;
        }

        public Builder setMtu(final int mtu) throws BadConfigException {
            if (mtu < 0)
                throw new BadConfigException(Section.INTERFACE, Location.LISTEN_PORT,
                        Reason.INVALID_VALUE, String.valueOf(mtu));
            this.mtu = mtu == 0 ? Optional.empty() : Optional.of(mtu);
            return this;
        }
    }
}
