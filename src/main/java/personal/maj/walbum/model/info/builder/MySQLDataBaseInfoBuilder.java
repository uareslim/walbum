package personal.maj.walbum.model.info.builder;

import personal.maj.walbum.exception.DefinationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MAJ on 2018/5/2.
 */
public class MySQLDataBaseInfoBuilder extends AbstractDataBaseInfoBuilder {

    private static final Map<String, AbstractDataBaseInfoBuilder> INSTANCES = new HashMap<>();

    private MySQLDataBaseInfoBuilder(String provider, String databaseName) throws DefinationException {
        super(provider, databaseName);
    }

    public static AbstractDataBaseInfoBuilder get(String provider, String databaseName) throws DefinationException {
        AbstractDataBaseInfoBuilder result = INSTANCES.get(databaseName);
        result = result == null ? new MySQLDataBaseInfoBuilder(provider, databaseName) : result;
        INSTANCES.put(databaseName, result);
        return result;
    }

}
