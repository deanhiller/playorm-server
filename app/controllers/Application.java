package controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import play.Play;
import play.data.validation.Required;
import play.mvc.Controller;

import com.alvazan.orm.api.base.NoSqlEntityManager;
import com.alvazan.orm.api.exc.ParseException;
import com.alvazan.orm.api.z3api.NoSqlTypedSession;
import com.alvazan.orm.api.z3api.QueryResult;
import com.alvazan.orm.api.z5api.IndexPoint;
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
import com.alvazan.play.NoSql;

public class Application extends Controller {
	private static final org.slf4j.Logger log = LoggerFactory
			.getLogger(StartupBean.class);
	static Cursor<List<TypedRow>> rowsIter;
	static int batchsize = 50;

	public static void index() {
		render();
	}

	public static void showResult(@Required String testSQL, int page) {
		processSQL(testSQL, page);
	}

	public static void processSQL(@Required String testSQL, int page) {
		if (validation.hasErrors()) {
			flash.error("SQL was not entered. Please enter a SQL");
			index();
		}
		page = page != 0 ? page : 1;
		log.info("Getting results from the data store");
		NoSqlEntityManager mgr = NoSql.em();
		NoSqlTypedSession ntsession = mgr.getTypedSession();
		try {
			QueryResult result = ntsession
					.createQueryCursor(testSQL, batchsize);
			rowsIter = result.getAllViewsCursor();
		} catch (ParseException e) {
			flash.error("Sorry, there is a problem.  "
					+ e.getCause().getMessage());
			index();
		} catch (RuntimeException e) {
			flash.error("Sorry, there is a problem with the SQL. Please enter a Valid SQL");
			e.printStackTrace();
			index();
		}
		renderResult(testSQL, page);
	}

	public static void renderResult(@Required String testSQL, int page) {
		if (rowsIter != null) {
			List<ArrayList<LinkedHashMap>> tableRowsList = processBatch();
			int localsize = batchsize;
			if (!tableRowsList.isEmpty())
				render(testSQL, tableRowsList, page, localsize);
			else
				processSQL(testSQL, page);
		} else
			processSQL(testSQL, page);
	}

	/**
	 * 
	 * @return The list with all the values for a table
	 */
	private static List<ArrayList<LinkedHashMap>> processBatch() {
		List<ArrayList<LinkedHashMap>> rowsList = new ArrayList<ArrayList<LinkedHashMap>>();
		int rowcount = 0;
		while (rowsIter.next()) {
			rowcount++;
			List<TypedRow> joinedRow = rowsIter.getCurrent();
			ArrayList<LinkedHashMap> colList = new ArrayList<LinkedHashMap>();
			for (TypedRow r : joinedRow) {
				if (r != null) {
					Map idColumnMap = new LinkedHashMap();
					DboTableMeta meta = r.getView().getTableMeta();
					DboColumnIdMeta idColumnMeta = meta.getIdColumnMeta();
					String columnName = idColumnMeta.getColumnName();
					idColumnMap.put("colId", columnName);
					idColumnMap.put("colValue", r.getRowKeyString());
					colList.add((LinkedHashMap) idColumnMap);
					for (TypedColumn c : r.getColumnsAsColl()) {
						DboColumnMeta colMeta = c.getColumnMeta();
						Map columnMap = new LinkedHashMap();
						if (colMeta != null) {
							String name = c.getName();
							String strVal = c.getValueAsString();
							columnMap.put("colId", name);
							columnMap.put("colValue", strVal);
						} else {
							String fullName = c.getNameAsString(byte[].class);
							String val = c.getValueAsString(byte[].class);
							columnMap.put("colId", fullName);
							columnMap.put("colValue", val);
						}
						colList.add((LinkedHashMap) columnMap);
					}
				}
			}
			rowsList.add(colList);
			if (rowcount == batchsize) {
				return rowsList;
			}
		}
		return rowsList;
	}

