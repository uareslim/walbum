package personal.maj.walbum.model.info.builder;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import personal.maj.walbum.annotations.constant.Constant;
import personal.maj.walbum.annotations.rdbms.*;
import personal.maj.walbum.common.Algorithm;
import personal.maj.walbum.common.Reflector;
import personal.maj.walbum.exception.DefinationException;
import personal.maj.walbum.model.info.JoinInfo;
import personal.maj.walbum.model.info.DataBaseInfo;
import personal.maj.walbum.model.info.TableInfo;
import personal.maj.walbum.model.structure.*;
import personal.maj.walbum.model.structure.constant.ConstraintType;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.DatabaseMetaData;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by MAJ on 2018/4/3.
 */
public abstract class AbstractDataBaseInfoBuilder implements DataBaseInfoBuilder {

    private DataBaseInfo dataBaseInfo = null;

    private Map<Class<?>, Integer> typeMapper = new HashMap<>();

    private Map<Class<?>, Integer> lengthMapper = new HashMap<>();

    private Map<Integer, String> colTypeMapper = new HashMap<>();

    private Map<String, Integer> sqlTypes = sqlTypes();

    private Reflector reflector = Reflector.get();

    private String provider;

    private String databaseName;

    AbstractDataBaseInfoBuilder(String provider, String databaseName) throws DefinationException {
        this.provider = provider;
        this.databaseName = databaseName;
        if (lengthMapper.isEmpty() || colTypeMapper.isEmpty() || typeMapper.isEmpty())
            loadMappers(provider);
    }

    private void parseDataBase(String databaseName, String provider) throws DefinationException {
        List<Class<?>> classes = getTableClasses(databaseName);
        List<Field> entityListFields = new ArrayList<>();
        if (classes != null && !classes.isEmpty()) {
            DataBaseStructure structure = new DataBaseStructure(databaseName, provider);
            this.dataBaseInfo = new DataBaseInfo(databaseName, structure);
            Map<String, TableStructure> tables = new LinkedHashMap<>();
            Map<String, ConstraintStructure> constraints = new LinkedHashMap<>();
            Map<String, IndexStructure> indices = new LinkedHashMap<>();
            for (Class clazz : classes) {
                TableInfo tableInfo = parseTable(clazz, tables, constraints, indices, entityListFields);
                dataBaseInfo.getTableInfoMap().put(clazz.getName(), tableInfo);
            }
            if (!tables.isEmpty())
                structure.setTableStructures(tables);
            if (!constraints.isEmpty())
                structure.setConstraintStructures(constraints);
            if (!indices.isEmpty())
                structure.setIndexStructures(indices);
            if (!entityListFields.isEmpty())
                for (Field field : entityListFields)
                    dealEntityListFields(field);
        }
    }

