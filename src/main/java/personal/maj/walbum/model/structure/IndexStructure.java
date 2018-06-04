package personal.maj.walbum.model.structure;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by MAJ on 2018/3/28.
 */
public class IndexStructure extends AbstractStructure {

    private String name;

    private boolean clustered;

    private Set<String> columnNames;

    public IndexStructure() {}

    public IndexStructure(String name) {
        this.name = name;
    }

    public IndexStructure(String name, Boolean clustered, String... columnNames) {
        this.name = name;
        this.clustered = clustered;
        this.columnNames = new LinkedHashSet<>(Arrays.asList(columnNames));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isClustered() {
        return clustered;
    }

    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }

    public Set<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(Set<String> columnNames) {
        this.columnNames = columnNames;
    }

}
