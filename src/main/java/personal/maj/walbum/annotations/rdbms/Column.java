package personal.maj.walbum.annotations.rdbms;

import personal.maj.walbum.annotations.constant.Constant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by MAJ on 2018/3/28.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    public String name() default Constant.UNDEFINED_STRING;
    public boolean primary() default false;
    public boolean unique() default false;
    public boolean notNull() default false;
    public String check() default Constant.UNDEFINED_STRING;
    public String defaultValue() default Constant.UNDEFINED_STRING;
    public int length() default Constant.UNDEFINED_NUMBER;
}
