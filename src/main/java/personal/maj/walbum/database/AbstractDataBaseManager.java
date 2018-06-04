package personal.maj.walbum.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import personal.maj.walbum.common.Reflector;
import personal.maj.walbum.model.data.ConnectionData;
import personal.maj.walbum.model.data.PoolConfig;
import personal.maj.walbum.model.info.builder.DataBaseInfoBuilder;
import personal.maj.walbum.model.structure.*;
import personal.maj.walbum.model.structure.constant.ConstraintType;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by MAJ on 2018/3/29.
 */
public abstract class AbstractDataBaseManager implements DataBaseManager{

    private static final String[] TABLE_TYPES = new String[]{"TABLE"};

    private Connection connection;

    private String provider;

    private DatabaseMetaData metaData;

    private DataSource dataSource;

    @Override
    public DataBaseStructure readDataBase() throws SQLException {
        String catalog = getCatalog();
        String schema = getSchema();
        DataBaseStructure dataBaseStructure = new DataBaseStructure(getDataBaseName(), getProvider());
        ResultSet rs = getMetaData().getTables(catalog, schema, null, TABLE_TYPES);
        Map<String, TableStructure> tableStructures = new LinkedHashMap<>();
        while (rs.next()) {
            TableStructure tableStructure = readTable(rs.getString(3));
            tableStructures.put(tableStructure.getName(), tableStructure);
            if (tableStructure.getConstraintStructures() != null && !tableStructure.getConstraintStructures().isEmpty()) {
                if (dataBaseStructure.getConstraintStructures() == null)
                    dataBaseStructure.setConstraintStructures(new LinkedHashMap<>());
                dataBaseStructure.getConstraintStructures().putAll(tableStructure.getConstraintStructures());
            }
            if (tableStructure.getIndexStructures() != null && tableStructure.getIndexStructures().isEmpty()) {
                if (dataBaseStructure.getIndexStructures() == null)
                    dataBaseStructure.setIndexStructures(new LinkedHashMap<>());
                dataBaseStructure.getIndexStructures().putAll(tableStructure.getIndexStructures());
            }
        }
        rs.close();
        if (!tableStructures.isEmpty())
            dataBaseStructure.setTableStructures(tableStructures);
        return dataBaseStructure;
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        boolean mine = false;
        if (connection!= null) {
            mine = this.connection.equals(connection);
            connection.close();
        }
        if (mine)
            this.connection = null;
    }

    @Override
    public Connection newConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (this.connection == null)
            connection = newConnection();
        return this.connection;
    }

    private TableStructure readTable(String tableName) throws SQLException {
        TableStructure tableStructure = new TableStructure(tableName);
        List<String> primaries = getPrimaries(tableName);
        Map<String, ColumnStructure> columnStructures = readColumns(tableName, primaries);
        if (columnStructures != null && !columnStructures.isEmpty())
            tableStructure.setColumnStructures(columnStructures);
        Map<String, IndexStructure> indices = readIndices(tableName, primaries);
        if (!indices.isEmpty())
            tableStructure.setIndexStructures(indices);
        Map<String, ConstraintStructure> constraintStructures = readConstraints(tableName);
        if (constraintStructures != null && !constraintStructures.isEmpty())
            tableStructure.setConstraintStructures(constraintStructures);
        return tableStructure;
    }

    private Map<String, IndexStructure> readIndices(String tableName, List<String> primaries) throws SQLException {
        Map<String, IndexStructure> indices = new HashMap<>();
        ResultSet rs = getMetaData().getIndexInfo(getCatalog(), getSchema(), tableName, false, true);
        while (rs.next()) {
            String name = rs.getString(6);
            String columnName = rs.getString(9);
            if (name == null || primaries.contains(columnName) || !rs.getBoolean(4))
                continue;
            IndexStructure indexStructure = indices.get(name);
            if (indexStructure == null) {
                indexStructure = new IndexStructure(name);
                indexStructure.setClustered(false);
                Set<String> columnStructures = new LinkedHashSet<>();
                columnStructures.add(columnName);
                indexStructure.setColumnNames(columnStructures);
            } else {
                indexStructure.getColumnNames().add(columnName);
                indexStructure.setClustered(true);
            }
            indices.put(name, indexStructure);
        }
        rs.close();
        return indices;
    }

    private Map<String, ColumnStructure> readColumns(String tableName, List<String> primaries) throws SQLException {
        ResultSet rs = getMetaData().getColumns(getCatalog(), getSchema(), tableName, null);
        Map<String, ColumnStructure> columnStructures = new LinkedHashMap<>();
        while (rs.next()) {
            String name = rs.getString(4);
            boolean isPrimary = primaries.contains(rs.getString(4));
            int sqlType = rs.getInt(5);
            int length = rs.getInt(7);
            boolean notNull = rs.getInt(11) == DatabaseMetaData.columnNoNulls;
            String defaultValue = rs.getString(13);
            defaultValue = (defaultValue == null) ? null : defaultValue.trim();
            ColumnStructure structure = new ColumnStructure(name, isPrimary, sqlType, length, notNull, defaultValue);
            columnStructures.put(structure.getName(), structure);
        }
        rs.close();
        return columnStructures;
    }

    private List<String> getPrimaries(String tableName) throws SQLException {
        List<String> primaries = new ArrayList<>();
        ResultSet rs = getMetaData().getPrimaryKeys(getCatalog(), getSchema(), tableName);
        while (rs.next())
            primaries.add(rs.getString(4));
        rs.close();
        return primaries;
    }

    private Map<String, ConstraintStructure> readConstraints(String tableName) throws SQLException {
        Map<String, ConstraintStructure> constraintStructures = new LinkedHashMap<>();
        ResultSet rs = getMetaData().getImportedKeys(getCatalog(), getSchema(), tableName);
        while (rs.next())
            constraintStructures.put(rs.getString(12), new ConstraintStructure(rs.getString(12), ConstraintType.FOREIGN_KEY, DataBaseInfoBuilder.formDetail(rs.getString(7), rs.getString(8), rs.getString(3), rs.getString(4), null), rs.getShort(11), rs.getShort(10)));
        Map<String, ConstraintStructure> others = readOtherConstraints(tableName);
        constraintStructures.putAll(others);
        rs.close();
        return constraintStructures;
    }

    protected DatabaseMetaData getMetaData() {
        return this.metaData;
    }

    protected int getConstraintType(String value) {
        int result = -1;
        Reflector reflector = Reflector.get();
        Method method = reflector.getMethodByUniquePart(ConstraintType.class, getProvider());
        if (method != null)
            result = reflector.invokeMethod(method, ConstraintType.class, int.class, value);
        return result;
    }

    protected DataSource createDataSource(ConnectionData data) {
        PoolConfig config = data.getConfig();
        String provider = config.getProvider();
        ComboPooledDataSource result = new ComboPooledDataSource();
        return null;
    }

    protected void setConnection(Connection connection) throws SQLException {
        this.connection = connection;
        this.metaData = connection.getMetaData();
    }

    protected void setProvider(String provider) {
        this.provider = provider;
    }

    protected String getProvider() {
        return this.provider;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected abstract String getSchema();

    protected abstract String getCatalog() throws SQLException;

    protected abstract String getDataBaseName();

    protected abstract Map<String, ConstraintStructure> readOtherConstraints(String table) throws SQLException;

}
