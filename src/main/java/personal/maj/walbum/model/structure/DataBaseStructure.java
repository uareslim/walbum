package personal.maj.walbum.model.structure;

import java.util.Map;

/**
 * Created by MAJ on 2018/3/28.
 */
public class DataBaseStructure extends AbstractStructure {

    private String name;

    private String provider;

    private Map<String, TableStructure> tableStructures;

    private Map<String, ConstraintStructure> constraintStructures;

    private Map<String, IndexStructure> indexStructures;

    public DataBaseStructure() {
    }

    public DataBaseStructure(String name, String provider) {
        this.name = name;
        this.provider = provider;
    }

    public Map<String, TableStructure> getTableStructures() {
        return tableStructures;
    }

    public void setTableStructures(Map<String, TableStructure> tableStructures) {
        this.tableStructures = tableStructures;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Map<String, ConstraintStructure> getConstraintStructures() {
        return constraintStructures;
    }

    public void setConstraintStructures(Map<String, ConstraintStructure> constraintStructures) {
        this.constraintStructures = constraintStructures;
    }

    public Map<String, IndexStructure> getIndexStructures() {
        return indexStructures;
    }

    public void setIndexStructures(Map<String, IndexStructure> indexStructures) {
        this.indexStructures = indexStructures;
    }
}
