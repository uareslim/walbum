package personal.maj.walbum.model.info.builder;

import personal.maj.walbum.exception.DefinationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MAJ on 2018/5/2.
 */
public class OracleDataBaseInfoBuilder extends AbstractDataBaseInfoBuilder {

    private static final Map<String, AbstractDataBaseInfoBuilder> INSTANCES = new HashMap<>();

    private OracleDataBaseInfoBuilder(String provider, String databaseName) throws DefinationException {
        super(provider, databaseName);
    }

    public static AbstractDataBaseInfoBuilder get(String provider, String databaseName) throws DefinationException {
        AbstractDataBaseInfoBuilder result = INSTANCES.get(databaseName);
        result = result == null ? new OracleDataBaseInfoBuilder(provider, databaseName) : result;
        INSTANCES.put(databaseName, result);
        return result;
    }

}
