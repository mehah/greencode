package greencode.database;

import greencode.kernel.Console;
import greencode.kernel.GreenCodeConfig;
import greencode.kernel.LogMessage;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

public final class DatabaseConnection implements Connection {
	private Connection connection;	
	private DatabaseConfig config;
	
	private ArrayList<Statement> statements = new ArrayList<Statement>();
	
	public static DatabaseConnection getInstance() {return new DatabaseConnection();}
	
	private byte chanceConnected = 1;
	public void start() throws SQLException {
		close();
		String url = "jdbc:"+getConfig().getDatabase()+"://" + getConfig().getServerName() + ":3306/" + getConfig().getSchema();
		try {
			this.connection = DriverManager.getConnection(url, getConfig().getUserName(), getConfig().getPassword());
		} catch (SQLException e) {
			Console.log(url);
			Console.error(LogMessage.getMessage("green-db-0001", getConfig().getDatabase()));
		
			if(config.getChanceReconnect() > 0 && chanceConnected < config.getChanceReconnect())
			{
				System.out.println(LogMessage.getMessage("green-db-0002"));
				try {
					Thread.sleep(250);
				} catch (InterruptedException e1) {
					Console.error(e1);
				}
				
				++chanceConnected;				
				start();
			}else if(config.getConnectionFileName() != null)
			{
				chanceConnected = 1;
				
				System.out.println(LogMessage.getMessage("green-db-0003", config.getConnectionFileName()));
				config = GreenCodeConfig.DataBase.getConfig(config.getConnectionFileName());
				start();
			}else
				throw e;
		}
	}
	
	private Connection getConnection() throws SQLException {
		if(connection == null)
			throw new SQLException(LogMessage.getMessage("green-db-0004"));
		
		if(isClosed())
			throw new SQLException(LogMessage.getMessage("green-db-0005"));
		
		return connection;
	}
	
	private Statement addStatements(Statement dbs) {this.statements.add(dbs);return dbs;}

	public DatabaseStatement createStatement() throws SQLException
	{return (DatabaseStatement) addStatements(new DatabaseStatement(getConnection().createStatement()));}
	
	public DatabaseStatement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
	{return (DatabaseStatement) addStatements(new DatabaseStatement(getConnection().createStatement(resultSetType, resultSetConcurrency)));}
	
	public DatabaseStatement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
	{return (DatabaseStatement) addStatements(new DatabaseStatement(getConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability)));}
	
	public DatabasePreparedStatement prepareStatement(String sql) throws SQLException
	{return (DatabasePreparedStatement) addStatements(new DatabasePreparedStatement(getConnection().prepareStatement(sql)));}
	
	public DatabasePreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
	{return (DatabasePreparedStatement)addStatements(new DatabasePreparedStatement(getConnection().prepareStatement(sql, autoGeneratedKeys)));}
	
	public DatabasePreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
	{return (DatabasePreparedStatement)addStatements(new DatabasePreparedStatement(getConnection().prepareStatement(sql, columnIndexes)));}
	
	public DatabasePreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
	{return (DatabasePreparedStatement)addStatements(new DatabasePreparedStatement(getConnection().prepareStatement(sql, columnNames)));}
	
	public DatabasePreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
	{return (DatabasePreparedStatement)addStatements(new DatabasePreparedStatement(getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency)));}
	
	public DatabasePreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
	{return (DatabasePreparedStatement)addStatements(new DatabasePreparedStatement(getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability)));}

	public DatabaseConfig getConfig() {return config;}
	public DatabaseConnection setConfig(DatabaseConfig config) { this.config = config; return this; }

	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return getConnection().isWrapperFor(arg0);
	}

	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return getConnection().unwrap(arg0);
	}

	public void clearWarnings() throws SQLException {
		getConnection().clearWarnings();
	}

	public void close() throws SQLException {
		for (Statement statement : this.statements) {			
			statement.getResultSet().close();
			statement.close();
		}
		this.statements.clear();
					
		if(this.connection != null)
			this.connection.close();
	}

	public void commit() throws SQLException {
		getConnection().commit();
	}

	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return getConnection().createArrayOf(typeName, elements);
	}

	public Blob createBlob() throws SQLException {
		return getConnection().createBlob();
	}

	public Clob createClob() throws SQLException {
		return getConnection().createClob();
	}

	public NClob createNClob() throws SQLException {
		return getConnection().createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return getConnection().createSQLXML();
	}

	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return getConnection().createStruct(typeName, attributes);
	}

	public boolean getAutoCommit() throws SQLException {
		return getConnection().getAutoCommit();
	}

	public String getCatalog() throws SQLException {
		return getConnection().getCatalog();
	}

	public Properties getClientInfo() throws SQLException {
		return getConnection().getClientInfo();
	}

	public String getClientInfo(String name) throws SQLException {
		return getConnection().getClientInfo(name);
	}
	
	public int getHoldability() throws SQLException {
		return getConnection().getHoldability();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		return getConnection().getMetaData();
	}

	public int getTransactionIsolation() throws SQLException {
		return getConnection().getTransactionIsolation();
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return getConnection().getTypeMap();
	}

	public SQLWarning getWarnings() throws SQLException {
		return getConnection().getWarnings();
	}

	public boolean isClosed() throws SQLException {
		return getConnection().isClosed();
	}

	public boolean isReadOnly() throws SQLException {
		return getConnection().isReadOnly();
	}

	public boolean isValid(int timeout) throws SQLException {
		return getConnection().isValid(timeout);
	}

	public String nativeSQL(String sql) throws SQLException {
		return getConnection().nativeSQL(sql);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		return (CallableStatement)addStatements(new DatabasePreparedStatement(getConnection().prepareCall(sql)));
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return (CallableStatement)addStatements(new DatabasePreparedStatement(getConnection().prepareCall(sql, resultSetType, resultSetConcurrency)));
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return (CallableStatement)addStatements(new DatabasePreparedStatement(getConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability)));
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		getConnection().releaseSavepoint(savepoint);
	}

	public void rollback() throws SQLException {
		getConnection().rollback();
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		getConnection().rollback(savepoint);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		getConnection().setAutoCommit(autoCommit);
	}

	public void setCatalog(String catalog) throws SQLException {
		getConnection().setCatalog(catalog);
	}

	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		try {
			getConnection().setClientInfo(properties);
		} catch (SQLException e) {
			Console.error(e);
		}
	}

	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		try {
			getConnection().setClientInfo(name, value);
		} catch (SQLException e) {
			Console.error(e);
		}
	}

	public void setHoldability(int holdability) throws SQLException {
		getConnection().setHoldability(holdability);
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		getConnection().setReadOnly(readOnly);
	}

	public Savepoint setSavepoint() throws SQLException {
		return getConnection().setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return getConnection().setSavepoint(name);
	}

	public void setTransactionIsolation(int level) throws SQLException {
		getConnection().setTransactionIsolation(level);
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		getConnection().setTypeMap(map);
	}
}
