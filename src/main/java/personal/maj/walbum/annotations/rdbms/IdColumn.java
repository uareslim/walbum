package personal.maj.walbum.annotations.rdbms;

import personal.maj.walbum.annotations.constant.Constant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by MAJ on 2018/4/19.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdColumn {
    public String name() default Constant.UNDEFINED_STRING;;
}
