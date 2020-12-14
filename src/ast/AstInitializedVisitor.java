package ast;

import symbolTable.Symbol;
import symbolTable.SymbolTable;
import symbolTable.SymbolTableUtils;
import symbolTable.Type;

import java.util.*;

public class AstInitializedVisitor implements Visitor {

    private String errorMsg = "";
    private boolean error = false;
    private String currentMethod;
    private String currentClass;
    private Map<String, Set<String>> classFields = new HashMap<>();

    private Stack<Set<String>> initVars = new Stack<>();

    private void removeTop() {
        initVars.pop();
    }

    private void pushScope() {
        Set<String> set = new HashSet<>();
        if (!initVars.isEmpty()) {
            Set<String> prev = initVars.peek();
            set.addAll(prev);
        }
        initVars.push(set);
    }

    private void pushVar(String varName) {
        Set<String> set = initVars.peek();
        set.add(varName);
    }

    private boolean isInit(String varName) {
        Set<String> set = initVars.peek();
        if (set.contains(varName)) {
            return true;
        } else if (classFields.get(currentClass).contains(varName)) {
            return true;
        }
        return false;
    }

    private void addClassesFields() {
        for (Map.Entry<String, SymbolTable> classEntry : SymbolTableUtils.getSymbolTableClassMap_real().entrySet()) {
            addClassField(classEntry.getKey(), classEntry.getValue());
        }
    }

    private void addClassField(String classId, SymbolTable classSymbolTable) {
        Set<String> setFields = new HashSet<>();
        while (classSymbolTable != null) {
            for (Map.Entry<String, Symbol> entry : classSymbolTable.getEntries().entrySet()) {
                Symbol symbol = entry.getValue();
                if (symbol.getType() == Type.VARIABLE) {
                    setFields.add(symbol.getSymbolName());
                }
            }
            classSymbolTable = classSymbolTable.getParentSymbolTable();
        }
        classFields.put(classId, setFields);
    }


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

    private Symbol getSymbol(String methodName, SymbolTable currentSymbolTable) {
        for (Map.Entry<String, Symbol> entry : currentSymbolTable.getEntries().entrySet()) {
            if (entry.getValue().getSymbolName().equals(methodName)) {
                return entry.getValue();
            }
        }
        return null;
    }


