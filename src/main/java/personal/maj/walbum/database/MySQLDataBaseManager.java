package personal.maj.walbum.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import personal.maj.walbum.model.data.ConnectionData;
import personal.maj.walbum.model.info.builder.DataBaseInfoBuilder;
import personal.maj.walbum.model.structure.ConstraintStructure;
import personal.maj.walbum.model.structure.constant.ConstraintType;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by MAJ on 2018/3/29.
 */
public class MySQLDataBaseManager extends AbstractDataBaseManager {

    private static final Map<String, MySQLDataBaseManager> INSTANCES = new HashMap<>();

    private static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";

    private ConnectionData connectionData;

    private MySQLDataBaseManager(ConnectionData connectionData) throws SQLException {
        this.setProvider(connectionData.getProvider());
        this.connectionData = connectionData;
        setDataSource(createDataSource(connectionData));
        this.setConnection(newConnection());
    }

    public static MySQLDataBaseManager get(ConnectionData connectionData) throws SQLException {
        MySQLDataBaseManager result = INSTANCES.get(connectionData.getDbName());
        result = result == null ? new MySQLDataBaseManager(connectionData) : result;
        INSTANCES.put(connectionData.getDbName(), result);
        return result;
    }

    @Override
    protected String getSchema(){
        return getCatalog();
    }

    @Override
    protected String getCatalog() {
        return this.connectionData.getDbName();
    }

    @Override
    protected String getDataBaseName(){
        return getCatalog();
    }

    @Override
    protected Map<String, ConstraintStructure> readOtherConstraints(String tableName) throws SQLException {
        Map<String, ConstraintStructure> constraintStructures = new LinkedHashMap<>();
        PreparedStatement ps = getConnection().prepareStatement("SELECT k.CONSTRAINT_NAME,k.TABLE_NAME,k.COLUMN_NAME,c.CONSTRAINT_TYPE FROM INFORMATION_SCHEMA.`KEY_COLUMN_USAGE` k INNER JOIN INFORMATION_SCHEMA.`TABLE_CONSTRAINTS` c ON k.TABLE_SCHEMA = c.TABLE_SCHEMA AND k.TABLE_NAME = c.TABLE_NAME AND k.CONSTRAINT_NAME = c. CONSTRAINT_NAME WHERE k.TABLE_SCHEMA = ? AND k.TABLE_NAME = ?");
        ps.setString(1, getSchema());
        ps.setString(2, tableName);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int type = getConstraintType(rs.getString(4));
            if (type == ConstraintType.FOREIGN_KEY || type == ConstraintType.PRIMARY_KEY || type == -1)
                continue;
            constraintStructures.put(rs.getString(1), new ConstraintStructure(rs.getString(1), type, DataBaseInfoBuilder.formDetail(rs.getString(2), rs.getString(3), null, null, null), -1, -1));
        }
        rs.close();
        return constraintStructures;
    }

}
