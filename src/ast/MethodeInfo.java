package ast;

public class MethodeInfo {

    private String args;
    private String ret;
    private int offset;

    public MethodeInfo(String args, String ret, int offset) {
        this.args = args;
        this.ret = ret;
        this.offset = offset;
    }

    public String getArgs() {
        return args;
    }
    public String getRet() {
        return ret;
    }

    public int getOffset() {
        return offset;
    }

}