	public static void firstPage(String testSQL, int page) {
		rowsIter.beforeFirst();
		renderResult(testSQL, page);
	}

	public static void allTables() {
		String keyspace = Play.configuration
				.getProperty("nosql.cassandra.keyspace");
		NoSqlEntityManager mgr = NoSql.em();
		DboDatabaseMeta database = mgr.find(DboDatabaseMeta.class,
				DboDatabaseMeta.META_DB_ROWKEY);
		Collection<DboTableMeta> allTables = database.getAllTables();
		ArrayList<String> tableList = new ArrayList<String>();
		for (DboTableMeta tableMeta : allTables) {
			tableList.add(tableMeta.getColumnFamily());
		}
		render(keyspace, tableList);
	}

	public static void detailResult(String columnFamilyName) {
		String keyspace = Play.configuration
				.getProperty("nosql.cassandra.keyspace");
		NoSqlEntityManager mgr = NoSql.em();
		DboDatabaseMeta database = mgr.find(DboDatabaseMeta.class,
				DboDatabaseMeta.META_DB_ROWKEY);
		Collection<DboTableMeta> allTables = database.getAllTables();
		ArrayList<String> columnList = new ArrayList<String>();
		ArrayList<String> indexList = new ArrayList<String>();
		ArrayList<String> showList = new ArrayList<String>();
		for (DboTableMeta tableMeta : allTables) {
			if (tableMeta.getColumnFamily().equals(columnFamilyName)) {
				Collection<DboColumnMeta> allColumns = tableMeta
						.getAllColumns();
				Collection<DboColumnMeta> allIndexColumn = tableMeta
						.getIndexedColumns();
				for (DboColumnMeta column : allColumns)
					columnList.add(column.getColumnName());
				for (DboColumnMeta index : allIndexColumn)
					indexList.add(index.getColumnName());
				for (DboColumnMeta column : allColumns) {
					if (!indexList.contains(column.getColumnName()))
						showList.add(column.getColumnName());
				}
				break;
			}
		}
		render(keyspace, indexList, columnFamilyName, showList);
	}

	public static void processIndex(String columnFamilyName, String index) {
		NoSqlEntityManager mgr = NoSql.em();
		NoSqlTypedSession s = mgr.getTypedSession();
		ArrayList<String> columnList = new ArrayList<String>();
		Cursor<IndexPoint> indexView = s.indexView(columnFamilyName, index,
				null, null);
		int count = 0;
		int first = 50;
		while (indexView.next() && count < first) {
			IndexPoint current = indexView.getCurrent();
			String indVal = current.getIndexedValueAsString();
			columnList.add(indVal);
			count++;
		}
		render(columnList, columnFamilyName, index, first, count);
	}

	public static void moreIndex(String columnFamilyName, String index, int page) {
		NoSqlEntityManager mgr = NoSql.em();
		NoSqlTypedSession s = mgr.getTypedSession();
		ArrayList<String> columnList = new ArrayList<String>();
		Cursor<IndexPoint> indexView = s.indexView(columnFamilyName, index,
				null, null);
		while (indexView.next()) {
			IndexPoint current = indexView.getCurrent();
			String indVal = current.getIndexedValueAsString();
			columnList.add(indVal);
		}
		renderIndexBatch(columnFamilyName, index, page, columnList);
	}

	private static void renderIndexBatch(String columnFamilyName, String index, int page,  List columnList) {
		page = page != 0 ? page : 1;
		int first = 50;
		ArrayList<String> batchList = new ArrayList<String>();
		int k = columnList.size();
		int p = 0;
		for (int i = ((page - 1) * 50 + first); i < ((page) * 50 + first)
				&& i < k; i++) {
			batchList.add(columnList.get(i).toString());
			p++;
		}
		renderIndex(batchList, page, columnFamilyName, index, p);
	}

	public static void renderIndex(List batchList, int page,
			String columnFamilyName, String index, int p) {
		render(batchList, page, columnFamilyName, index, p);
	}
}