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
