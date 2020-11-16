package ast;

import javax.xml.bind.annotation.XmlElement;

public class AssignStatement extends Statement {
    @XmlElement(required = true)
    private String lv;
    @XmlElement(required = true)
    private ExprWrapper rv;

    // for deserialization only!
    public AssignStatement() {
    }

    public AssignStatement(String lv, Expr rv) {
        this.lv = lv;
        this.rv = new ExprWrapper(rv);
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    public String lv() {
        return lv;
    }

    public void setLv(String lv) {
        this.lv = lv;
    }

    public Expr rv() {
        return rv.e;
    }
}
