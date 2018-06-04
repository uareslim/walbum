package personal.maj.walbum.model.structure;

/**
 * Created by MAJ on 2018/3/28.
 */
public class ConstraintStructure extends AbstractStructure {

    private String name;

    private int type;

    private String detail;

    private int deleteRule = -1;

    private int updateRule = -1;

    public ConstraintStructure() {
    }

    public ConstraintStructure(String name) {
        this.name = name;
    }

    public ConstraintStructure(String name, int type, String detail, int deleteRule, int updateRule) {
        this.name = name;
        this.type = type;
        this.detail = detail;
        this.deleteRule = deleteRule;
        this.updateRule = updateRule;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getDeleteRule() {
        return deleteRule;
    }

    public void setDeleteRule(int deleteRule) {
        this.deleteRule = deleteRule;
    }

    public int getUpdateRule() {
        return updateRule;
    }

    public void setUpdateRule(int updateRule) {
        this.updateRule = updateRule;
    }
}
