package controllers;

import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.slf4j.LoggerFactory;

import play.data.validation.Required;
import play.mvc.Controller;

import com.alvazan.orm.api.base.NoSqlEntityManager;
import com.alvazan.orm.api.exc.ParseException;
import com.alvazan.orm.api.util.NoSql;
import com.alvazan.orm.api.z3api.NoSqlTypedSession;
import com.alvazan.orm.api.z3api.QueryResult;
import com.alvazan.orm.api.z8spi.KeyValue;
import com.alvazan.orm.api.z8spi.conv.StandardConverters;
import com.alvazan.orm.api.z8spi.iter.Cursor;
import com.alvazan.orm.api.z8spi.meta.DboColumnIdMeta;
import com.alvazan.orm.api.z8spi.meta.DboColumnMeta;
import com.alvazan.orm.api.z8spi.meta.DboColumnToManyMeta;
import com.alvazan.orm.api.z8spi.meta.DboColumnToOneMeta;
import com.alvazan.orm.api.z8spi.meta.DboDatabaseMeta;
import com.alvazan.orm.api.z8spi.meta.DboTableMeta;
import com.alvazan.orm.api.z8spi.meta.TypedColumn;
import com.alvazan.orm.api.z8spi.meta.TypedRow;
 

public class Application extends Controller {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(StartupBean.class);
    
    public static void index() {
        render();
    }
    
    public static void renderResult(@Required String testSQL) {
        if (validation.hasErrors()) {
            flash.error("SQL was not entered. Please enter a SQL");
            index();
        }
        log.info("Getting results from the data store");
        NoSqlEntityManager mgr = NoSql.em();
        NoSqlTypedSession ntsession = mgr.getTypedSession();
        try {
            QueryResult result = ntsession.createQueryCursor(testSQL, 5);
            Cursor<KeyValue<TypedRow>> iter = result.getPrimaryViewCursor();
            Cursor<List<TypedRow>> rowsIter = result.getAllViewsCursor();
            render(testSQL,iter,rowsIter);
        } catch(ParseException e) {
            flash.error("Sorry, there is a problem.  " + e.getCause().getMessage());
            index();
        } catch(RuntimeException e) {
            flash.error("Sorry, there is a problem with the SQL. Please enter a Valid SQL");
            e.printStackTrace();
            index();
        }
    }
}