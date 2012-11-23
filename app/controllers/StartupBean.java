package controllers;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.Http;


import com.alvazan.orm.api.base.NoSqlEntityManager;
import com.alvazan.orm.api.base.NoSqlEntityManagerFactory;
import com.alvazan.orm.api.base.anno.NoSqlEntity;
import com.alvazan.orm.models.test.PlayAccount;
import com.alvazan.orm.models.test.PlayAccountMTM;
import com.alvazan.orm.models.test.PlayActivity;
import com.alvazan.orm.models.test.PlayActivityMTM;
import com.alvazan.play.NoSql;


@OnApplicationStart
public class StartupBean extends Job {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(StartupBean.class);
     
    public static NoSqlEntityManager mgr;

    @Override
    public void doJob() throws Exception {

		mgr = NoSql.em(); 
       createTestdataMTM();
       createTestdata();
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
    
    private void createTestdataMTM() {
        
        PlayAccountMTM acc1 = new PlayAccountMTM();
        acc1.setId("acc1");
        acc1.setIsActive(false);
        mgr.fillInWithKey(acc1);

        PlayAccountMTM acc2 = new PlayAccountMTM();
        acc2.setId("acc2");
        acc2.setIsActive(true);
        mgr.fillInWithKey(acc2);

        PlayAccountMTM acc3 = new PlayAccountMTM();
        acc3.setId("acc3");
        acc3.setIsActive(false);
        mgr.fillInWithKey(acc3);
        
        PlayActivityMTM act1 = new PlayActivityMTM();
        act1.setId("act1");
        act1.setNumTimes(10);
        act1.setAccount(acc1);
        acc1.addActivity(act1);
        mgr.put(act1);

        PlayActivityMTM act2 = new PlayActivityMTM();
        act2.setId("act2");
        act2.setNumTimes(20);
        act2.setAccount(acc1);
        acc1.addActivity(act2);
        mgr.put(act2);

        PlayActivityMTM act3 = new PlayActivityMTM();
        act3.setId("act3");
        act3.setAccount(acc2);
        act3.setNumTimes(10);
        acc2.addActivity(act3);
        mgr.put(act3);

        PlayActivityMTM act4 = new PlayActivityMTM();
        act4.setId("act4");
        act4.setNumTimes(20);
        act4.setAccount(acc2);
        acc2.addActivity(act4);
        mgr.put(act4);

        PlayActivityMTM act5 = new PlayActivityMTM();
        act5.setId("act5");
        act5.setNumTimes(10);
        acc2.addActivity(act5);
        mgr.put(act5);

        PlayActivityMTM act6 = new PlayActivityMTM();
        act6.setId("act6");
        act6.setNumTimes(20);
        acc2.addActivity(act6);
        mgr.put(act6);
      
        
        PlayActivityMTM act7 = new PlayActivityMTM();
        act7.setId("act7");
        act7.setNumTimes(10);
        act7.setAccount(acc1);
        acc1.addActivity(act7);
        acc2.addActivity(act7);
        mgr.put(act7);
        
    	mgr.put(acc1);
		mgr.put(acc2);
		mgr.put(acc3);

        mgr.flush();
    }
}
