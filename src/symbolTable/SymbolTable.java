package symbolTable;

import ast.AstNode;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Symbol> entries;
    private SymbolTable parentSymbolTable;

    public SymbolTable(SymbolTable parentSymbolTable) {
        this.entries = new HashMap<>();
        this.parentSymbolTable = parentSymbolTable;
    }
    public static String createKey(String name, Type type) {
        return name + type.getName();
    }

    public void addSymbol(AstNode astNode, String name, Type type, String decl) {
        Symbol symbol = new Symbol(name, type, decl, astNode);
        this.entries.put(createKey(name, type), symbol);
    }

    private Symbol getKey(String key) {
        return this.entries.get(key);
    }

    public Symbol resolveKey(String key) {
        Symbol symbol = entries.get(key);
        SymbolTable parentSymbolTable = this.parentSymbolTable;
        while (symbol == null && parentSymbolTable != null) {
            symbol = parentSymbolTable.getKey(key);
            parentSymbolTable = parentSymbolTable.parentSymbolTable;
        }
        if (symbol == null) {
            // TODO - error handling
        }
        return symbol;
    }
}
