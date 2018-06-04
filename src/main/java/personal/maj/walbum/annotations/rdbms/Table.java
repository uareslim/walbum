package personal.maj.walbum.annotations.rdbms;

import personal.maj.walbum.annotations.constant.Constant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by MAJ on 2018/3/28.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {
    public String name() default Constant.UNDEFINED_STRING;
    public String database() default Constant.UNDEFINED_STRING;
}