    @Override
    public void visit(Program program) {
        addClassesFields();
        program.mainClass().accept(this);
        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {
        currentClass = classDecl.name();
        for (var methodDecl : classDecl.methoddecls()) {
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
//        Symbol rootMethodSymbol = getRootMethod(methodDecl.name());
//        String rootMethodReturnType = rootMethodSymbol.getDecl().get(0);
//        if (!isRootMethod(methodDecl.name())) {
//            int i = 1;
//            // check that the variables are exact type of root
//            if (methodDecl.formals().size() != rootMethodSymbol.getDecl().size() - 1) {
//                String exp = "" + (rootMethodSymbol.getDecl().size() - 1);
//                setError("Incorrect number of formals; expected: " + exp + " got: " + methodDecl.formals().size());
//            }
//
//            for (FormalArg formal : methodDecl.formals()) {
//                formal.accept(this);
//                // resolve upper type
//                String formalRootMethod = rootMethodSymbol.getDecl().get(i);
//                // compare upper type and currentType
//                if (!currentType.equals(formalRootMethod)) {
//                    setError("Formal type incorrect; expected: " + formalRootMethod + " got: " + currentType);
//                }
//                i++;
//            }
//            // check that the return type is subtype of root return
//            methodDecl.returnType().accept(this);
//            if (!isSubTypeOf(currentType, rootMethodReturnType)) {
//                setError("Return type incorrect; expected: " + rootMethodReturnType + " got: " + currentType);
//            }
//        }
//
//
//        // Do we need this?
        for (var varDecl : methodDecl.vardecls()) {
            varDecl.accept(this);
        }

        // Only for accept
        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }

        // make sure return type is appropriate
        methodDecl.ret().accept(this);
//        if (!isSubTypeOf(currentType, rootMethodReturnType)) {
//            setError("Return value incorrect; expected: " + rootMethodReturnType + " got: " + currentType);
//        }
    }

    @Override
    public void visit(FormalArg formalArg) {
//        currentType = resolveVariableType(formalArg.name());
    }

    @Override
    public void visit(VarDecl varDecl) {
        // make sure it needs to be empty
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        for (var statement : blockStatement.statements()) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(IfStatement ifStatement) {
        // accept cond
        ifStatement.cond().accept(this);
//        if (!currentType.equals("boolean")) {
//            setError("Expected: boolean, Received: " + currentType);
//        }

        // then statements
        ifStatement.thencase().accept(this);

        // else statement
        ifStatement.elsecase().accept(this);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        // accept cond
        whileStatement.cond().accept(this);
//        if (!currentType.equals("boolean")) {
//            setError("Expected: boolean, Received: " + currentType);
//        }

        // while statements
        whileStatement.body().accept(this);
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        // handle ref-id or int-literal
        sysoutStatement.arg().accept(this);
//        if (!currentType.equals("int")) {
//            setError("Expected: int, Received: " + currentType);
//        }
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        // compute lv
//        String typeLv = resolveVariableType(assignStatement.lv());
//
//        assignStatement.rv().accept(this);
//        String typeRv = currentType;
//
//        if (!isSubTypeOf(typeRv, typeLv)) {
//            setError("AssignStatement, expected subtype of: " + typeLv + " Received: " + typeRv);
//        }
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        // compute lv
//        String typeLv = resolveVariableType(assignArrayStatement.lv());
//        if (!typeLv.equals("intArray")) {
//            setError("Expected: intArray, Received: " + typeLv);
//        }
//
//        assignArrayStatement.index().accept(this);
//        if (!currentType.equals("int")) {
//            setError("Expected: int, Received: " + currentType);
//        }
//
//        assignArrayStatement.rv().accept(this);
//        if (!currentType.equals("int")) {
//            setError("Expected: int, Received: " + currentType);
//        }
    }

    @Override
    public void visit(AndExpr e) {
        e.e1().accept(this);
//        if (!currentType.equals("boolean")) {
//            setError("Expected: boolean, Got: " + currentType);
//        }
//
//        e.e2().accept(this);
//        if (!currentType.equals("boolean")) {
//            setError("Expected: boolean, Got: " + currentType);
//        }
//
//        currentType = "boolean";
    }

    private void visitIntBinaryExpr(BinaryExpr e) {
//        e.e1().accept(this);
//        if (!currentType.equals("int")) {
//            setError("Expected: int, Got: " + currentType);
//        }
//
//        e.e2().accept(this);
//        if (!currentType.equals("int")) {
//            setError("Expected: int, Got: " + currentType);
//        }
//        currentType = "int";
    }

    @Override
    public void visit(LtExpr e) {
        visitIntBinaryExpr(e);
    }

    @Override
    public void visit(AddExpr e) {
        visitIntBinaryExpr(e);
    }

    @Override
    public void visit(SubtractExpr e) {
        visitIntBinaryExpr(e);
    }

    @Override
    public void visit(MultExpr e) {
        visitIntBinaryExpr(e);
    }

    @Override
    public void visit(ArrayAccessExpr e) {
//        e.arrayExpr().accept(this);
//        if (!currentType.equals("intArray")) {
//            setError("Expected: intArray, Got: " + currentType);
//        }
//
//        e.indexExpr().accept(this);
//        if (!currentType.equals("int")) {
//            setError("Expected: int, Got: " + currentType);
//        }
//
//        currentType = "int";
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        e.arrayExpr().accept(this);
//        if (!currentType.equals("intArray")) {
//            setError("Expected: intArray, Got: " + currentType);
//        }
//
//        currentType = "int";
    }

    @Override
    public void visit(MethodCallExpr e) {
        // make sure correct owner type
//        e.ownerExpr().accept(this);
//        if (primitiveTypes.contains(currentType)) {
//            setError("Expected: ref-type, Got: " + currentType);
//        }
//
//        Symbol methodSymbol = getMethodCallSymbol(currentType, e.methodId());

        // make sure variables are of correct type
        int i = 1;
        for (Expr arg : e.actuals()) {
            arg.accept(this);
//            String formalType = methodSymbol.getDecl().get(i);
//            if (!isSubTypeOf(currentType, formalType)) {
//                setError("Formal type incorrect; expected: " + formalType + " got: " + currentType);
//            }
//            i++;
        }
//
//        currentType = methodSymbol.getDecl().get(0);
    }

    @Override
    public void visit(IntegerLiteralExpr e) {
//        currentType = "int";
    }

    @Override
    public void visit(TrueExpr e) {
//        currentType = "boolean";
    }

    @Override
    public void visit(FalseExpr e) {
//        currentType = "boolean";
    }

    @Override
    public void visit(IdentifierExpr e) {
//        currentType = resolveVariableType(e.id());
    }

    public void visit(ThisExpr e) {
//        currentType = currentClass;
    }

    @Override
    public void visit(NewIntArrayExpr e) {
        e.lengthExpr().accept(this);
//        if (!currentType.equals("int")) {
//            setError("Expected: int, Got: " + currentType);
//        }

//        currentType = "intArray";
    }

    @Override
    public void visit(NewObjectExpr e) {
//        currentType = e.classId();
    }

    @Override
    public void visit(NotExpr e) {
        // accept
        e.e().accept(this);
//        if (!currentType.equals("boolean")) {
//            setError("Expected: boolean, Got: " + currentType);
//        }

//        currentType = "boolean";
    }

    @Override
    public void visit(IntAstType t) {
//        currentType = t.id;
    }

    @Override
    public void visit(BoolAstType t) {
//        currentType = t.id;
    }

    @Override
    public void visit(IntArrayAstType t) {
//        currentType = t.id;
    }

    @Override
    public void visit(RefType t) {
        System.out.println("Why we got here????");
    }
}
