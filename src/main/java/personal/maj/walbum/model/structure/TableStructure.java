package personal.maj.walbum.model.structure;

import java.util.Map;

/**
 * Created by MAJ on 2018/3/28.
 */
public class TableStructure extends AbstractStructure {

    private String name;

    private Map<String, ColumnStructure> columnStructures;

    private Map<String, ConstraintStructure> constraintStructures;

    private Map<String, IndexStructure> indexStructures;

    public TableStructure() {
    }

    public TableStructure(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, ColumnStructure> getColumnStructures() {
        return columnStructures;
    }

    public void setColumnStructures(Map<String, ColumnStructure> columnStructures) {
        this.columnStructures = columnStructures;
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
