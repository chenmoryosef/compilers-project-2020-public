package symbolTable;

import ast.AstVisitor;
import ast.Program;

import java.util.Map;

public class SymbolTableUtils {
    private static SymbolTable root;
    // Example - if we define Class A and then Class B and afterwords Class C extends A
    // we need to know to connect C symbol table to A
    private static Map<String, SymbolTable> symbolTableMap;
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
        symbolTableMap.put(name, symbolTable);
        currSymTable = symbolTable;
    }

    public static void buildSymbolTables(Program program) {
        AstVisitor visitor = new AstVisitor();
        program.accept(visitor);
    }

    public static Symbol findSymbol(Program program, String originalName, String originalLine) {
        Symbol symbol;
        // code which finds reference symbol
        return null;
    }

}
