package ast;

public class BoolAstType extends AstType {
    final String id = "boolean";

    public String id() {
        return id;
    }

    public BoolAstType() {
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}

