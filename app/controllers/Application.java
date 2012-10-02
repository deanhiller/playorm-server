package controllers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    public static void showResult(@Required String testSQL,Integer size, Integer page) {
    	renderResult(testSQL, size, page);
    }
    public static void renderResult(@Required String testSQL,Integer size, Integer page) {
        if (validation.hasErrors()) {
            flash.error("SQL was not entered. Please enter a SQL");
            index();
        }
        page = page != null ? page : 1;
        size = size != null ? size : 50;        
        log.info("Getting results from the data store");
        NoSqlEntityManager mgr = NoSql.em();
        NoSqlTypedSession ntsession = mgr.getTypedSession();
        try {
            QueryResult result = ntsession.createQueryCursor(testSQL, size.intValue());
            Cursor<List<TypedRow>> rowsIter = result.getAllViewsCursor();
            List<ArrayList<LinkedHashMap>> tableRowsList = processBatch(rowsIter,size.intValue()); 
            render(testSQL, tableRowsList, size, page);
        } catch(ParseException e) {
            flash.error("Sorry, there is a problem.  " + e.getCause().getMessage());
            index();
        } catch(RuntimeException e) {
            flash.error("Sorry, there is a problem with the SQL. Please enter a Valid SQL");
            e.printStackTrace();
            index();
        }
    }
    
    /**
     * @param cursor
     * @return The list with all the values for a table 
     */
    private static List<ArrayList<LinkedHashMap>> processBatch(Cursor<List<TypedRow>> cursor, int size) {
        List<ArrayList<LinkedHashMap>> rowsList = new ArrayList<ArrayList<LinkedHashMap>>();
        while(cursor.next()) {
            List<TypedRow> joinedRow = cursor.getCurrent();
            ArrayList<LinkedHashMap> colList = new ArrayList<LinkedHashMap>();
            for(TypedRow r: joinedRow) {
                if (r!=null){            	
                	Map idColumnMap = new LinkedHashMap();
                    DboTableMeta meta = r.getView().getTableMeta();
                    DboColumnIdMeta idColumnMeta = meta.getIdColumnMeta();
                    String columnName = idColumnMeta.getColumnName();
                    idColumnMap.put("colId",columnName);
                    idColumnMap.put("colValue",r.getRowKeyString() );
                    colList.add((LinkedHashMap) idColumnMap);
                    for(TypedColumn c : r.getColumnsAsColl()) {
                        DboColumnMeta colMeta = meta.getColumnMeta(c.getName());
                        Map columnMap = new LinkedHashMap();
                        if(colMeta != null) {
                            String name = c.getName();            
                            if(colMeta instanceof DboColumnToManyMeta) {
                                String subName = c.getCompositeSubName();
                                name = name+"."+subName;
                                byte[] value = (byte[]) c.getValue();
                                String strVal = StandardConverters.convertToString(byte[].class, value);
                                columnMap.put("colId", name);
                                columnMap.put("colValue", strVal);                    
                            } else {
                                String val = c.getValueAsString();
                                columnMap.put("colId", name);
                                columnMap.put("colValue", val);
                            }
                        } else {
                            throw new RuntimeException("we need to fix this");
                        }
                        colList.add((LinkedHashMap) columnMap);
                    }
                } 
            }
            rowsList.add(colList);                
        }        
      return rowsList;
    }    
}