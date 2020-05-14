package com.jc.jnotes.dao.remote.cassandra;

import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.servererrors.AlreadyExistsException;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.jc.jnotes.dao.remote.RemoteInstallerDao;

@Component
public class CassandraInstallerDao implements RemoteInstallerDao {
    
    @Autowired
    private CassandraSessionManager cassandraSessionManager;

    @Override
    public boolean installUserOnline(String userId) {
        boolean returnStatus = true;
        try {
            CreateTable create = createTable(cassandraSessionManager.getKeyspace(), userId).withPartitionKey("notebook", DataTypes.TEXT)
                    .withClusteringColumn("noteid", DataTypes.TEXT).withColumn("key", DataTypes.TEXT).withColumn("value", DataTypes.TEXT)
                    .withColumn("info", DataTypes.TEXT).withColumn("lastModifiedTime", DataTypes.TIMESTAMP);

            cassandraSessionManager.getInstallerSession().execute(create.build());
        } catch (AlreadyExistsException ex) {
            System.err.println(userId + "already exists");
            returnStatus = false;
        }
        cassandraSessionManager.closeInstallerSession();
        return returnStatus;
    }

}
