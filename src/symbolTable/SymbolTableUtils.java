package symbolTable;

import ast.AstNode;
import ast.AstVisitor;
import ast.Program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTableUtils {
    private static SymbolTable root;
    // Example - if we define Class A and then Class B and afterwords Class C extends A
    // we need to know to connect C symbol table to A
    private static Map<String, SymbolTable> symbolTableClassMap = new HashMap<>();
    private static Map<String, List<AstNode>> unresolvedParams = new HashMap<>();
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
        for (var unresolved : unresolvedParams.entrySet()) {
            String[] args = unresolved.getKey().split(" ");
            String classId = args[0];
            String methodName = args[1];
            SymbolTable symbolTable = SymbolTableUtils.getSymbolTable(classId);
            Symbol symbol = symbolTable.resolveSymbol(SymbolTable.createKey(methodName, Type.METHOD));
            for (var e : unresolved.getValue()) {
                symbol.addProperty(e);
            }
        }
    }

    public static void addUnresolvedParam(String classId, String methodName, AstNode astNode) {
        String key = SymbolTable.createKey(classId, methodName);
        if (unresolvedParams.containsKey(key)) {
            unresolvedParams.get(key).add(astNode);
        } else {
            ArrayList<AstNode> lst = new ArrayList<>();
            lst.add(astNode);
            unresolvedParams.put(key, lst);
        }
    }

    public static SymbolTable getSymbolTable(String key) {
        return symbolTableClassMap.get(key);
    }
}
