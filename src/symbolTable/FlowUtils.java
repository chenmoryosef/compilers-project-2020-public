package symbolTable;

import ast.*;

import java.util.HashMap;
import java.util.Map;

public class FlowUtils {
    private static Map<Integer, Symbol> lineNumberToSymbol = new HashMap<>();

    public static void rename(Properties properties, String newName) throws UnsupportedOperationException {
        // TODO - raise error handling
        for (var astNode : properties.getPtrList()) {
            if (astNode instanceof VariableIntroduction) {
                ((VariableIntroduction) astNode).setName(newName);
            } else if (astNode instanceof MethodDecl) {
                ((MethodDecl) astNode).setName(newName);
            } else if (astNode instanceof AssignArrayStatement) {
                ((AssignArrayStatement) astNode).setName(newName);
            } else if (astNode instanceof AssignStatement) {
                ((AssignStatement) astNode).setName(newName);
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
        lineNumberToSymbol.put(lineNumber, symbol);
    }

    public static Symbol findSymbolToRename(Integer originalLine, boolean isMethod) {
        Symbol symbol = lineNumberToSymbol.get(originalLine);
        symbol = isMethod ?
                symbol.getEnclosingSymbolTable().resolveSymbol(SymbolTable.createKey(symbol.getSymbolName(), Type.METHOD))
                : symbol;
        return symbol;
    }

}
