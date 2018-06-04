package personal.maj.walbum.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import personal.maj.walbum.common.Reflector;
import personal.maj.walbum.model.data.ConnectionData;
import personal.maj.walbum.model.structure.DataBaseStructure;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Created by MAJ on 2018/5/22.
 */
public interface DataBaseManager {

    public static DataBaseManager get(ConnectionData connectionData) {
        Reflector reflector = Reflector.get();
        String provider = connectionData.getProvider();
        String packageName = DataBaseManager.class.getPackage().getName();
        String className = packageName + "." + provider + DataBaseManager.class.getSimpleName();
        Class subClass = reflector.getUniqueClass(className);
        Method method = reflector.getMethod(subClass, "get");
        return reflector.invokeMethod(method, subClass, DataBaseManager.class, connectionData);
    }

    public DataBaseStructure readDataBase() throws SQLException;

    public void releaseConnection(Connection connection) throws SQLException;

    public Connection getConnection() throws SQLException;

    public Connection newConnection() throws SQLException;
}
