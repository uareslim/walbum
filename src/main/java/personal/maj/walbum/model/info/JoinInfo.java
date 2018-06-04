package personal.maj.walbum.model.info;

/**
 * Created by MAJ on 2018/4/8.
 */
public class JoinInfo extends AbstractInfo {

    private String table;

    private String column;

    private boolean export;

    private boolean connectTable;

    public JoinInfo() {
    }

    public JoinInfo(String table, String column, boolean export, boolean connectTable) {
        this.table = table;
        this.column = column;
        this.export = export;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public boolean isExport() {
        return export;
    }

    public void setExport(boolean export) {
        this.export = export;
    }

    public boolean isConnectTable() {
        return connectTable;
    }

    public void setConnectTable(boolean connectTable) {
        this.connectTable = connectTable;
    }
}