    private void dealEntityListFields(Field field) throws DefinationException {
        Class<?> clazz = field.getDeclaringClass();
        TableInfo tableInfo = dataBaseInfo.getTableInfoMap().get(clazz.getName());
        String tableName = tableInfo.getStructure().getName();
        EntityList listAnno = field.getDeclaredAnnotation(EntityList.class);
        if (listAnno.lazy())
            tableInfo.getLazyFields().add(field.getName());
        String exportColName = listAnno.exportName().equals(Constant.UNDEFINED_STRING) ? field.getName() : listAnno.exportName();
        Field idField = getIdField(clazz);
        String idColName = reflector.getDefinedName(idField, IdColumn.class);
        ColumnStructure exportCol = new ColumnStructure(exportColName, false, toSqlType(idField.getType()), getDefaultLength(idField.getType()), false, null);
        Class<?> fkClass = reflector.getFieldGenericClass(field);
        checkTableAnnotation(fkClass);
        TableInfo fkTableInfo = dataBaseInfo.getTableInfoMap().get(fkClass.getName());
        String exportTableName = listAnno.exportTable().equals(Constant.UNDEFINED_STRING) ? reflector.getDefinedName(fkClass, Table.class) : listAnno.exportTable();
        boolean newTable = !fkTableInfo.getStructure().getName().equals(exportTableName);
        boolean hasRelative = hasRelative(newTable, fkClass, clazz, exportColName, exportTableName);
        TableStructure exportTable = dataBaseInfo.getStructure().getTableStructures().get(exportTableName);
        exportTable = exportTable == null ? new TableStructure(exportTableName) : exportTable;
        if (newTable) {
            TableInfo connectTableInfo = new TableInfo(dataBaseInfo, exportTable);
            Map<String, ColumnStructure> columns = new LinkedHashMap<>();
            JoinInfo self = new JoinInfo(tableName, idColName, false, false);
            connectTableInfo.getJoinInfos().put(exportColName, self);
            columns.put(exportColName, exportCol);
            if (!hasRelative) {
                String fkTableName = reflector.getDefinedName(fkClass, Table.class);
                Field fkIdField = getIdField(fkClass);
                String fkIdColName = reflector.getDefinedName(fkIdField, IdColumn.class);
                if (exportColName.equals(fkIdColName))
                    throw new DefinationException("the defined 'exportName' of @EntityList on " + clazz.getName() + "." + field.getName() + " is conflicted with the name of the foreign table's id column name, just rename it");
                Class<?> relativeType = fkIdField.getType();
                ColumnStructure relativeCol = new ColumnStructure(fkIdColName, false, toSqlType(relativeType), getDefaultLength(relativeType), false, null);
                columns.put(fkIdColName, relativeCol);
                JoinInfo foreign = new JoinInfo(fkTableName, fkIdColName, false, false);
                connectTableInfo.getJoinInfos().put(fkIdColName, foreign);
                if (listAnno.createKey()) {
                    String constraintName = newConstraintName(exportTableName, fkIdColName, ConstraintType.FOREIGN_KEY);
                    String detail = DataBaseInfoBuilder.formDetail(exportTableName, fkIdColName, fkTableName, fkIdColName, null);
                    ConstraintStructure constraint = new ConstraintStructure(constraintName, ConstraintType.FOREIGN_KEY, detail,
                            DatabaseMetaData.importedKeyRestrict, DatabaseMetaData.importedKeySetNull);
                    addConstraintAtEnd(constraint, exportTable);
                }
            }
            exportTable.setColumnStructures(columns);
            dataBaseInfo.getStructure().getTableStructures().put(exportTableName, exportTable);
            tableInfo.getConnectTableInfos().put(exportTableName, connectTableInfo);
        } else {
            if (exportTable.getColumnStructures().keySet().contains(exportColName)) {
                if (!hasRelative)
                    throw new DefinationException("the export table has column named '" + exportColName + "', rename the @EntityList on '" + clazz.getName() + "." + field.getName() + "'");
                else
                    return;
            } else
                exportTable.getColumnStructures().put(exportColName, exportCol);
        }
        if (listAnno.createKey()) {
            String constraintName = newConstraintName(exportTableName, exportColName, ConstraintType.FOREIGN_KEY);
            String detail = DataBaseInfoBuilder.formDetail(exportTableName, exportColName, tableName, idColName, null);
            ConstraintStructure constraint = new ConstraintStructure(constraintName, ConstraintType.FOREIGN_KEY, detail,
                    listAnno.deleteRule(), listAnno.updateRule());
            addConstraintAtEnd(constraint, exportTable);
        }
        JoinInfo info = new JoinInfo(exportTableName, exportColName, true, newTable);
        tableInfo.getJoinInfos().put(field.getName(), info);
    }

    private void addConstraintAtEnd(ConstraintStructure constraint, TableStructure table) {
        if (table.getConstraintStructures() == null)
            table.setConstraintStructures(new LinkedHashMap<>());
        table.getConstraintStructures().put(constraint.getName(), constraint);
        if (dataBaseInfo.getStructure().getConstraintStructures() == null)
            dataBaseInfo.getStructure().setConstraintStructures(new LinkedHashMap<>());
        dataBaseInfo.getStructure().getConstraintStructures().put(constraint.getName(), constraint);
    }

    private boolean hasRelative(boolean newTable, Class<?> fkClass, Class<?> tClass, String exportName, String exportTable) throws DefinationException {
        boolean result = false;
        for (Field fkField : reflector.getFields(fkClass)) {
            if (newTable && reflector.hasAnnotation(fkField, EntityList.class) && reflector.getFieldGenericClass(fkField).equals(tClass))
                result |= exportTable.equals(reflector.getAnnotationsExactly(fkField, EntityList.class).get(0).exportTable());
            else if (reflector.hasAnnotation(fkField, EntityColumn.class) && fkField.getType().equals(tClass))
                result |= exportName.equals(reflector.getDefinedName(fkField, EntityColumn.class));
        }
        return result;
    }

