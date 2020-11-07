package ast;

public class IntAstType extends AstType {
    final String id = "int";

    public String id() {
        return id;
    }
    public IntAstType() {
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
