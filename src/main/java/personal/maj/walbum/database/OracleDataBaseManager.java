package personal.maj.walbum.database;

import personal.maj.walbum.model.data.ConnectionData;
import personal.maj.walbum.model.info.builder.DataBaseInfoBuilder;
import personal.maj.walbum.model.structure.ConstraintStructure;
import personal.maj.walbum.model.structure.constant.ConstraintType;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * Created by MAJ on 2018/3/29.
 */
public class OracleDataBaseManager extends AbstractDataBaseManager {

    private static final Map<String, OracleDataBaseManager> INSTANCES = new HashMap<>();

    private static final String DRIVER_CLASS = "oracle.jdbc.driver.OracleDriver";

    private DataSource dataSource;

    private ConnectionData connectionData;

    private OracleDataBaseManager(ConnectionData connectionData) throws SQLException {
        this.setProvider(connectionData.getProvider());
        this.connectionData = connectionData;
        setDataSource(createDataSource(connectionData));
        this.setConnection(newConnection());
    }

    public static OracleDataBaseManager get(ConnectionData connectionData) throws SQLException {
        OracleDataBaseManager result = INSTANCES.get(connectionData.getDbName());
        result = result == null ? new OracleDataBaseManager(connectionData) : result;
        INSTANCES.put(connectionData.getDbName(), result);
        return result;
    }

    @Override
    protected String getSchema() {
        return connectionData.getUserName();
    }

    @Override
    protected String getCatalog() throws SQLException {
        return getConnection().getCatalog();
    }

    @Override
    protected String getDataBaseName() {
        return getSchema();
    }

    @Override
    protected Map<String, ConstraintStructure> readOtherConstraints(String tableName) throws SQLException {
        Map<String, ConstraintStructure> constraintStructures = new LinkedHashMap<>();
        PreparedStatement ps = getConnection().prepareStatement("SELECT c.CONSTRAINT_NAME,c.TABLE_NAME,k.COLUMN_NAME,c.CONSTRAINT_TYPE,c.SEARCH_CONDITION FROM USER_CONSTRAINTS c INNER JOIN USER_CONS_COLUMNS k ON c.CONSTRAINT_NAME = k.CONSTRAINT_NAME AND c.TABLE_NAME = k.TABLE_NAME AND c.OWNER = k.OWNER WHERE c.OWNER = ? AND c.TABLE_NAME = ?");
        ps.setString(1, getSchema());
        ps.setString(2, tableName);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int type = getConstraintType(rs.getString(4));
            String checkCondition = rs.getString(5);
            if (type == ConstraintType.FOREIGN_KEY || type == ConstraintType.PRIMARY_KEY || (type == ConstraintType.CHECK && checkCondition.contains("IS NOT NULL")) || type == -1)
                continue;
            constraintStructures.put(rs.getString(1), new ConstraintStructure(rs.getString(1), type, DataBaseInfoBuilder.formDetail(rs.getString(2), rs.getString(3), null, null, checkCondition), -1, -1));
        }
        rs.close();
        return constraintStructures;
    }

}
