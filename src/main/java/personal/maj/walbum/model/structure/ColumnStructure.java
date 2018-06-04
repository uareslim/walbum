package personal.maj.walbum.model.structure;

/**
 * Created by MAJ on 2018/3/28.
 */
public class ColumnStructure extends AbstractStructure {

    private String name;

    private boolean isPrimary;

    private int sqlType;

    private int length;

    private boolean notNull;

    private String defaultValue;

    public ColumnStructure() {
    }

    public ColumnStructure(String name) {
        this.name = name;
    }

    public ColumnStructure(String name, boolean isPrimary, int sqlType, int length, boolean notNull, String defaultValue) {
        this.name = name;
        this.isPrimary = isPrimary;
        this.sqlType = sqlType;
        this.length = length;
        this.notNull = notNull;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}
