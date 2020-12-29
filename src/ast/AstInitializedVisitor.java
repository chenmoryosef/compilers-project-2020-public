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

    private Set<String> removeTop() {
        return initVars.pop();
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

    private void pushVarSet(Set<String> varSet) {
        Set<String> set = initVars.peek();
        set.addAll(varSet);
    }

    private boolean isInit(String varName) {
        Set<String> set = initVars.peek();
        if (set.contains(varName)) {
            return true;
        } else if (classFields.get(currentClass).contains(varName) && !SymbolTableUtils.getSymbolTableClassWithMethodMap().get(currentMethod + currentClass).getEntries().containsKey(SymbolTable.createKey(varName, Type.VARIABLE))) {
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
        pushScope();
        mainClass.mainStatement().accept(this);
        removeTop();
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        pushScope();
        // Formals are always initialized
        for (FormalArg formal : methodDecl.formals()) {
            formal.accept(this);
        }

        for (var varDecl : methodDecl.vardecls()) {
            varDecl.accept(this);
        }

        // Only for accept
        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }

        // make sure return type is initialized
        methodDecl.ret().accept(this);
        removeTop();
    }

    @Override
    public void visit(FormalArg formalArg) {
        pushVar(formalArg.name());
    }

    @Override
    public void visit(VarDecl varDecl) {
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        for (var statement : blockStatement.statements()) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(IfStatement ifStatement) {
        ifStatement.cond().accept(this);

        // then statements
        // push then scope
        pushScope();
        ifStatement.thencase().accept(this);
        // remove scope
        Set<String> thenCaseVars = removeTop();

        // else statements
        // push then scope
        pushScope();
        ifStatement.elsecase().accept(this);
        // remove scope
        Set<String> elseCaseVars = removeTop();
        elseCaseVars.retainAll(thenCaseVars);

        // push intersection vars to current scope
        pushVarSet(elseCaseVars);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        // accept cond
        whileStatement.cond().accept(this);

        // while statements
        // push then scope
        pushScope();
        whileStatement.body().accept(this);
        // remove scope
        removeTop();
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        // check if arg is initialized is inside
        sysoutStatement.arg().accept(this);
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        // check if rv is initialized is inside
        assignStatement.rv().accept(this);
        pushVar(assignStatement.lv());
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        // make sure index is initialized
        assignArrayStatement.index().accept(this);

        // make sure rv is initialized
        assignArrayStatement.rv().accept(this);

        // push lv to init vars
        if(!isInit(assignArrayStatement.lv())) {
            setError("UnInitialized variable: " + assignArrayStatement.lv());
        }

    }

    @Override
    public void visit(AndExpr e) {
        visitBinaryExpr(e);
    }

    private void visitBinaryExpr(BinaryExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(LtExpr e) {
        visitBinaryExpr(e);
    }

    @Override
    public void visit(AddExpr e) {
        visitBinaryExpr(e);
    }

    @Override
    public void visit(SubtractExpr e) {
        visitBinaryExpr(e);
    }

    @Override
    public void visit(MultExpr e) {
        visitBinaryExpr(e);
    }

    @Override
    public void visit(ArrayAccessExpr e) {
        // make sure array is initialized
        e.arrayExpr().accept(this);

        // make sure indexExpr is initialized
        e.indexExpr().accept(this);
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        // make sure array is initialized
        e.arrayExpr().accept(this);
    }

    @Override
    public void visit(MethodCallExpr e) {
        // make sure ownerExpr is initialized
        e.ownerExpr().accept(this);

        // make sure variables are initialized
        for (Expr arg : e.actuals()) {
            arg.accept(this);
        }
    }

    @Override
    public void visit(IntegerLiteralExpr e) {
    }

    @Override
    public void visit(TrueExpr e) {
    }

    @Override
    public void visit(FalseExpr e) {
    }

    @Override
    public void visit(IdentifierExpr e) {
        if (!isInit(e.id())) {
            setError("UnInitialized variable: " + e.id());
        }
    }

    public void visit(ThisExpr e) {
    }

    @Override
    public void visit(NewIntArrayExpr e) {
        e.lengthExpr().accept(this);
    }

    @Override
    public void visit(NewObjectExpr e) {
    }

    @Override
    public void visit(NotExpr e) {
        // make sure e is initialized
        e.e().accept(this);
    }

    @Override
    public void visit(IntAstType t) {
    }

    @Override
    public void visit(BoolAstType t) {
    }

    @Override
    public void visit(IntArrayAstType t) {
    }

    @Override
    public void visit(RefType t) {
    }
}
