package ast;

import symbolTable.Symbol;
import symbolTable.SymbolTableUtils;
import symbolTable.SymbolTable;
import symbolTable.Type;

import java.util.*;

public class VtableCreator {
    StringBuilder vtables = new StringBuilder();
    final String refPointerString = "i8*";
    final String intString = "i32";
    final String intPointerString = "i32*";
    final String boolString = "i1";

    public static Map<SymbolTable, String> getSymbolTableClassesMap() {
        return symbolTableClassesMap;
    }

    private static Map<SymbolTable, String> symbolTableClassesMap;
    private static Map<String, ObjectStruct> objectStructMap;


    public static Map<String, ObjectStruct> getObjectStructMap() {
        return objectStructMap;
    }

    public String createVtableAndObjectsStruct() {
        objectStructMap = new HashMap<>();
        inverseMap(symbolTable.SymbolTableUtils.getSymbolTableClassMap_real());
        StringBuilder stringBuilder = new StringBuilder();
        int countMethods = 0;
        //for each class in the program
        for (Map.Entry<String, SymbolTable> entry : symbolTable.SymbolTableUtils.getSymbolTableClassMap_real().entrySet()) {
            String className = entry.getKey();
            SymbolTable symbolTable = entry.getValue();
            List<MethodeRow> allClassMethods = new ArrayList<MethodeRow>();
            List<Field> allFields = new ArrayList<>();
            Set<String> methodsNames = new HashSet<>();
            //for each methode in this class
            for (Map.Entry<String, Symbol> symbolTableRow : symbolTable.getEntries().entrySet()) {
                Symbol symbol = symbolTableRow.getValue();
                if (!symbol.getType().equals(Type.METHOD)) {
                    Field field = new Field();
                    extractFieldFields(field, symbol, allFields);
                } else {
                    countMethods++;
                    MethodeRow methodeRow = new MethodeRow();
                    extractMethodFileds(symbol, methodeRow, allClassMethods, methodsNames);
                    methodeRow.setClassName(className);
                }
            }
            //look for methods the class inherits
            countMethods += findAllmethodsAndFields(symbolTable, allClassMethods, methodsNames, allFields);
            addToObjectsStructMap(className, allClassMethods, allFields);

            //concatenate the vtable of this class
            vtableHeader(countMethods, className, stringBuilder);
            vtableContent(allClassMethods, stringBuilder);
        }

        return stringBuilder.toString();
    }

    private void extractFieldFields(Field field, Symbol symbol, List<Field> allFields) {
        field.setType(convertAstTypeToLLVMRepresention(symbol.getDecl().get(0)));
        field.setSize(convertAstTypeToSize(symbol.getDecl().get(0)));
        field.setFieldName(symbol.getSymbolName());
        allFields.add(field);
    }

    public void addToObjectsStructMap(String className, List<MethodeRow> methodeRowList, List<Field> fieldList) {
        ObjectStruct objectStruct = new ObjectStruct();
        for (MethodeRow methodeRow : methodeRowList) {
            methodeRow.createArgsString();
            objectStruct.addMethode(methodeRow.getMethodeName(), methodeRow.getArgs(), methodeRow.getRetType());
        }
        for (Field field : fieldList) {
            objectStruct.addField(field.getFieldName(), field.getType(), field.getSize());
        }
        objectStructMap.put(className, objectStruct);
    }

    public void vtableContent(List<MethodeRow> methodeRowList, StringBuilder stringBuilder) {
        int i;
        for (i = 0; i < methodeRowList.size() - 1; i++) {
            stringBuilder.append(methodeRowList.get(i).toString());
            stringBuilder.append(",\n");
        }
        if (methodeRowList.size() > 0) {
            stringBuilder.append(methodeRowList.get(methodeRowList.size() - 1).toString());
        }
        stringBuilder.append("\n]");
    }

    public void vtableHeader(int funcsNum, String className, StringBuilder stringBuilder) {
        stringBuilder.append("@." + className + "_vtable = global [" + funcsNum + " x " + refPointerString + "] [\n");
    }

