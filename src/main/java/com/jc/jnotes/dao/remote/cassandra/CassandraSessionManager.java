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

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.jc.jnotes.JNotesApplication;
import com.jc.jnotes.util.EncryptionUtil;

@Component
public class CassandraSessionManager {

    @Value("${cassandra.security_bundle}")
    private String securityBundle;

    @Value("${cassandra.keyspace}")
    private String keyspace;

    @Value("${cassandra.jnotes_client_username}")
    private String clientUserName;

    @Value("${cassandra.jnotes_client_password}")
    private String clientPassword;

    private CqlSession clientCqlSession;

    protected void closeClientSession() {
        if (clientCqlSession != null) {
            clientCqlSession.close();
            clientCqlSession = null;
        }
    }

    public CqlSession getClientSession() {
        if (clientCqlSession == null) {
            clientCqlSession = CqlSession.builder().withCloudSecureConnectBundle(JNotesApplication.getResource(securityBundle))
                    .withKeyspace(keyspace).withAuthCredentials(clientUserName, EncryptionUtil.locallyDecrypt(clientPassword))
                    .build();
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
