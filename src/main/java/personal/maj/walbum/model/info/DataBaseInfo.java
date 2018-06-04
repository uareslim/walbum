package personal.maj.walbum.model.info;

import personal.maj.walbum.model.structure.DataBaseStructure;

import java.util.*;

/**
 * Created by MAJ on 2018/4/18.
 */
public class DataBaseInfo extends AbstractInfo {

    private String databaseName;

    private DataBaseStructure structure;

    private Map<String, TableInfo> tableInfoMap = new LinkedHashMap<>();

    public DataBaseInfo() {
    }

    public DataBaseInfo(String databaseName) {
        this.databaseName = databaseName;
    }

    public DataBaseInfo(String databaseName, DataBaseStructure structure) {
        this.databaseName = databaseName;
        this.structure = structure;
    }

    public DataBaseStructure getStructure() {
        return structure;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public Map<String, TableInfo> getTableInfoMap() {
        return tableInfoMap;
    }

}