    public void extractMethodFileds(Symbol symbol, MethodeRow methodeRow, List<MethodeRow> methodsList, Set<String> methodsNames) {
        extractArgsTypes(symbol, methodeRow);
        extractRetType(symbol, methodeRow);
        extractMethodeName(symbol, methodeRow);
        methodsList.add(methodeRow);
        methodsNames.add(symbol.getSymbolName());
    }

    public void extractMethodeName(Symbol symbol, MethodeRow methodeRow) {
        String methodName = symbol.getSymbolName();
        methodeRow.setMethodeName(methodName);
    }

    public void extractRetType(Symbol symbol, MethodeRow row) {
        String ret = symbol.getDecl().get(0);
        String retType = convertAstTypeToLLVMRepresention(ret);
        row.setRetType(retType);
    }

    public void extractArgsTypes(Symbol symbol, MethodeRow row) {
        List<String> decl = symbol.getDecl();
        for (int i = 1; i < decl.size(); i++) {
            String arg = decl.get(i);
            String argType = convertAstTypeToLLVMRepresention(arg);
            row.addToArgsType(argType);
        }

    }

    public String convertAstTypeToLLVMRepresention(String astType) {
        switch (astType) {
            case "boolean":
                return boolString;
            case "int":
                return intString;
            case "intArray":
                return intPointerString;
            default:
                return refPointerString;
        }

    }

    public int convertAstTypeToSize(String astType) {
        switch (astType) {
            case "boolean":
                return 1;
            case "int":
                return 4;
            default:
                return 8;
        }
    }

    public class Field {
        private String fieldName;
        private String type;
        private int size;


        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public class MethodeRow {
        String retType;
        List<String> argsType;
        String className;
        String methodeName;
        String args;

        public MethodeRow() {
            this.argsType = new ArrayList<String>();
        }

        public String getMethodeName() {
            return methodeName;
        }

        public String getArgs() {
            return args;
        }

        public void createArgsString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("(" + refPointerString);
            for (String argType : argsType) {
                stringBuilder.append(", " + argType);
            }
            stringBuilder.append(")");
            args = stringBuilder.toString();
        }

        public String getRetType() {
            return retType;
        }

        public void setRetType(String retType) {
            this.retType = retType;
        }

        public void addToArgsType(String arg) {
            argsType.add(arg);
        }

        public void setClassName(String className) {
            this.className = className;
        }


        public void setMethodeName(String methodeName) {
            this.methodeName = methodeName;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(refPointerString + " bitcast (" + retType + "(" + refPointerString);
            for (String argType : argsType) {
                stringBuilder.append(", " + argType);
            }
            stringBuilder.append(")* @" + className + "." + methodeName + " to " + refPointerString + ")");
            return stringBuilder.toString();
        }

    }


    public int findAllmethodsAndFields(SymbolTable symbolTable, List<MethodeRow> methodsList, Set<String> methodesNames, List<Field> fieldList) {
        SymbolTable parentSymbolTable = symbolTable.getParentSymbolTable();
        int countMethodes = 0;
        while (parentSymbolTable != null) {
            for (Map.Entry<String, Symbol> symbolTableRow : symbolTable.getEntries().entrySet()) {
                Symbol symbol = symbolTableRow.getValue();
                if (!symbol.getType().equals(Type.METHOD)) {
                    Field field = new Field();
                    field.setType(convertAstTypeToLLVMRepresention(symbol.getDecl().get(0)));
                    field.setFieldName(symbol.getSymbolName());
                    fieldList.add(field);
                }
                if (methodesNames.contains(symbol.getSymbolName())) {
                    continue;
                }
                countMethodes++;
                MethodeRow methodeRow = new MethodeRow();
                extractMethodFileds(symbol, methodeRow, methodsList, methodesNames);
                methodeRow.setMethodeName(symbolTableClassesMap.get(symbolTable));

            }
            parentSymbolTable = parentSymbolTable.getParentSymbolTable();
        }
        return countMethodes;
    }

    public void inverseMap(Map<String, SymbolTable> map) {
        Map<SymbolTable, String> inverseMap = new HashMap<>();
        for (String className : map.keySet()) {
            inverseMap.put(map.get(className), className);
        }
        symbolTableClassesMap = inverseMap;

    }

}
