package personal.maj.walbum.annotations.rdbms;

import java.lang.annotation.*;

/**
 * Created by MAJ on 2018/5/17.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Indices {
    public Index[] value();
}
