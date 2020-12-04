package ast;

import java.util.HashMap;
import java.util.Map;

public class ObjectStruct {
    private int sizeInBytes = 8;


    //maps between methode name and it's properties
    private Map<String, MethodeInfo> methodeInfoMap;

    //maps between field and it's offset

    private Map<String, FieldInfo> fieldInfoMap;
    private int lastOffsetFields = 0;
    private int lastOffsetMethodes = 0;
    public int getSizeInBytes() {
        return sizeInBytes;
    }
    public Map<String, MethodeInfo> getMethodeInfoMap() {
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

    private int getLastOffsetMethodes() {
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

    public void addMethode(String methodeName, String args, String ret) {
        MethodeInfo methodeInfo = new MethodeInfo(args, ret, getLastOffsetMethodes());
        methodeInfoMap.put(methodeName, methodeInfo);

    }


}
