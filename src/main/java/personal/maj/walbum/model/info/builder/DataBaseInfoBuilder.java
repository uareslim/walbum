package personal.maj.walbum.model.info.builder;

import personal.maj.walbum.common.Reflector;
import personal.maj.walbum.exception.DefinationException;
import personal.maj.walbum.model.info.DataBaseInfo;

import java.lang.reflect.Method;

/**
 * Created by MAJ on 2018/5/22.
 */
public interface DataBaseInfoBuilder {

    public static DataBaseInfoBuilder get(String provider, String databaseName) {
        Reflector reflector = Reflector.get();
        String packageName = DataBaseInfoBuilder.class.getPackage().getName();
        String className = packageName + "." + provider + DataBaseInfoBuilder.class.getSimpleName();
        Class subClass = reflector.getUniqueClass(className);
        Method method = reflector.getMethod(subClass, "get");
        return reflector.invokeMethod(method, subClass, DataBaseInfoBuilder.class, provider, databaseName);
    }

    public static String formDetail(String tableName, String columnName, String referencedTable, String referencedColumnName, String checkCondition) {
        StringBuilder sb = new StringBuilder();
        if (columnName != null) {
            sb.append("tableName=").append(tableName).append("\n");
            sb.append("columnName=").append(columnName).append("\n");
            if (referencedTable != null && referencedColumnName != null) {
                sb.append("referencedTable=").append(referencedTable).append("\n");
                sb.append("referencedColumnName=").append(referencedColumnName).append("\n");
            }
            if (checkCondition != null)
                sb.append("checkCondition=").append(checkCondition).append("\n");
        }
        return sb.toString();
    }

    public DataBaseInfo getDataBaseInfo() throws DefinationException;
}
