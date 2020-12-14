package ast;

import java.util.HashMap;
import java.util.Map;

public class ObjectStruct {
    private int sizeInBytes = 8;


    //maps between methode name and it's properties
    private Map<String, MethodInfo> methodeInfoMap;

    //maps between field and it's offset

    private Map<String, FieldInfo> fieldInfoMap;
    private int lastOffsetFields = 8;
    private int lastOffsetMethodes = 0;
    public int getSizeInBytes() {
        return sizeInBytes;
    }
    public Map<String, MethodInfo> getMethodeInfoMap() {
        return methodeInfoMap;
    }

    public Map<String, FieldInfo> getFieldInfoMap() {
        return fieldInfoMap;
    }

    public ObjectStruct() {
        this.fieldInfoMap = new HashMap<>();
        this.methodeInfoMap = new HashMap<>();
    }

    private int getLastOffsetFields(int fieldSize) {
        int result = lastOffsetFields;
        lastOffsetFields += fieldSize;

        return result;
    }

    private int getLastOffsetMethods() {
        return lastOffsetMethodes++;
    }

    private void incrementSize(int num) {
        sizeInBytes += num;
    }

    public void addField(String fieldName, String fieldType, int fieldSize) {
        FieldInfo fieldInfo = new FieldInfo(fieldType, getLastOffsetFields(fieldSize));
        incrementSize(fieldSize);
        fieldInfoMap.put(fieldName, fieldInfo);
    }

    public void addMethod(String methodName, String args, String ret) {
        MethodInfo methodInfo = new MethodInfo(args, ret, getLastOffsetMethods());
        methodeInfoMap.put(methodName, methodInfo);
    }
}
