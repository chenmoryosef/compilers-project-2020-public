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
    private static Map<String, SymbolTable> symbolTableClassWithMethodMap = new HashMap<>();
    private static Map<String, SymbolTable> symbolTableClassMap_real = new HashMap<>();
    private static Map<String, SymbolTable> symbolTableClassMap = new HashMap<>();
    private static Map<String, List<AstNode>> unresolvedParams = new HashMap<>();
    private static List<String> unresolvedClasses = new ArrayList<>();
    private static SymbolTable currSymTable;
    private static String currClassID;
    private static boolean ERROR = false;
    private static String ERRORReasons;


    public static String getERRORReasons() {
        return ERRORReasons;
    }

    public static void setERRORReasons(String string){
        ERRORReasons = string;

    }

    public static boolean isERROR() {
        return ERROR;
    }

    public static void setERROR(boolean ERROR) {
        SymbolTableUtils.ERROR = ERROR;
    }



    public static Map<String, SymbolTable> getSymbolTableClassWithMethodMap() { return symbolTableClassWithMethodMap; }
    public static Map<String, SymbolTable> getSymbolTableClassMap_real() { return symbolTableClassMap_real; }
    public static Map<String, SymbolTable> getSymbolTableClassMap() { return symbolTableClassMap; }
    public static void setCurrClassID(String currClassID) {
        SymbolTableUtils.currClassID = currClassID;
    }

    public static String getCurrClassId() {
        return currClassID;
    }

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

    public static void addClassMethodSymbolTable(String name, SymbolTable symbolTable) {
        symbolTableClassWithMethodMap.put(name, symbolTable);
    }
    public static boolean addClassSymbolTable(String name, SymbolTable symbolTable) {
        if(symbolTableClassMap_real.containsKey(name)){
            SymbolTableUtils.setERROR(true);
            SymbolTableUtils.setERRORReasons("more than one class with the same name is decleard in program");
            return true;
        }
        symbolTableClassMap_real.put(name, symbolTable);
        return false;
    }

    public static void buildSymbolTables(Program program) {
        AstVisitor visitor = new AstVisitor();
        program.accept(visitor);
        if(SymbolTableUtils.isERROR()){return;}
        for (var unresolved : unresolvedParams.entrySet()) {
            String[] args = unresolved.getKey().split(" ");
            String classId = args[0];
            String methodName = args[1];
            SymbolTable symbolTable = SymbolTableUtils.getSymbolTable(classId);
            if(symbolTable==null){
                SymbolTableUtils.setERROR(true);
                SymbolTableUtils.setERRORReasons("call methode of a class that was not declared in file");
                return;
            }
            Symbol symbol = symbolTable.resolveSymbol(SymbolTable.createKey(methodName, Type.METHOD));
            if(symbol==null){
                SymbolTableUtils.setERROR(true);
                SymbolTableUtils.setERRORReasons("call methode that eas not declared in the class or upper");
                return;
            }
            for (var e : unresolved.getValue()) {
                symbol.addProperty(e);
            }
        }
        for(String className:unresolvedClasses){
            if(!symbolTableClassMap_real.containsKey(className)){
                SymbolTableUtils.setERROR(true);
                SymbolTableUtils.setERRORReasons("created new class object that has not been declared in file," +
                        "or created reference to object of class type not declared in file ");
                return;
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

    public static void addUnresolvedClasses(String classId) {
        unresolvedClasses.add(classId);
    }

    public static SymbolTable getSymbolTable(String key) {
        return symbolTableClassMap.get(key);
    }
}
