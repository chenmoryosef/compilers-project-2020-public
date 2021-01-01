package ast;

import symbolTable.Symbol;
import symbolTable.SymbolTable;
import symbolTable.SymbolTableUtils;
import symbolTable.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AstTypesVisitor implements Visitor {

    private String errorMsg = "";
    private boolean error = false;
    private String currentMethod;
    private String currentClass;
    private String currentType;

    List<String> primitiveTypes = Arrays.asList("int", "boolean", "intArray");

    // sets error
    private void setError(String errorString) {
        error = true;
        errorMsg += "\nMethod: " + currentMethod + " Class:" + currentClass + "\n" + errorString;
    }

    public boolean isError() {
        return error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    // Checks if a is subtype of b
    private boolean isSubTypeOf(String typeA, String typeB) {
        boolean bIsPrimitive = false;
        for (var type : primitiveTypes) {
            if (typeA.equals(type)) {
                if (!typeA.equals(typeB)) {
                    return false;
                } else {
                    return true;
                }
            }
            if (typeB.equals(type)) {
                bIsPrimitive = true;
            }
        }

        if(bIsPrimitive) {
            return false;
        }
        SymbolTable typeASymbolTable = SymbolTableUtils.getSymbolTableClassMap_real().get(typeA);
        SymbolTable typeBSymbolTable = SymbolTableUtils.getSymbolTableClassMap_real().get(typeB);

        SymbolTable symbolTable = typeASymbolTable;
        if (symbolTable == typeBSymbolTable) {
            return true;
        }
        while (symbolTable != null) {
            symbolTable = symbolTable.getParentSymbolTable();
            if (symbolTable != null && symbolTable == typeBSymbolTable) {
                return true;
            }
        }

        return false;
    }

    // Receives ast node and returns its type
    private String resolveVariableType(String variableName) {
        SymbolTable currentSymbolTable = SymbolTableUtils.getSymbolTableClassWithMethodMap().get(currentMethod + currentClass);
        String type;
        type = isVariableInSymbolTable(variableName, currentSymbolTable);
        boolean isField = type == null;
        if (isField) {
            SymbolTable parentSymbolTable = currentSymbolTable.getParentSymbolTable();
            boolean foundField = false;
            while (parentSymbolTable != null && !foundField) {
                type = isVariableInSymbolTable(variableName, parentSymbolTable);
                foundField = type != null;
                parentSymbolTable = parentSymbolTable.getParentSymbolTable();
            }
        }
        return type;
    }

    private String isVariableInSymbolTable(String variableName, SymbolTable currentSymbolTable) {
        for (Map.Entry<String, Symbol> entry : currentSymbolTable.getEntries().entrySet()) {
            if (entry.getValue().getSymbolName().equals(variableName)) {
                return entry.getValue().getDecl().get(0);
            }
        }
        return null;
    }

    private Symbol getSymbol(String methodName, SymbolTable currentSymbolTable) {
        for (Map.Entry<String, Symbol> entry : currentSymbolTable.getEntries().entrySet()) {
            if (entry.getKey().equals(SymbolTable.createKey(methodName, Type.METHOD))) {
                return entry.getValue();
            }
        }
        return null;
    }

    // Returns whether the method is root or not
    private boolean isRootMethod(String methodName) {
        SymbolTable currentSymbolTable = SymbolTableUtils.getSymbolTableClassMap_real().get(currentClass);
        return getSymbol(methodName, currentSymbolTable).isRootMethod();
    }


    // Returns root method - only when the current class doesn't have it
    private Symbol getRootMethod(String methodName) {
        SymbolTable currentSymbolTable = SymbolTableUtils.getSymbolTableClassMap_real().get(currentClass);
//        currentSymbolTable = currentSymbolTable.getParentSymbolTable();
        while (currentSymbolTable != null) {
            Symbol symbol = getSymbol(methodName, currentSymbolTable);
            if (symbol != null) {
                return symbol;
            }
            currentSymbolTable = currentSymbolTable.getParentSymbolTable();
        }
        return null;
    }

    // Returns closet anestor method with the same name- to check override
    private Symbol getAncestorMethod(String methodName) {
        SymbolTable currentSymbolTable = SymbolTableUtils.getSymbolTableClassMap_real().get(currentClass);
        currentSymbolTable = currentSymbolTable.getParentSymbolTable();
        while (currentSymbolTable != null) {
            Symbol symbol = getSymbol(methodName, currentSymbolTable);
            if (symbol != null) {
                return symbol;
            }
            currentSymbolTable = currentSymbolTable.getParentSymbolTable();
        }
        return null;
    }


    private Symbol getMethodCallSymbol(String classId, String methodName) {
        SymbolTable currentSymbolTable = SymbolTableUtils.getSymbolTableClassMap_real().get(classId);
        do {
            Symbol symbol = getSymbol(methodName, currentSymbolTable);
            if (symbol != null) {
                return symbol;
            }
            currentSymbolTable = currentSymbolTable.getParentSymbolTable();

        } while ((currentSymbolTable != null));

        return null;
    }

    @Override
    public void visit(Program program) {
        program.mainClass().accept(this);
        for (ClassDecl classdecl : program.classDecls()) {
            if(isError()){return;}
            classdecl.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {
        currentClass = classDecl.name();
        for (var methodDecl : classDecl.methoddecls()) {
            if(isError()){return;}
            currentMethod = methodDecl.name();
            methodDecl.accept(this);
        }
    }

    @Override
    public void visit(MainClass mainClass) {
        mainClass.mainStatement().accept(this);
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        // Check if this method is root.
//        System.out.println(currentClass);
        Symbol rootMethodSymbol;
        String rootMethodReturnType;
        if(!isRootMethod(methodDecl.name())){
            rootMethodSymbol = getAncestorMethod(methodDecl.name());
            rootMethodReturnType= rootMethodSymbol.getDecl().get(0);
        }
        else {
            rootMethodSymbol = getRootMethod(methodDecl.name());
            rootMethodReturnType = rootMethodSymbol.getDecl().get(0);
//        System.out.println("root");
//        System.out.println(rootMethodSymbol.getDecl().size());
//        System.out.println(rootMethodSymbol.getSymbolName());
        }
        if (!isRootMethod(methodDecl.name())) {
            int i = 1;
            // check that the variables are exact type of root
            if (methodDecl.formals().size() != rootMethodSymbol.getDecl().size() - 1) {
                String exp = "" + (rootMethodSymbol.getDecl().size() - 1);
                setError("Incorrect number of formals; expected: " + exp + " got: " + methodDecl.formals().size());
            }

            for (FormalArg formal : methodDecl.formals()) {
                if(isError()){return;}
                formal.accept(this);
                // resolve upper type
                String formalRootMethod = rootMethodSymbol.getDecl().get(i);
                // compare upper type and currentType
                if (!currentType.equals(formalRootMethod)) {
                    setError("Formal type incorrect; expected: " + formalRootMethod + " got: " + currentType);
                }
                i++;
            }
            // check that the return type is subtype of root return
            methodDecl.returnType().accept(this);
            if(isError()){return;}
            if (!isSubTypeOf(currentType, rootMethodReturnType)) {
                setError("Return type incorrect; expected: " + rootMethodReturnType + " got: " + currentType);
            }
        }


        // Do we need this?
        for (var varDecl : methodDecl.vardecls()) {
            if(isError()){return;}
            varDecl.accept(this);
        }
        if(isError()){return;}
        // Only for accept
        for (var stmt : methodDecl.body()) {
            if(isError()){return;}
            stmt.accept(this);
        }

        // make sure return type is appropriate
        if(isError()){return;}
        methodDecl.ret().accept(this);
        if (!isSubTypeOf(currentType, rootMethodReturnType)) {
            setError("Return value incorrect; expected: " + rootMethodReturnType + " got: " + currentType);
        }

    }

    @Override
    public void visit(FormalArg formalArg) {
        if(isError()){return;}
        currentType = resolveVariableType(formalArg.name());
    }

    @Override
    public void visit(VarDecl varDecl) {
        if(isError()){return;}
        // make sure it needs to be empty
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        for (var statement : blockStatement.statements()) {
            if(isError()){return;}
            statement.accept(this);
        }
    }

    @Override
    public void visit(IfStatement ifStatement) {
        if(isError()){return;}
        // accept cond
        ifStatement.cond().accept(this);
        if (!currentType.equals("boolean")) {
            setError("Expected: boolean, Received: " + currentType);
        }

        // then statements
        ifStatement.thencase().accept(this);
        if(isError()){return;}
        // else statement
        ifStatement.elsecase().accept(this);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        // accept cond
        if(isError()){return;}
        whileStatement.cond().accept(this);
        if (!currentType.equals("boolean")) {
            setError("Expected: boolean, Received: " + currentType);
        }
        if(isError()){return;}
        // while statements
        whileStatement.body().accept(this);
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        // handle ref-id or int-literal
        if(isError()){return;}
        sysoutStatement.arg().accept(this);
        if(isError()){return;}
        if (!currentType.equals("int")) {
            setError("Expected: int, Received: " + currentType);
        }
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        if(isError()){return;}
        // compute lv
        String typeLv = resolveVariableType(assignStatement.lv());

        assignStatement.rv().accept(this);
        if(isError()){return;}
        String typeRv = currentType;

        if (!isSubTypeOf(typeRv, typeLv)) {
            setError("AssignStatement, expected subtype of: " + typeLv + " Received: " + typeRv);
        }
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        if(isError()){return;}
        // compute lv
        String typeLv = resolveVariableType(assignArrayStatement.lv());
        if (!typeLv.equals("intArray")) {
            setError("Expected: intArray, Received: " + typeLv);
        }

        assignArrayStatement.index().accept(this);
        if(isError()){return;}
        if (!currentType.equals("int")) {
            setError("Expected: int, Received: " + currentType);
        }
        if(isError()){return;}
        assignArrayStatement.rv().accept(this);
        if(isError()){return;}
        if (!currentType.equals("int")) {
            setError("Expected: int, Received: " + currentType);
        }
    }

    @Override
    public void visit(AndExpr e) {
        if(isError()){return;}
        e.e1().accept(this);
        if(isError()){return;}
        if (!currentType.equals("boolean")) {
            setError("Expected: boolean, Got: " + currentType);
        }

        e.e2().accept(this);
        if(isError()){return;}
        if (!currentType.equals("boolean")) {
            setError("Expected: boolean, Got: " + currentType);
        }

        currentType = "boolean";
    }

    private void visitIntBinaryExpr(BinaryExpr e) {
        if(isError()){return;}
        e.e1().accept(this);
        if(isError()){return;}
        if (!currentType.equals("int")) {
            setError("Expected: int, Got: " + currentType);
        }

        e.e2().accept(this);
        if(isError()){return;}
        if (!currentType.equals("int")) {
            setError("Expected: int, Got: " + currentType);
        }
        currentType = "int";
    }

    @Override
    public void visit(LtExpr e) {
        if(isError()){return;}
        e.e1().accept(this);
        if (!currentType.equals("int")) {
            setError("Expected: int, Got: " + currentType);
        }
        if(isError()){return;}
        e.e2().accept(this);
        if(isError()){return;}
        if (!currentType.equals("int")) {
            setError("Expected: int, Got: " + currentType);
        }
        currentType = "boolean";
    }

    @Override
    public void visit(AddExpr e) {
        if(isError()){return;}
        visitIntBinaryExpr(e);
    }

    @Override
    public void visit(SubtractExpr e) {
        if(isError()){return;}
        visitIntBinaryExpr(e);
    }

    @Override
    public void visit(MultExpr e) {
        if(isError()){return;}
        visitIntBinaryExpr(e);
    }

    @Override
    public void visit(ArrayAccessExpr e) {
        if(isError()){return;}
        e.arrayExpr().accept(this);
        if(isError()){return;}
        if (!currentType.equals("intArray")) {
            setError("Expected: intArray, Got: " + currentType);
        }
        if(isError()){return;}
        e.indexExpr().accept(this);
        if(isError()){return;}
        if (!currentType.equals("int")) {
            setError("Expected: int, Got: " + currentType);
        }

        currentType = "int";
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        if(isError()){return;}
        e.arrayExpr().accept(this);
        if(isError()){return;}
        if (!currentType.equals("intArray")) {
            setError("Expected: intArray, Got: " + currentType);
        }

        currentType = "int";
    }

    @Override
    public void visit(MethodCallExpr e) {
        if(isError()){return;}
        // make sure correct owner type
        e.ownerExpr().accept(this);
        if(isError()){return;}
        if (primitiveTypes.contains(currentType)) {
            setError("Expected: ref-type, Got: " + currentType);
        }

        Symbol methodSymbol = getMethodCallSymbol(currentType, e.methodId());
        //make sure number of actual params matches the method declaration
        if (e.actuals().size()!=methodSymbol.getDecl().size()-1) {
            setError("Number of actual params doesn't match the method declaration; expected: " + (methodSymbol.getDecl().size()-1) +
                    " got: " + e.actuals().size());
        }
        if(isError()){return;}
        // make sure variables are of correct type
        int i = 1;
        for (Expr arg : e.actuals()) {
            if(isError()){return;}
            arg.accept(this);
            String formalType = methodSymbol.getDecl().get(i);
            if (!isSubTypeOf(currentType, formalType)) {
                setError("Formal type incorrect; expected: " + formalType + " got: " + currentType);
            }
            i++;
        }
        if(isError()){return;}
        currentType = methodSymbol.getDecl().get(0);
    }

    @Override
    public void visit(IntegerLiteralExpr e) {
        if(isError()){return;}
        currentType = "int";
    }

    @Override
    public void visit(TrueExpr e) {
        if(isError()){return;}
        currentType = "boolean";
    }

    @Override
    public void visit(FalseExpr e) {
        if(isError()){return;}
        currentType = "boolean";
    }

    @Override
    public void visit(IdentifierExpr e) {
        if(isError()){return;}
        currentType = resolveVariableType(e.id());
    }

    public void visit(ThisExpr e) {
        if(isError()){return;}
        currentType = currentClass;
    }

    @Override
    public void visit(NewIntArrayExpr e) {
        if(isError()){return;}
        e.lengthExpr().accept(this);
        if(isError()){return;}
        if (!currentType.equals("int")) {
            setError("Expected: int, Got: " + currentType);
        }

        currentType = "intArray";
    }

    @Override
    public void visit(NewObjectExpr e) {
        currentType = e.classId();
    }

    @Override
    public void visit(NotExpr e) {
        if(isError()){return;}
        // accept
        e.e().accept(this);
        if(isError()){return;}
        if (!currentType.equals("boolean")) {
            setError("Expected: boolean, Got: " + currentType);
        }

        currentType = "boolean";
    }

    @Override
    public void visit(IntAstType t) {
        if(isError()){return;}
        currentType = t.id;
    }

    @Override
    public void visit(BoolAstType t) {
        if(isError()){return;}
        currentType = t.id;
    }

    @Override
    public void visit(IntArrayAstType t) {
        if(isError()){return;}
        currentType = t.id;
    }

    @Override
    public void visit(RefType t) {
        if(isError()){return;}
        currentType = t.id();
    }
}
