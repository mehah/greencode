package greencode.database;

import greencode.exception.GreencodeError;
import greencode.kernel.Console;
import greencode.kernel.GreenCodeConfig;
import greencode.util.LogMessage;

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
import java.util.concurrent.Executor;

public final class DatabaseConnection implements Connection {
	private Connection connection;
	private DatabaseConfig config = GreenCodeConfig.Server.DataBase.getConfig(GreenCodeConfig.Server.DataBase.defaultConfigFile);

	private ArrayList<Statement> statements = new ArrayList<Statement>();

	public static DatabaseConnection getInstance() {
		return new DatabaseConnection();
	}

	private byte chanceConnected = 1;

	public void start() throws SQLException {
		close();
		if(config == null) {
			throw new GreencodeError(LogMessage.getMessage("green-db-0008"));
		}
		String url = "jdbc:" + config.getDatabase() + "://" + config.getServerName() + ":3306/" + config.getSchema();
		try {
			this.connection = DriverManager.getConnection(url, config.getUserName(), config.getPassword());
		} catch (SQLException e) {
			Console.warning(url + "\n" + LogMessage.getMessage("green-db-0001", config.getDatabase()));

			if (config.getChanceReconnect() > 0 && chanceConnected < config.getChanceReconnect()) {
				Console.log(LogMessage.getMessage("green-db-0002"));
				try {
					Thread.sleep(250);
				} catch (InterruptedException e1) {
					throw new GreencodeError(e1);
				}

				++chanceConnected;
				start();
			} else if (config.getConnectionFileName() != null) {
				chanceConnected = 1;

				System.out.println(LogMessage.getMessage("green-db-0003", config.getConnectionFileName()));
				config = GreenCodeConfig.Server.DataBase.getConfig(config.getConnectionFileName());
				start();
			} else
				throw e;
		}
	}

	private Connection getConnection() throws SQLException {
		if (connection == null)
			throw new SQLException(LogMessage.getMessage("green-db-0004"));

		if (connection.isClosed())
			throw new SQLException(LogMessage.getMessage("green-db-0005"));

		return connection;
	}

	private Statement addStatements(Statement dbs) {
		this.statements.add(dbs);
		return dbs;
	}

	public DatabaseStatement createStatement() throws SQLException {
		return (DatabaseStatement) addStatements(new DatabaseStatement(getConnection().createStatement()));
	}

	public DatabaseStatement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return (DatabaseStatement) addStatements(new DatabaseStatement(getConnection().createStatement(resultSetType, resultSetConcurrency)));
	}

	public DatabaseStatement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return (DatabaseStatement) addStatements(new DatabaseStatement(getConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability)));
	}

	public DatabasePreparedStatement prepareStatement(String sql) throws SQLException {
		return (DatabasePreparedStatement) addStatements(new DatabasePreparedStatement(getConnection().prepareStatement(sql)));
	}

	public DatabasePreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return (DatabasePreparedStatement) addStatements(new DatabasePreparedStatement(getConnection().prepareStatement(sql, autoGeneratedKeys)));
	}

	public DatabasePreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return (DatabasePreparedStatement) addStatements(new DatabasePreparedStatement(getConnection().prepareStatement(sql, columnIndexes)));
	}

	public DatabasePreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return (DatabasePreparedStatement) addStatements(new DatabasePreparedStatement(getConnection().prepareStatement(sql, columnNames)));
	}

	public DatabasePreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return (DatabasePreparedStatement) addStatements(new DatabasePreparedStatement(getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency)));
	}

	public DatabasePreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return (DatabasePreparedStatement) addStatements(new DatabasePreparedStatement(getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability)));
	}

	public DatabaseConfig getConfig() {
		return config;
	}

	public DatabaseConnection setConfig(DatabaseConfig config) {
		this.config = config;
		return this;
	}

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
			if (statement.getResultSet() != null)
				statement.getResultSet().close();
			statement.close();
		}
		this.statements.clear();

		if (this.connection != null)
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
		return (CallableStatement) addStatements(new DatabasePreparedStatement(getConnection().prepareCall(sql)));
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return (CallableStatement) addStatements(new DatabasePreparedStatement(getConnection().prepareCall(sql, resultSetType, resultSetConcurrency)));
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return (CallableStatement) addStatements(new DatabasePreparedStatement(getConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability)));
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
			throw new GreencodeError(e);
		}
	}

	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		try {
			getConnection().setClientInfo(name, value);
		} catch (SQLException e) {
			throw new GreencodeError(e);
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

	public void setSchema(String schema) throws SQLException {
		getConnection().setSchema(schema);
	}

	public String getSchema() throws SQLException {
		return getConnection().getSchema();
	}

	public void abort(Executor executor) throws SQLException {
		getConnection().abort(executor);
	}

	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		getConnection().setNetworkTimeout(executor, milliseconds);
	}

	public int getNetworkTimeout() throws SQLException {
		return getConnection().getNetworkTimeout();
	}
}