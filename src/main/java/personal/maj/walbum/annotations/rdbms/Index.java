package personal.maj.walbum.annotations.rdbms;

import java.lang.annotation.*;

/**
 * Created by MAJ on 2018/3/28.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Indices.class)
public @interface Index {
    public String name();
    public boolean clustered() default false;
}