package com.jc.jnotes.dao.remote;

import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;

public class CassandraInstallerDao implements RemoteInstallerDao {
    
    private CassandraSessionManager cassandraSessionManager;
    
    public CassandraInstallerDao(CassandraSessionManager cassandraSessionManager, String userId, String userEncKey) {
        this.cassandraSessionManager = cassandraSessionManager;
    }

    @Override
    public boolean installUserOnline(String userId) {
        
        CreateTable create = createTable(cassandraSessionManager.getKeyspace(), userId)
                .withPartitionKey("notebook", DataTypes.TEXT)
                .withClusteringColumn("noteid", DataTypes.TEXT)
                .withColumn("key", DataTypes.TEXT)
                .withColumn("value", DataTypes.TEXT)
                .withColumn("info", DataTypes.TEXT)
                .withColumn("lastModifiedTime", DataTypes.TIMESTAMP);
        
        ResultSet results = cassandraSessionManager.getInstallerSession().execute(create.build());
        
        cassandraSessionManager.closeInstallerSession();
        return true;
    }

}
