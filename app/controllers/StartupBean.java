package controllers;

import java.util.List;

import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.Http;

import com.alvazan.orm.api.base.DbTypeEnum;
import com.alvazan.orm.api.base.NoSqlEntityManager;
import com.alvazan.orm.api.base.anno.NoSqlEntity;
import com.alvazan.orm.api.util.NoSql;
import com.alvazan.orm.api.util.PlayCallback;
import com.alvazan.orm.models.test.PlayAccount;
import com.alvazan.orm.models.test.PlayActivity;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.AstyanaxContext.Builder;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ConsistencyLevel;

@OnApplicationStart
public class StartupBean extends Job {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(StartupBean.class);
     
    private NoSqlEntityManager mgr;

    @Override
    public void doJob() throws Exception {
        String prop = Play.configuration.getProperty("nosql.db");
        DbTypeEnum db = DbTypeEnum.IN_MEMORY;
        Builder builder = null;
        
        if ("cassandra".equalsIgnoreCase(prop)) {
            String clusterName = Play.configuration.getProperty("nosql.cassandra.clustername");
            String keyspace = Play.configuration.getProperty("nosql.cassandra.keyspace");
            String seeds = Play.configuration.getProperty("nosql.cassandra.seeds");
            
            if (clusterName == null)
                throw new IllegalArgumentException("property nosql.cassandra.clustername is required if using cassandra");
            else if (keyspace == null)
                throw new IllegalArgumentException("property nosql.cassandra.keyspace is required if using cassandra");
            else if (seeds == null)
                throw new IllegalArgumentException("property nosql.cassandra.seeds is required if using cassandra");
            builder = new AstyanaxContext.Builder()
                    .forCluster(clusterName)
                    .forKeyspace(keyspace)
                    .withAstyanaxConfiguration(new AstyanaxConfigurationImpl().setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE))
                    .withConnectionPoolConfiguration(
                            new ConnectionPoolConfigurationImpl("MyConnectionPool").setMaxConnsPerHost(2).setInitConnsPerHost(2).setSeeds(seeds))
                    .withConnectionPoolMonitor(new CountingConnectionPoolMonitor());
            // For a single cluster, the astyanax default of CL_ONE is JUST FINE but for a
            // multi-node cluster
            // we need to have it reconfigured...
            if (!"localhost:9160".equals(seeds)) {
                // for a multi-node cluster, we want the test suite using quorum on writes and
                // reads so we have no issues...
                AstyanaxConfigurationImpl config = new AstyanaxConfigurationImpl();
                config.setDefaultWriteConsistencyLevel(ConsistencyLevel.CL_QUORUM);
                config.setDefaultReadConsistencyLevel(ConsistencyLevel.CL_QUORUM);
                builder = builder.withAstyanaxConfiguration(config);
            }
            db = DbTypeEnum.CASSANDRA;
        }
        
        
        NoSql.initialize(new OurPlayCallback(), db, builder);
        String mode = Play.configuration.getProperty("application.mode");
        
        mgr = NoSql.em();
        
        if ("prod".equals(mode)) {
            log.info("running in production so skipping the rest of startup bean that sets up a mock database");
            return;
        }
                
        createTestdata();
    }

    private static class OurPlayCallback implements PlayCallback {
        @Override
        public List<Class> getClassesToScan() {
            return Play.classloader.getAnnotatedClasses(NoSqlEntity.class);
        }

        @Override
        public ClassLoader getClassLoader() {
            return Play.classloader;
        }

        @Override
        public Object getCurrentRequest() {
            return Http.Request.current.get();
        }
    }

    private void createTestdata() {
        PlayAccount acc1 = new PlayAccount();
        acc1.setId("acc1");
        acc1.setIsActive(false);
        mgr.put(acc1);

        PlayAccount acc2 = new PlayAccount();
        acc2.setId("acc2");
        acc2.setIsActive(true);
        mgr.put(acc2);

        PlayAccount acc3 = new PlayAccount();
        acc3.setId("acc3");
        acc3.setIsActive(false);
        mgr.put(acc3);

        PlayActivity act1 = new PlayActivity();
        act1.setId("act1");
        act1.setAccount(acc1);
        act1.setNumTimes(10);
        mgr.put(act1);

        PlayActivity act2 = new PlayActivity();
        act2.setId("act2");
        act2.setAccount(acc1);
        act2.setNumTimes(20);
        mgr.put(act2);

        PlayActivity act3 = new PlayActivity();
        act3.setId("act3");
        act3.setAccount(acc2);
        act3.setNumTimes(10);
        mgr.put(act3);

        PlayActivity act4 = new PlayActivity();
        act4.setId("act4");
        act4.setAccount(acc2);
        act4.setNumTimes(20);
        mgr.put(act4);

        PlayActivity act5 = new PlayActivity();
        act5.setId("act5");
        act5.setNumTimes(10);
        mgr.put(act5);

        PlayActivity act6 = new PlayActivity();
        act6.setId("act6");
        act6.setNumTimes(20);
        mgr.put(act6);

        PlayActivity act7 = new PlayActivity();
        act7.setId("act7");
        act7.setAccount(acc1);
        act7.setNumTimes(10);
        mgr.put(act7);

        mgr.flush();
    }
}
