package ast;

public class IntArrayAstType extends AstType {
    final String id = "intArray";

    public String id() {
        return id;
    }

    public IntArrayAstType() {
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
