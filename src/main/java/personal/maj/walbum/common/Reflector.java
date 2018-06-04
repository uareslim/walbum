package personal.maj.walbum.common;

import personal.maj.walbum.annotations.constant.Constant;
import personal.maj.walbum.exception.DefinationException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by MAJ on 2018/3/28.
 */
public class Reflector {

    private static Reflector instance = new Reflector();

    private Reflector() {
    }

    public static Reflector get() {
        return instance;
    }

    public List<Field> getFields(Class clazz) {
        List<Field> result = new ArrayList<Field>();
        for (Field field : clazz.getDeclaredFields())
            if (field.getDeclaringClass().equals(clazz))
                result.add(field);
        return result;
    }

    public <T> T getFieldValue(Field field, Object instance, Class<T> valueClass) {
        T result = null;
        try {
            result = (T) field.get(instance);
        } catch (IllegalAccessException e) {
            Logger.getGlobal().info(e.getMessage() + "\ncause:" + e.getCause());
        }
        return result;
    }

    public Method getMethodByUniquePart(Class<?> clazz, String uniqueMethodPart) {
        Method result = null;
        for (Method method : clazz.getDeclaredMethods())
            if (method.getName().contains(uniqueMethodPart))
                result = method;
        return result;
    }

    public Class<?> getUniqueClass(String uniqueClassName) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(uniqueClassName);
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().info(e.getMessage() + "\ncause:" + e.getCause());
        }
        return clazz;
    }

    public Method getMethod(Class<?> clazz, String uniqueMethodName) {
        Method result = null;
        for (Method method : clazz.getDeclaredMethods())
            if (method.getName().equals(uniqueMethodName))
                result = method;
        return result;
    }

    public <T> T invokeMethod(Method method, Object instance, Class<T> returnClass, Object... params) {
        T result = null;
        try {
            result = (T) method.invoke(instance, params);
        } catch (IllegalAccessException e) {
            Logger.getGlobal().info(e.getMessage() + "\ncause:" + e.getCause());
        } catch (InvocationTargetException e) {
            Logger.getGlobal().info(e.getMessage() + "\ncause:" + e.getCause());
        }
        return result;
    }

    public boolean hasAnnotation(AnnotatedElement element, Class<? extends Annotation> annotationClass) {
        return getAnnotations(element, annotationClass).size() > 0;
    }

    public List<Annotation> getAnnotations(AnnotatedElement element, Class<? extends Annotation> annoClass) {
        List<Annotation> result = new ArrayList<>();
        Repeatable repeatable = annoClass.getDeclaredAnnotation(Repeatable.class);
        if (repeatable == null) {
            Annotation t = element.getDeclaredAnnotation(annoClass);
            if (t != null)
                result.add(t);
        } else {
            Annotation[] ts = element.getAnnotationsByType(annoClass);
            result = Arrays.asList(ts);
        }
        return result;
    }

    public <T extends Annotation> List<T> getAnnotationsExactly(AnnotatedElement element, Class<T> returnClass) {
        List<T> result = new ArrayList<>();
        Repeatable repeatable = returnClass.getDeclaredAnnotation(Repeatable.class);
        if (repeatable == null) {
            T t = element.getDeclaredAnnotation(returnClass);
            if (t != null)
                result.add(t);
        } else {
            T[] ts = element.getAnnotationsByType(returnClass);
            result = Arrays.asList(ts);
        }
        return result;
    }

    public int existCount(AnnotatedElement element, Class<? extends Annotation>... classes) {
        int count = 0;
        for (Class<? extends Annotation> clazz : classes)
            if (hasAnnotation(element, clazz))
                count++;
        return count;
    }

    public Class<?> getFieldGenericClass(Field field) {
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        return (Class<?>) type.getActualTypeArguments()[0];
    }

    public Field getDeclaredField(String name, Class clazz) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    public String getDefinedName(AnnotatedElement element, Class<? extends Annotation> clazz) throws DefinationException {
        List<String> list = getAllDefinedName(element, clazz);
        return list.size() > 0 ? list.get(0) : null;
    }

    public List<String> getAllDefinedName(AnnotatedElement element, Class<? extends Annotation> clazz) throws DefinationException {
        Set<String> result = new LinkedHashSet<>();
        if (hasAnnotation(element, clazz)) {
            List<Annotation> annotations = getAnnotations(element, clazz);
            for (Annotation annotation : annotations) {
                InvocationHandler h = Proxy.getInvocationHandler(annotation);
                try {
                    Class<?> aihClass = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
                    Field mvField = aihClass.getDeclaredField("memberValues");
                    mvField.setAccessible(true);
                    Map<String, Object> memberValues = (Map<String, Object>) mvField.get(h);
                    Object value = memberValues.get("name");
                    if (value == null)
                        throw new DefinationException("the value is null, check the annotation '" + clazz.getName() + "' has method 'name' or not");
                    if (value.equals(Constant.UNDEFINED_STRING)) {
                        if (element instanceof Field)
                            result.add(((Field) element).getName());
                        else if (element instanceof Class)
                            result.add(((Class) element).getSimpleName());
                    } else
                        result.add(value.toString());
                    mvField.setAccessible(false);
                } catch (ReflectiveOperationException e) {
                    System.err.println(e.getMessage());
                }
            }
        } else
            throw new DefinationException("Annotation '" + clazz.getName() + "' not found in '" + element.toString() + "'");
        return new ArrayList<>(result);
    }

    public List<Class<?>> getClasses(String path, Class<? extends Annotation> annotation) {
        List<Class<?>> result = new ArrayList<>();
        File file = new File(path);
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                List<Class<?>> subResult = getClasses(f.getPath(), annotation);
                result.addAll(subResult);
            }
        } else if (file.getPath().endsWith(".class")) {
            URL url = Reflector.class.getClassLoader().getResource("");
            File classpathDir = new File(URLDecoder.decode(url.getFile()));
            String classpath = classpathDir.getAbsolutePath();
            String after = file.getPath().replace(classpath + File.separator, "");
            after = after.replace(File.separatorChar, '.');
            try {
                Class aClass = Class.forName(after.substring(0, after.length() - 6), false, Reflector.class.getClassLoader());
                if (hasAnnotation(aClass, annotation))
                    result.add(aClass);
            } catch (ClassNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }
        return result;
    }
}
