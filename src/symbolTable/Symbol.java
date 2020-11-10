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
    private String symbolName;
    private Type type;
    private List<String> decl;
    private Properties properties;
    private boolean isRootMethod;
    private SymbolTable enclosingSymbolTable;

    public Symbol(String varName, Type type, List<String> decl, AstNode astNode, SymbolTable enclosingSymbolTable) {
        this.symbolName = varName;
        this.type = type;
        this.decl = decl;
        this.properties = new Properties(astNode);
        this.isRootMethod = false;
        this.enclosingSymbolTable = enclosingSymbolTable;
    }

    public SymbolTable getEnclosingSymbolTable() {
        return enclosingSymbolTable;
    }

    public void enableRootMethod() {
        this.isRootMethod = true;
    }

    public boolean isRootMethod() {
        return isRootMethod;
    }

    public String getSymbolName() {
        return symbolName;
    }

    public void setSymbolName(String symbolName) {
        this.symbolName = symbolName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<String> getDecl() {
        return decl;
    }

    public Properties getProperties() {
        return properties;
    }

    public void addProperty(AstNode astNode) {
        this.properties.addAstNode(astNode);
    }

    public void addProperty(AstNode astNode, Symbol symbol) {
        this.properties.addAstNode(astNode);
        this.properties.addSymbol(symbol);
    }
}