    private TableInfo parseTable(Class<?> clazz, Map<String, TableStructure> existedTables, Map<String, ConstraintStructure> existedConstraints, Map<String, IndexStructure> existedIndices, List<Field> entityListFields) throws DefinationException {
        TableInfo tableInfo = null;
        checkTableAnnotation(clazz);
        String tableName = reflector.getDefinedName(clazz, Table.class);
        if (existedTables.keySet().contains(tableName))
            throw new DefinationException("the table name has been occupied ,rename class or just give a table name to '" + clazz.getName() + "'");
        TableStructure tableStructure = new TableStructure(tableName);
        tableInfo = new TableInfo(getDataBaseInfo(), tableStructure);
        Map<String, ColumnStructure> columnStructures = new LinkedHashMap<>();
        Map<String, ConstraintStructure> constraintStructures = new LinkedHashMap<>();
        Map<String, IndexStructure> indexStructures = new LinkedHashMap<>();
        for (Field field : reflector.getFields(clazz)) {
            checkFieldAnnotations(field);
            if (reflector.hasAnnotation(field, EntityList.class))
                entityListFields.add(field);
            else {
                ColumnStructure columnStructure = parseColumn(field, columnStructures);
                if (columnStructure != null) {
                    parseConstraint(field, constraintStructures);
                    parseIndex(field, existedIndices, indexStructures, columnStructure.getName());
                    if (reflector.hasAnnotation(field, IdColumn.class))
                        tableInfo.setIdColumn(columnStructure.getName());
                    if (reflector.hasAnnotation(field, EntityColumn.class)) {
                        if (field.getDeclaredAnnotation(EntityColumn.class).lazy())
                            tableInfo.getLazyFields().add(field.getName());
                        Class<?> fkClass = field.getType();
                        checkTableAnnotation(fkClass);
                        String table = reflector.getDefinedName(fkClass, Table.class);
                        Field fkIdField = getIdField(fkClass);
                        String column = reflector.getDefinedName(fkIdField, IdColumn.class);
                        JoinInfo info = new JoinInfo(table, column, false, false);
                        tableInfo.getJoinInfos().put(field.getName(), info);
                    }
                    tableInfo.getFieldColumnMap().put(field.getName(), columnStructure.getName());
                }
            }
        }
        if (!columnStructures.isEmpty())
            tableStructure.setColumnStructures(columnStructures);
        if (!constraintStructures.isEmpty()) {
            tableStructure.setConstraintStructures(constraintStructures);
            existedConstraints.putAll(constraintStructures);
        }
        if (!indexStructures.isEmpty()) {
            tableStructure.setIndexStructures(indexStructures);
            existedIndices.putAll(indexStructures);
        }
        existedTables.put(tableName, tableStructure);
        return tableInfo;
    }

    private ColumnStructure parseColumn(Field field, Map<String, ColumnStructure> existed) throws DefinationException {
        ColumnStructure columnStructure = null;
        if (reflector.hasAnnotation(field, Column.class)) {
            Column columnAnno = field.getAnnotation(Column.class);
            String columnName = columnAnno.name().equals(Constant.UNDEFINED_STRING) ? field.getName() : columnAnno.name();
            columnStructure = new ColumnStructure(columnName);
            int sqlType = toSqlType(field.getType());
            columnStructure.setSqlType(sqlType);
            int length = columnAnno.length() == Constant.UNDEFINED_NUMBER ? getDefaultLength(field.getType()) : columnAnno.length();
            columnStructure.setLength(length);
            columnStructure.setPrimary(columnAnno.primary());
            columnStructure.setNotNull(columnAnno.notNull());
            String defaultValue = columnAnno.defaultValue().equals(Constant.UNDEFINED_STRING) ? null : columnAnno.defaultValue();
            columnStructure.setDefaultValue(defaultValue);
        } else if (reflector.hasAnnotation(field, IdColumn.class)) {
            String columnName = reflector.getDefinedName(field, IdColumn.class);
            columnStructure = new ColumnStructure(columnName, true, toSqlType(field.getType()), getDefaultLength(field.getType()), true, null);
        } else if (reflector.hasAnnotation(field, EntityColumn.class)) {
            String columnName = reflector.getDefinedName(field, EntityColumn.class);
            Class<?> fkClass = field.getType();
            checkTableAnnotation(fkClass);
            Field fkIdField = getIdField(fkClass);
            columnStructure = new ColumnStructure(columnName, false, toSqlType(fkIdField.getType()), getDefaultLength(fkIdField.getType()), false, null);
        }
        if (columnStructure != null) {
            if (existed.keySet().contains(columnStructure.getName()))
                throw new DefinationException("the column name '" + columnStructure.getName() + "' has been occupied in '" + field.getDeclaringClass().getName() + "'");
            existed.put(columnStructure.getName(), columnStructure);
        }
        return columnStructure;
    }

