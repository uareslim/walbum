package personal.maj.walbum.annotations.rdbms;

import personal.maj.walbum.annotations.constant.Constant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.DatabaseMetaData;

/**
 * Created by MAJ on 2018/4/2.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityColumn {
    public String name() default Constant.UNDEFINED_STRING;
    public boolean lazy() default true;
    public boolean createKey() default true;
    public short updateRule() default DatabaseMetaData.importedKeyNoAction;
    public short deleteRule() default DatabaseMetaData.importedKeySetNull;
}
