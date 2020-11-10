package symbolTable;

import ast.AstNode;
import ast.AstVisitor;
import ast.Program;

import java.util.HashMap;
import java.util.Map;

public class SymbolTableUtils {
    private static SymbolTable root;
    // Example - if we define Class A and then Class B and afterwords Class C extends A
    // we need to know to connect C symbol table to A
    private static Map<String, SymbolTable> symbolTableClassMap = new HashMap<>();
    private static SymbolTable currSymTable;

    public static SymbolTable getCurrSymTable() {
        return currSymTable;
    }

    public static SymbolTable getRoot() {
        return root;
    }

    public static void setRoot(SymbolTable root) {
        SymbolTableUtils.root = root;
        SymbolTableUtils.currSymTable = root;
    }

    public static void setCurrSymTable(SymbolTable currSymTable) {
        SymbolTableUtils.currSymTable = currSymTable;
    }

    public static void addSymbolTable(String name, SymbolTable symbolTable) {
        symbolTableClassMap.put(name, symbolTable);
        currSymTable = symbolTable;
    }

    public static void buildSymbolTables(Program program) {
        AstVisitor visitor = new AstVisitor();
        program.accept(visitor);
    }

    public static SymbolTable getSymbolTable(String key) {
        return symbolTableClassMap.get(key);
    }
}
