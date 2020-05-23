/*
 * This file is part of JNotes. Copyright (C) 2020  Joy Chakravarty
 * 
 * JNotes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JNotes is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JNotes.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * 
 */
package com.jc.jnotes.dao.remote.cassandra;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.shaded.guava.common.net.HostAndPort;
import com.jc.jnotes.JNotesApplication;
import com.jc.jnotes.util.EncryptionUtil;

public class CassandraSessionManager {
    private String securityBundle;

    private String keyspace;

    private String clientUserName;

    private String clientPassword;

    private CqlSession clientCqlSession;
    
    //Only for Testing with local
    private static final String LOCALHOST = "127.0.0.1";
    private static final int DEFAULT_LOCAL_CASSANDRA_PORT = 9042;
    private static final String DEFAULT_LOCAL_DC = "datacenter1";

    public CassandraSessionManager(InputStream resourceAsStream) throws IOException {
        Properties prop = new Properties();
        prop.load(resourceAsStream);
        securityBundle = prop.getProperty("cassandra.security_bundle");
        keyspace = prop.getProperty("cassandra.keyspace");
        clientUserName = prop.getProperty("cassandra.jnotes_client_username");
        clientPassword = prop.getProperty("cassandra.jnotes_client_password");
    }

    protected void closeClientSession() {
        if (clientCqlSession != null) {
            clientCqlSession.close();
            clientCqlSession = null;
        }
    }

    public CqlSession getClientSession() {
        if (clientCqlSession == null) {
            if (StringUtils.isBlank(securityBundle)) {
                HostAndPort parsed = HostAndPort.fromString(LOCALHOST);
                InetSocketAddress inetSocketAddress = new InetSocketAddress(parsed.getHost(), parsed.getPortOrDefault(DEFAULT_LOCAL_CASSANDRA_PORT));
                clientCqlSession = CqlSession.builder().addContactPoint(inetSocketAddress).withLocalDatacenter(DEFAULT_LOCAL_DC)
                        .withAuthCredentials(clientUserName, EncryptionUtil.locallyDecrypt(clientPassword)).build();
            } else {
                clientCqlSession = CqlSession.builder().withCloudSecureConnectBundle(JNotesApplication.getResource(securityBundle))
                        .withKeyspace(keyspace).withAuthCredentials(clientUserName, EncryptionUtil.locallyDecrypt(clientPassword)).build();
            }
        }
        return clientCqlSession;
    }

    public String getKeyspace() {
        return keyspace;
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("CassandraSessionManager: Session cleanup");
        closeClientSession();
    }

}
