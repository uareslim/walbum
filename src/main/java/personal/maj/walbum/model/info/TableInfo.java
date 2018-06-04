package personal.maj.walbum.model.info;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import personal.maj.walbum.model.structure.TableStructure;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by MAJ on 2018/4/18.
 */
public class TableInfo extends AbstractInfo {

    private DataBaseInfo dataBaseInfo;

    private TableStructure structure;

    private String idColumn;

    private Map<String, JoinInfo> joinInfos = new LinkedHashMap<>();

    private Map<String, TableInfo> connectTableInfos = new LinkedHashMap<>();

    private Set<String> lazyFields = new LinkedHashSet<>();

    private BiMap<String,String> fieldColumnMap = HashBiMap.create();

    public TableInfo() {
    }

    public TableInfo(DataBaseInfo dataBaseInfo, TableStructure structure) {
        this.dataBaseInfo = dataBaseInfo;
        this.structure = structure;
    }

    public TableStructure getStructure() {
        return structure;
    }

    public String getIdColumn() {
        return idColumn;
    }

    public void setIdColumn(String idColumn) {
        this.idColumn = idColumn;
    }

    public DataBaseInfo getDataBaseInfo() {
        return dataBaseInfo;
    }

    public Map<String, JoinInfo> getJoinInfos() {
        return joinInfos;
    }

    public Set<String> getLazyFields() {
        return lazyFields;
    }

    public BiMap<String, String> getFieldColumnMap() {
        return fieldColumnMap;
    }

    public Map<String, TableInfo> getConnectTableInfos() {
        return connectTableInfos;
    }
}
