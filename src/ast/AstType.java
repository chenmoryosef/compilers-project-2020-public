package ast;

public abstract class AstType extends AstNode {
    private String id;

    public String id() {
        return id;
    }

    public AstType() {
    }
}
