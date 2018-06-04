package personal.maj.walbum.model.structure.constant;

import personal.maj.walbum.common.Reflector;

import java.lang.reflect.Field;

/**
 * Created by MAJ on 2018/4/3.
 */
public class ConstraintType {
    //in column
    public static final int NOTNULL = 1;
    //in constraint
    public static final int UNIQUE = 2;
    //in constraint
    public static final int CHECK = 4;
    //in column
    public static final int PRIMARY_KEY = 8;
    //in constraint
    public static final int FOREIGN_KEY = 16;
    //in column
    public static final int DEFAULT = 32;

    public static int getType_MySQL(String type) {
        Reflector reflector = Reflector.get();
        for (Field field : reflector.getFields(ConstraintType.class))
            if (field.getName().equals(type.trim().replaceAll(" ", "_")))
                return reflector.getFieldValue(field,ConstraintType.class,int.class);
        return -1;
    }

    public static int getType_Oracle(String type) {
        Reflector reflector = Reflector.get();
        if (type.equals("R"))
            return FOREIGN_KEY;
        else {
            for (Field field : reflector.getFields(ConstraintType.class))
                if (field.getName().startsWith(type))
                    return reflector.getFieldValue(field,ConstraintType.class,int.class);
            return -1;
        }
    }
}
