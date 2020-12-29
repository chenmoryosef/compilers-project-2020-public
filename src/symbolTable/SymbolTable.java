package symbolTable;

import ast.AstNode;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {

    private Map<String, Symbol> entries;
    private SymbolTable parentSymbolTable;

    public SymbolTable(SymbolTable parentSymbolTable) {
        this.entries = new LinkedHashMap<>();
        this.parentSymbolTable = parentSymbolTable;
    }
    public Map<String, Symbol> getEntries() {
        return entries;
    }

    public SymbolTable getParentSymbolTable() {
        return parentSymbolTable;
    }

    public static String createKey(String name, String name2) {
        return name + " " + name2;
    }

    public static String createKey(String name, Type type) {
        return name + type.getName();
    }

    public Symbol addSymbol(AstNode astNode, String name, Type type, List<String> decl) {
        Symbol symbol = new Symbol(name, type, decl, astNode, this);
        this.entries.put(createKey(name, type), symbol);
        FlowUtils.addSymbolLineNumber(symbol, astNode.lineNumber);
        return symbol;
    }

    private Symbol getKey(String key) {
        return this.entries.get(key);
    }

    public Symbol resolveSymbol(String key) {
        Symbol symbol = entries.get(key);
        SymbolTable parentSymbolTable = this.parentSymbolTable;
        while ((symbol == null && parentSymbolTable != null) ||
                (symbol != null && symbol.getType() == Type.METHOD && !symbol.isRootMethod())) {
            symbol = parentSymbolTable.getKey(key);
            parentSymbolTable = parentSymbolTable.parentSymbolTable;
        }
        if (symbol == null) {
            // TODO - error handling
        }
        return symbol;
    }

    public boolean checkFieldWasDeclaredBefore(String key) {
        Symbol symbol = entries.get(key);
        SymbolTable parentSymbolTable = this.parentSymbolTable;
        while (symbol == null && parentSymbolTable != null) {
            symbol = parentSymbolTable.getKey(key);
            parentSymbolTable = parentSymbolTable.parentSymbolTable;
        }
        if (symbol != null) {
            SymbolTableUtils.setERROR(true);
            SymbolTableUtils.setERRORReasons("field was declared at least twice in the same class, or in a class and subclass ");
            return true;
        }
        return false;
    }

    public boolean checkWasAlreadyDeclared(String key) {
        Symbol symbol = entries.get(key);
        if (symbol != null) {
            SymbolTableUtils.setERROR(true);
            SymbolTableUtils.setERRORReasons("methode was declared at least twice in the same class or variable declared twice in same method");
            return true;
        }
        return false;
    }
}
