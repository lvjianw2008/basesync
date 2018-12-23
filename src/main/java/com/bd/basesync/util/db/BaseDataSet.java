package com.bd.basesync.util.db;

import com.bd.basesync.util.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseDataSet {
	private static final Log log = LogFactory.getLog(BaseDataSet.class);
	/**
	 * 当前数据源名称
	 */
	protected String dataSourceStr = null;
	/**
	 * 当前数据源的配置信息
	 */
	protected Map dataSourceConf = new HashMap();
	/**
	 * jdbc连接
	 */
	protected Connection conn = null;
	
	protected Statement stmt = null;

	protected PreparedStatement prepstmt = null;

	protected CallableStatement cstmt = null;
	public static final String DS_NAME = "idb";
	public static final String JDBC_VER = "jdbc";

	private static LinkedList<Connection> pool = (LinkedList<Connection>)new LinkedList<Connection>();
	/**
	 * 初始化数据源
	 * @throws Exception
	 */
	public BaseDataSet(String dataSourceStr) throws Exception {
		try {
			getDataSource(dataSourceStr);
			this.stmt = this.conn.createStatement();
		} catch (Exception e) {
			log.error("DataSet init error: ", e);
			throw e;
		}
	}
	/**
	 * 初始化数据源及执行sql语句
	 * @param sql sql语句
	 * @throws Exception
	 */
	public BaseDataSet(String dataSourceStr,String sql) throws Exception {
		try {
			getDataSource(dataSourceStr);
			prepareStatement(sql);
		} catch (SQLException e) {
			log.error("DataSet init error: " + e);
			throw e;
		}
	}
	/**
	 * 获取jdbc连接
	 * @throws Exception
	 */
	protected void getDataSource(String dataSourceStr) throws Exception {
			getJdbcConn(dataSourceStr);
	}

	protected void getJdbcConn(String dataSourceStr) throws Exception {
		log.debug("dataSource:get");
//		if (pool.size() > 0) {
//			this.conn = pool.removeFirst();
//			return;
//		}
		try {
				Map<String,String> map=getDataSourceConf(dataSourceStr);
				this.setLinkInfoMap(map);//设置链接信息
				this.conn = DriverManager.getConnection(map.get("url"),map.get("user"),map.get("password"));
//				pool.add(conn);
		} catch (Exception e) {
			log.error("getJdbcConn() error: " + e);
			throw e;
		}
	}
	/**
	 * 设置链接信息的map
	 * @param map map
	 * 		minCount		最小连接数
			maxCount		最大连接数
			maxActiveTime	最大有效时间
			maxConnLifeTime 最大连接时间
	 * @throws Exception
	 */
	private void setLinkInfoMap(Map map)throws Exception {
		map.put("driver", getValue((String)map.get("driver"), ""));
		map.put("url", getValue((String)map.get("url"), ""));
		map.put("user", getValue((String)map.get("user"), ""));
		map.put("password", getValue((String)map.get("password"), ""));
		this.dataSourceConf=map;
		map.put("minCount",getValue((String)map.get("minCount"), BaseDataUtil.minCount));
		map.put("maxCount",getValue((String)map.get("maxCount"), BaseDataUtil.maxCount));
		map.put("maxActiveTime",getValue((String)map.get("maxActiveTime"), BaseDataUtil.maxActiveTime));
		map.put("maxConnLifeTime",getValue((String)map.get("maxConnLifeTime"), BaseDataUtil.maxConnLifeTime));
	}
	/**
	 * 获取数据源的配置信息
	 * @return
	 */
	public Map<String,String> getDataSourceConf(String dataSourceStr){
		Map map=new HashMap();
		try{
			if(dataSourceStr.equals("baida")){
				map.put("driver", "com.mysql.cj.jdbc.Driver");
				map.put("url", "jdbc:mysql://120.27.157.8:3306/ssgs");
				map.put("user", "root");
				map.put("password", "nosFMiGCARhB4oxu");
			}else if(dataSourceStr.equals("nc")){
				map.put("driver", "oracle.jdbc.driver.OracleDriver");
				map.put("url", "jdbc:oracle:thin:@192.9.200.241:1521:orcl");
				map.put("user", "ncllzj");
				map.put("password", "ncllzj");
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	/**
	 * 获取连接
	 * @return
	 */
	public Connection getConnection() {
		return this.conn;
	}
	/**
	 * 设置参数
	 * @param sql sql语句
	 * @param params 参数map
	 * @throws SQLException
	 */
	public void setParametersInPreparation(String sql,
			Map<String, Object> params) throws SQLException {
		Assert.notNull(sql);

		StringBuffer sqlBuff = new StringBuffer();

		List paramList = new ArrayList();

		Pattern pattern = Pattern.compile(":[^\\s=)<>,]+");
		Matcher matcher = pattern.matcher(sql);

		while (matcher.find()) {
			paramList.add(matcher.group(0).substring(1));
			matcher.appendReplacement(sqlBuff, "?");
		}
		matcher.appendTail(sqlBuff);

		prepareStatement(sqlBuff.toString());

		int i = 0;
		for (int length = paramList.size(); i < length; i++) {
			Object value = params.get(paramList.get(i));
			if ((value instanceof String))
				setString(i + 1, (String) value);
			else if ((value instanceof Float))
				setFloat(i + 1, ((Float) value).floatValue());
			else if ((value instanceof Integer))
				setInt(i + 1, ((Integer) value).intValue());
			else if ((value instanceof Long))
				setLong(i + 1, ((Long) value).longValue());
			else if ((value instanceof Date))
				setDate(i + 1, (Date) value);
			else if ((value instanceof Timestamp))
				setTimestamp(i + 1, (Timestamp) value);
			else
				setString(i + 1, value.toString());
		}
	}
	/**
	 * 取消自动提交
	 * @throws SQLException
	 */
	public void beginTran() throws SQLException {
		this.conn.setAutoCommit(false);
	}
	/**
	 * 手动提交
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		this.conn.commit();
		this.conn.setAutoCommit(true);
	}
	/**
	 * 回滚
	 */
	public void rollback() {
		try {
			this.conn.rollback();
			this.conn.setAutoCommit(true);
		} catch (Exception ex) {
			log.debug(ex);
		}
	}
	/**
	 * 执行sql语句
	 * @param sql sql语句
	 * @throws SQLException
	 */
	public void prepareStatement(String sql) throws SQLException {
		this.prepstmt = this.conn.prepareStatement(sql);
	}
	
	public void prepareCall(String sql) throws SQLException {
		prepareCall(sql, false);
	}

	public void prepareCall(String sql, boolean concurChanged)
			throws SQLException {
		if (concurChanged)
			this.cstmt = this.conn.prepareCall(sql, 1004, 1007);
		else
			this.cstmt = this.conn.prepareCall(sql);
	}

	public PreparedStatement getPreparedStatement() {
		return this.prepstmt;
	}

	public void setString(int index, String value) throws SQLException {
		this.prepstmt.setString(index, value);
	}

	public void setInt(int index, int value) throws SQLException {
		this.prepstmt.setInt(index, value);
	}

	public void setForKeyInt(int index, int value) throws SQLException {
		if (value <= 0)
			this.prepstmt.setNull(index, 0);
		else
			setInt(index, value);
	}

	public void setForKeyString(int index, String value) throws SQLException {
		if (value.length() == 0)
			this.prepstmt.setNull(index, 0);
		else
			setString(index, value);
	}

	public void setForKeyInt(PreparedStatement pstmt, int index, int value)
			throws SQLException {
		if (value <= 0)
			pstmt.setNull(index, 0);
		else
			pstmt.setInt(index, value);
	}

	public void setForKeyString(PreparedStatement pstmt, int index, String value)
			throws SQLException {
		if (value.length() == 0)
			pstmt.setNull(index, 0);
		else
			pstmt.setString(index, value);
	}

	public void setBoolean(int index, boolean value) throws SQLException {
		this.prepstmt.setBoolean(index, value);
	}

	public void setDate(int index, Date value) throws SQLException {
		this.prepstmt.setDate(index, value);
	}

	public void setTime(int index, Time value) throws SQLException {
		this.prepstmt.setTime(index, value);
	}

	public void setTimestamp(int index, Timestamp value) throws SQLException {
		this.prepstmt.setTimestamp(index, value);
	}

	public void setLong(int index, long value) throws SQLException {
		this.prepstmt.setLong(index, value);
	}

	public void setFloat(int index, float value) throws SQLException {
		this.prepstmt.setFloat(index, value);
	}

	public void setBinaryStream(int index, InputStream in, int length)
			throws SQLException {
		this.prepstmt.setBinaryStream(index, in, length);
	}

	public void clearParameters() throws SQLException {
		this.prepstmt.clearParameters();
	}

	public CallableStatement getCallableStatement() {
		return this.cstmt;
	}

	public Statement getStatement() {
		return this.stmt;
	}

	public Statement getStatement(int typeScroll, int concurOpe)
			throws SQLException {
		return this.conn.createStatement(typeScroll, concurOpe);
	}
	/**
	 * 执行sql语句
	 * @param sql
	 * @return rs 记录集
	 * @throws SQLException
	 */
	public ResultSet executeQuery(String sql) throws SQLException {
		if (this.stmt != null) {
			return this.stmt.executeQuery(sql);
		}
		return null;
	}

	public ResultSet executeQuery() throws SQLException {
		if (this.prepstmt != null) {
			return this.prepstmt.executeQuery();
		}
		return null;
	}
	/**
	 * 执行sql语句
	 * 	用于：INSERT、UPDATE 或 DELETE 类语句
	 * @param sql
	 * @throws SQLException
	 */
	public void executeUpdate(String sql) throws SQLException {
		if (this.stmt != null)
			this.stmt.executeUpdate(sql);
	}

	public int executeUpdate() throws SQLException {
		if (this.prepstmt != null) {
			return this.prepstmt.executeUpdate();
		}
		return 0;
	}
	/**
	 * 查询语句
	 * @param sql sql语句
	 * @return
	 * @throws Exception
	 */
	public List<Map> selectBySql(String sql) throws SQLException {
		ResultSet resultSet = executeQuery(sql);
		List<Map> list=rowMapToList(resultSet);
		return list;
	}
	/**
	 * 将resultset转为list
	 * @param resultSet
	 * @return
	 * @throws Exception
	 */
	private List<Map> rowMapToList(ResultSet resultSet) throws SQLException {
		List<Map> list=new ArrayList();
		ResultSetMetaData md = resultSet.getMetaData();
		int columnCount = md.getColumnCount(); 
		while(resultSet.next()){
			Map rowData = new HashMap();
			for (int i = 1; i <= columnCount; i++) {
				if(resultSet.getObject(i)!=null){
					rowData.put(md.getColumnName(i), resultSet.getObject(i).toString());
				}else{
					rowData.put(md.getColumnName(i), "");
				}
			}
			list.add(rowData);
		}
		return list;
	}
	public int getGeneratedKey() throws SQLException {
		if (this.prepstmt != null) {
			ResultSet rs = this.prepstmt.getGeneratedKeys();
			if (rs.next()) {
				return rs.getInt(1);
			}
		}
		return 0;
	}

	public void close() {
		closeStmt();
		try {
			this.conn.close();
		} catch (Exception ex) {
			log.debug(ex);
		}
	}

	public void closeStmt() {
		try {
			if (this.stmt != null) {
				this.stmt.close();
				this.stmt = null;
			}
			if (this.prepstmt != null) {
				this.prepstmt.close();
				this.prepstmt = null;
			}
			if (this.cstmt != null) {
				this.cstmt.close();
				this.cstmt = null;
			}
		} catch (Exception e) {
			log.debug("Statement close error: " + e);
		}
	}
	private static String getValue(String value, String defValue) {
		if (StringUtil.isNotNull(value)) {
			return value;
		}
		    return defValue;
	}
	
}
