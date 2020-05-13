package com.jc.jnotes.dao.remote;

import java.net.URL;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.jc.jnotes.helper.EncryptionHelper;

@Component
public class CassandraSessionManager {
    
    @Autowired
    private URL secureConnectBundleURL;
    
    @Autowired
    private EncryptionHelper encryptionHelper;
    
    @Value( "${cassandra.keyspace}" )
    private String keyspace;
    
    @Value( "${cassandra.installation_role_username}" )
    private String installationRoleUserName;
    
    @Value( "${cassandra.installation_role_password}" )
    private String installationRolePassword;
    
    @Value( "${cassandra.client_role_username}" )
    private String clientRoleUserName;
    
    @Value( "${cassandra.client_role_password}" )
    private String clientRolePassword;
    
    private CqlSession installerCqlSession;
    private CqlSession clientCqlSession;
    
    public CqlSession getInstallerSession() {
        if(installerCqlSession == null) {
            installerCqlSession = CqlSession.builder()
                    .withCloudSecureConnectBundle(secureConnectBundleURL)
                    .withKeyspace(keyspace)
                    .withAuthCredentials(installationRoleUserName, encryptionHelper.locallyDecrypt(installationRolePassword))
                    .build();
        }
        return installerCqlSession;
    }
    
    protected void closeInstallerSession() {
        if(installerCqlSession!=null) {
            installerCqlSession.close();
            installerCqlSession = null;
        }
    }
    
    public CqlSession getClientSession() {
        if(clientCqlSession == null) {
            clientCqlSession = CqlSession.builder()
                    .withCloudSecureConnectBundle(secureConnectBundleURL)
                    .withKeyspace(keyspace)
                    .withAuthCredentials(clientRoleUserName, encryptionHelper.locallyDecrypt(clientRolePassword))
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
        if(clientCqlSession!=null) {
            clientCqlSession.close();
        }
    }
    

}