    private void parseConstraint(Field field, Map<String, ConstraintStructure> existed) throws DefinationException {
        Map<String, ConstraintStructure> result = new LinkedHashMap<>();
        Class<?> tableClass = field.getDeclaringClass();
        checkTableAnnotation(tableClass);
        String tableName = reflector.getDefinedName(tableClass, Table.class);
        if (reflector.hasAnnotation(field, Column.class)) {
            Column columnAnno = field.getAnnotation(Column.class);
            String columnName = columnAnno.name().equals(Constant.UNDEFINED_STRING) ? field.getName() : columnAnno.name();
            if (columnAnno.unique()) {
                String constraintName = newConstraintName(tableName, columnName, ConstraintType.UNIQUE);
                String detail = DataBaseInfoBuilder.formDetail(tableName, columnName, null, null, null);
                ConstraintStructure uniqueConstraint = new ConstraintStructure(constraintName, ConstraintType.UNIQUE, detail,
                        -1, -1);
                result.put(constraintName, uniqueConstraint);
            }
            if (!columnAnno.check().equals(Constant.UNDEFINED_STRING)) {
                String constraintName = newConstraintName(tableName, columnName, ConstraintType.CHECK);
                String detail = DataBaseInfoBuilder.formDetail(tableName, columnName, null, null, columnAnno.check());
                ConstraintStructure checkConstraint = new ConstraintStructure(constraintName, ConstraintType.CHECK, detail,
                        -1, -1);
                result.put(constraintName, checkConstraint);
            }
        } else if (reflector.hasAnnotation(field, EntityColumn.class)) {
            EntityColumn entityAnno = field.getDeclaredAnnotation(EntityColumn.class);
            String columnName = entityAnno.name().equals(Constant.UNDEFINED_STRING) ? field.getName() : entityAnno.name();
            if (entityAnno.createKey()) {
                Class<?> fkClass = field.getType();
                checkTableAnnotation(fkClass);
                String fkTableName = reflector.getDefinedName(fkClass, Table.class);
                Field fkIdField = getIdField(fkClass);
                String fkIdName = reflector.getDefinedName(fkIdField, IdColumn.class);
                String constraintName = newConstraintName(tableName, columnName, ConstraintType.FOREIGN_KEY);
                String detail = DataBaseInfoBuilder.formDetail(tableName, columnName, fkTableName, fkIdName, null);
                ConstraintStructure foreignKeyConstraint = new ConstraintStructure(constraintName, ConstraintType.FOREIGN_KEY, detail,
                        entityAnno.deleteRule(), entityAnno.updateRule());
                result.put(constraintName, foreignKeyConstraint);
            }
        }
        existed.putAll(result);
    }

    private void parseIndex(Field field, Map<String, IndexStructure> allExisted, Map<String, IndexStructure> tableExisted, String columnName) throws DefinationException {
        if (reflector.hasAnnotation(field, Index.class)) {
            for (Index indexAnno : reflector.getAnnotationsExactly(field, Index.class)) {
                String indexName = indexAnno.name();
                if (allExisted.keySet().contains(indexName))
                    throw new DefinationException("the index name '" + indexName + "' in '" + field.getDeclaringClass().getName() + "." + field.getName() + "' has been occupied");
                IndexStructure existedStructure = tableExisted.get(indexName);
                if (existedStructure != null) {
                    if (indexAnno.clustered() && existedStructure.isClustered()) {
                        if (existedStructure.getColumnNames().contains(columnName))
                            throw new DefinationException("duplicate @Index found ,you should remove the duplicate one");
                        existedStructure.getColumnNames().add(columnName);
                    } else
                        throw new DefinationException("the declared index '" + existedStructure.getName() + "' in '" + field.getDeclaringClass().getName() + "' is a clustered index, but not define it as true on the fields");
                } else
                    tableExisted.put(indexName, new IndexStructure(indexName, indexAnno.clustered(), columnName));
            }
        }
    }

    private void checkFieldAnnotations(Field field) throws DefinationException {
        int singleCount = reflector.existCount(field, Column.class, IdColumn.class, EntityColumn.class, EntityList.class);
        int unCoexistedCount = reflector.existCount(field, Index.class, EntityList.class);
        if (singleCount > 1)
            throw new DefinationException("@Column,@IdColumn,@EntityColumn,@EntityList can just has one on field '" + field.getDeclaringClass().getName() + "." + field.getName() + "'");
        else if (unCoexistedCount == 2)
            throw new DefinationException("@Index,@EntityList can just has one on field '" + field.getDeclaringClass().getName() + "." + field.getName() + "'");
        else if (singleCount == 0 && reflector.hasAnnotation(field, Index.class))
            throw new DefinationException("@Index can not put alone on field '" + field.getDeclaringClass().getName() + "." + field.getName() + "'");
        if (reflector.hasAnnotation(field, EntityList.class) && !List.class.isAssignableFrom(field.getType()))
            throw new DefinationException("the field '" + field.getDeclaringClass().getName() + "." + field.getName() + "' has @EntityList but not declared as '" + List.class.getName() + "'");
        if (reflector.hasAnnotation(field, IdColumn.class) && !field.getType().equals(Integer.class) && !field.getType().equals(Long.class))
            throw new DefinationException("the @IdColumn can just put on the field declared as '" + Integer.class.getName() + "' or '" + Long.class.getName() + "'");
    }

