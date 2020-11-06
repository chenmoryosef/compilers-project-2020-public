package symbolTable;

import ast.AstNode;

import java.util.ArrayList;
import java.util.List;

class Properties {
    private List<AstNode> ptrList;
    private List<Symbol> symbolList;
    // Maybe more ...

    public List<Symbol> getSymbolList() {
        return symbolList;
    }

    public Properties(AstNode astNode) {
        this.ptrList = new ArrayList<>();
        this.ptrList.add(astNode);
        this.symbolList = new ArrayList<>();
    }

    public List<AstNode> getPtrList() {
        return ptrList;
    }

    public void addAstNode(AstNode astNode) {
        this.ptrList.add(astNode);
    }

    public void addSymbol(Symbol symbol) {
        this.symbolList.add(symbol);
    }
}

public class Symbol {
    private String varName;
    private Type type;
    private String decl;
    private Properties properties;
    private boolean isRootMethod;

    public void enableRootMethod() {
        this.isRootMethod = true;
    }

    public Symbol(String varName, Type type, String decl, AstNode astNode) {
        this.varName = varName;
        this.type = type;
        this.decl = decl;
        this.properties = new Properties(astNode);
        this.isRootMethod = false;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getDecl() {
        return decl;
    }

    public void setDecl(String decl) {
        this.decl = decl;
    }

    public Properties getProperties() {
        return properties;
    }

    public void addProperty(AstNode astNode) {
        this.properties.addAstNode(astNode);
    }
}

