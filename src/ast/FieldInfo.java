package ast;

public class FieldInfo {

    private String fieldType;
    private int offset;


    public FieldInfo(String fieldType, int offset) {
        this.fieldType = fieldType;
        this.offset = offset;
    }

    public String getFieldType() {
        return fieldType;
    }

    public int getOffset() {
        return offset;
    }

}