    private void checkTableAnnotation(Class<?> clazz) throws DefinationException {
        if (!reflector.hasAnnotation(clazz, Table.class))
            throw new DefinationException("@Table not find on '" + clazz.getName() + "'");
    }

    private Field getIdField(Class<?> clazz) throws DefinationException {
        if (reflector.hasAnnotation(clazz, Table.class)) {
            for (Field field : reflector.getFields(clazz)) {
                if (reflector.hasAnnotation(field, IdColumn.class)) {
                    return field;
                }
            }
            throw new DefinationException("No @IdColumn found in '" + clazz.getName() + "' , please define it");
        } else
            throw new DefinationException("@Table not find on param '" + clazz.getName() + "'");
    }

    private int getDefaultLength(Class<?> javaType) throws DefinationException {
        Integer result = lengthMapper.get(javaType);
        if (result != null)
            return result;
        else
            throw new DefinationException("Unsupported type :'" + javaType.getName() + "' found, if you want define the relation between java type and data type ,define an type-map xml and set it as default type-map xml. for example, like TypeMapper.xml");
    }

    private int toSqlType(Class<?> javaType) throws DefinationException {
        Integer result = typeMapper.get(javaType);
        if (result != null)
            return result;
        else
            throw new DefinationException("Unsupported type :'" + javaType.getName() + "' found, if you want define the relation between java type and data type ,define an type-map xml and set it as default type-map xml. for example, like TypeMapper.xml");
    }

    private void loadMappers(String provider) {
        try {
            URL url = this.getClass().getClassLoader().getResource("TypeMapper.xml");
            File file = new File(url.getFile());
            String xml;
            xml = FileUtils.readFileToString(file);
            Document document = DocumentHelper.parseText(xml);
            Element root = document.getRootElement();
            for (Element types : (List<Element>) root.elements()) {
                if (provider.equals(types.attribute("provider").getText())) {
                    for (Element element : (List<Element>) types.elements()) {
                        String className = element.getText();
                        Class clazz = Class.forName(className);
                        Integer sqlType = this.sqlTypes.get(element.attribute("sqlType").getText());
                        Integer length = Integer.parseInt(element.attribute("length").getText());
                        String colType = element.attribute("colType").getText();
                        java.sql.Types.class.getFields();
                        lengthMapper.put(clazz, length);
                        typeMapper.put(clazz, sqlType);
                        colTypeMapper.put(sqlType, colType);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Integer> sqlTypes() {
        Map<String, Integer> map = new HashMap<>();
        try {
            Class clazz = java.sql.Types.class;
            for (Field field : clazz.getFields())
                map.put(clazz.getName() + "." + field.getName(), (Integer) field.get(clazz));
            return map;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return map;
        }
    }

    @Override
    public DataBaseInfo getDataBaseInfo() throws DefinationException {
        if (dataBaseInfo == null)
            parseDataBase(databaseName, provider);
        return dataBaseInfo;
    }

    private String newConstraintName(String table, String column, int type) {
        String cstType;
        switch (type) {
            case ConstraintType.CHECK:
                cstType = "CK_";
                break;
            case ConstraintType.UNIQUE:
                cstType = "UQ_";
                break;
            case ConstraintType.FOREIGN_KEY:
                cstType = "FK_";
                break;
            default:
                cstType = "";
        }
        String uniqueName = table + "_" + column;
        Integer hash = Algorithm.get().fnvHash(uniqueName);
        return "CST_" + cstType + Integer.toHexString(hash);
    }

    private List<Class<?>> getTableClasses(String databaseName) throws DefinationException {
        List<Class<?>> result = new ArrayList<>();
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String classpath = URLDecoder.decode(url.getFile());
        List<Class<?>> classes = reflector.getClasses(classpath, Table.class);
        for (Class<?> clazz : classes) {
            checkTableAnnotation(clazz);
            String definedDatabase = reflector.getAnnotationsExactly(clazz, Table.class).get(0).database();
            if (definedDatabase.equals(databaseName) || definedDatabase.equals(Constant.UNDEFINED_STRING))
                result.add(clazz);
        }
        return result;
    }

}
