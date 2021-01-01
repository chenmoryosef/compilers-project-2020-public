package symbolTable;

import ast.*;

import java.util.HashMap;
import java.util.Map;

public class FlowUtils {
    private static Map<String, Symbol> lineNumberToSymbol = new HashMap<>();

    public static void rename(Properties properties, String newName) throws UnsupportedOperationException {
        // raise error handling
        for (var astNode : properties.getPtrList()) {
            if (astNode instanceof VariableIntroduction) {
                ((VariableIntroduction) astNode).setName(newName);
            } else if (astNode instanceof MethodDecl) {
                ((MethodDecl) astNode).setName(newName);
            } else if (astNode instanceof AssignArrayStatement) {
                ((AssignArrayStatement) astNode).setLv(newName);
            } else if (astNode instanceof AssignStatement) {
                ((AssignStatement) astNode).setLv(newName);
            } else if (astNode instanceof RefType) {
                ((RefType) astNode).setId(newName);
            } else if (astNode instanceof IdentifierExpr) {
                ((IdentifierExpr) astNode).setId(newName);
            } else if (astNode instanceof MethodCallExpr) {
                ((MethodCallExpr) astNode).setMethodId(newName);
            } else {
                throw new UnsupportedOperationException("Bug in the code");
            }
        }

        for (var symbol : properties.getSymbolList()) {
            symbol.setSymbolName(newName);
        }
    }

    public static void addSymbolLineNumber(Symbol symbol, Integer lineNumber) {
        lineNumberToSymbol.put(lineNumber + symbol.getSymbolName(), symbol);
    }

    public static Symbol findSymbolToRename(Integer originalLine, String name, boolean isMethod) {
        Symbol symbol = lineNumberToSymbol.get(originalLine + name);
        symbol = isMethod ?
                symbol.getEnclosingSymbolTable().resolveSymbol(SymbolTable.createKey(symbol.getSymbolName(), Type.METHOD))
                : symbol;
        return symbol;
    }

}
