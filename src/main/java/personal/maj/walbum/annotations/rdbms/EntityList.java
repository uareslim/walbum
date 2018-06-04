package personal.maj.walbum.annotations.rdbms;

import personal.maj.walbum.annotations.constant.Constant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.DatabaseMetaData;

/**
 * Created by MAJ on 2018/5/3.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityList {
    public String exportName() default Constant.UNDEFINED_STRING;
    public boolean lazy() default true;
    public String exportTable() default Constant.UNDEFINED_STRING;
    public boolean createKey() default true;
    public short updateRule() default DatabaseMetaData.importedKeyRestrict;
    public short deleteRule() default DatabaseMetaData.importedKeySetNull;
}
