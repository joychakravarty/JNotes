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

    @Value("${cassandra.installation_role_username}")
    private String installationRoleUserName;

    @Value("${cassandra.installation_role_password}")
    private String installationRolePassword;

    @Value("${cassandra.client_role_username}")
    private String clientRoleUserName;

    @Value("${cassandra.client_role_password}")
    private String clientRolePassword;

    private CqlSession installerCqlSession;
    private CqlSession clientCqlSession;

    public CqlSession getInstallerSession() {
        if (installerCqlSession == null) {
            installerCqlSession = CqlSession.builder().withCloudSecureConnectBundle(JNotesApplication.getResource(securityBundle))
                    .withKeyspace(keyspace)
                    .withAuthCredentials(installationRoleUserName, EncryptionUtil.locallyDecrypt(installationRolePassword)).build();
        }
        return installerCqlSession;
    }

    protected void closeInstallerSession() {
        if (installerCqlSession != null) {
            installerCqlSession.close();
            installerCqlSession = null;
        }
    }

    protected void closeClientSession() {
        if (clientCqlSession != null) {
            clientCqlSession.close();
            clientCqlSession = null;
        }
    }

    public CqlSession getClientSession() {
        if (clientCqlSession == null) {
            clientCqlSession = CqlSession.builder().withCloudSecureConnectBundle(JNotesApplication.getResource(securityBundle))
                    .withKeyspace(keyspace).withAuthCredentials(clientRoleUserName, EncryptionUtil.locallyDecrypt(clientRolePassword))
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
        closeInstallerSession();
        closeClientSession();
    }

}
