package ast;


import symbolTable.*;

import java.util.ArrayList;
import java.util.List;

public class AstVisitor implements Visitor {

    private List<String> prepareDecl(List<FormalArg> list, AstType returnType) {
        ArrayList<String> decl = new ArrayList<>();
        decl.add(returnType.id());
        for (var formal : list) {
            decl.add(formal.type().id());
        }
        return decl;
    }


    @Override
    public void visit(Program program) {
        SymbolTable root = new SymbolTable(null);
        SymbolTableUtils.setRoot(root);

        for (ClassDecl classdecl : program.classDecls()) {
            SymbolTableUtils.setCurrSymTable(root);
            // TODO - we were here!
            classdecl.accept(this);
        }

        program.mainClass().accept(this);
    }

    @Override
    public void visit(ClassDecl classDecl) {
        SymbolTable parentSymbolTable = SymbolTableUtils.getCurrSymTable();
        if (classDecl.superName() != null) {
            parentSymbolTable = SymbolTableUtils.getSymbolTable(classDecl.superName());
            // TODO - error handling
        }
        SymbolTable classSymbolTable = new SymbolTable(parentSymbolTable);
        SymbolTableUtils.addSymbolTable(classDecl.name(), classSymbolTable);

        for (var fieldDecl : classDecl.fields()) {
            ArrayList<String> decl = new ArrayList<>();
            decl.add(fieldDecl.type().id());
            classSymbolTable.addSymbol(fieldDecl, fieldDecl.name(), Type.VARIABLE, decl);
        }

        for (var methodDecl : classDecl.methoddecls()) {
            List<String> decl = prepareDecl(methodDecl.formals(), methodDecl.returnType());
            Symbol methodSymbol = classSymbolTable.addSymbol(methodDecl, methodDecl.name(), Type.METHOD, decl);
            Symbol rootMethodSymbol = SymbolTableUtils.getCurrSymTable().resolveSymbol(SymbolTable.createKey(methodDecl.name(), Type.METHOD));
            if (rootMethodSymbol != null) {
                rootMethodSymbol.addProperty(methodDecl, methodSymbol);
            } else {
                methodSymbol.enableRootMethod();
            }

            SymbolTableUtils.setCurrSymTable(classSymbolTable);
            methodDecl.accept(this);
        }
    }

    @Override
    public void visit(MainClass mainClass) {
        // This is a new scope -> create new symbol table
        SymbolTable symbolTable = new SymbolTable(SymbolTableUtils.getRoot());
        SymbolTableUtils.addSymbolTable(mainClass.name(), symbolTable);
        // MainClass has argsName parameter only - create symbol
        ArrayList<String> decl = new ArrayList<>();
        decl.add("String[]");
        SymbolTableUtils.getCurrSymTable().addSymbol(mainClass, mainClass.argsName(), Type.VARIABLE, decl);
        mainClass.mainStatement().accept(this);
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        SymbolTable methodSymbolTable = new SymbolTable(SymbolTableUtils.getCurrSymTable());
        SymbolTableUtils.addSymbolTable(methodDecl.name(), methodSymbolTable);

        for (var formal : methodDecl.formals()) {
            ArrayList<String> decl = new ArrayList<>();
            decl.add(formal.type().id());
            methodSymbolTable.addSymbol(formal, formal.name(), Type.VARIABLE, decl);
        }

        for (var varDecl : methodDecl.vardecls()) {
            ArrayList<String> decl = new ArrayList<>();
            decl.add(varDecl.type().id());
            methodSymbolTable.addSymbol(varDecl, varDecl.name(), Type.VARIABLE, decl);
        }

        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }
        methodDecl.ret().accept(this);
    }

    @Override
    public void visit(FormalArg formalArg) {
        // Note - left empty - TODO - check if this is correct behaviour
    }

    @Override
    public void visit(VarDecl varDecl) {
        varDecl.type().accept(this);
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        SymbolTable blockSymbolTable = new SymbolTable(SymbolTableUtils.getCurrSymTable());
        SymbolTableUtils.addSymbolTable(String.valueOf(blockStatement.lineNumber), blockSymbolTable);
        for (var s : blockStatement.statements()) {
            s.accept(this);
        }
    }

    @Override
    public void visit(IfStatement ifStatement) {
        ifStatement.cond().accept(this);
        ifStatement.thencase().accept(this);
        ifStatement.elsecase().accept(this);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        whileStatement.cond().accept(this);
        whileStatement.body().accept(this);
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        sysoutStatement.arg().accept(this);
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        Symbol rootSymbol = SymbolTableUtils.getCurrSymTable().resolveSymbol(SymbolTable.createKey(assignStatement.lv(), Type.VARIABLE));
        if (rootSymbol != null) {
            rootSymbol.addProperty(assignStatement);
        } else {
            // TODO - error handling
        }
        assignStatement.rv().accept(this);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        Symbol rootSymbol = SymbolTableUtils.getCurrSymTable().resolveSymbol(SymbolTable.createKey(assignArrayStatement.lv(), Type.VARIABLE));
        if (rootSymbol != null) {
            rootSymbol.addProperty(assignArrayStatement);
        } else {
            // TODO - error handling
        }
        assignArrayStatement.index().accept(this);
        assignArrayStatement.rv().accept(this);
    }

    @Override
    public void visit(AndExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(LtExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(AddExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(SubtractExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(MultExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(ArrayAccessExpr e) {
        e.arrayExpr().accept(this);
        e.indexExpr().accept(this);
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        e.arrayExpr().accept(this);
    }

    @Override
    public void visit(MethodCallExpr e) {
        AstNode ownerExp = e.ownerExpr();
        Symbol symbol;
        String symbolKey = SymbolTable.createKey(e.methodId(), Type.METHOD);
        String classId = null;
        SymbolTable symbolTable;
        if (ownerExp instanceof ThisExpr) {
            symbolTable = SymbolTableUtils.getCurrSymTable();
        } else if (ownerExp instanceof NewObjectExpr) {
            classId = ((NewObjectExpr) ownerExp).classId();
            symbolTable = SymbolTableUtils.getSymbolTable(classId);
        } else {
            Symbol ownerSymbol = SymbolTableUtils.getCurrSymTable().resolveSymbol(SymbolTable.createKey(((IdentifierExpr) ownerExp).id(), Type.VARIABLE));
            classId = ownerSymbol.getDecl().get(0);
            symbolTable = SymbolTableUtils.getSymbolTable(classId);
        }
        if (symbolTable == null) {
            if (classId != null) {
                SymbolTableUtils.addUnresolvedParam(classId, e.methodId(), e);
                return;
            }
            // TODO - handle error
            return;
        }
        symbol = symbolTable.resolveSymbol(symbolKey);
        symbol.addProperty(e);
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
        Symbol symbol = SymbolTableUtils.getCurrSymTable().resolveSymbol(SymbolTable.createKey(e.id(), Type.VARIABLE));
        // TODO - handle null symbol
        symbol.addProperty(e);
    }

    public void visit(ThisExpr e) {
    }

    @Override
    public void visit(NewIntArrayExpr e) {
    }

    @Override
    public void visit(NewObjectExpr e) {
        // TODO - do we neeed to pay attention to this?
    }

    @Override
    public void visit(NotExpr e) {
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
        Symbol symbol = SymbolTableUtils.getCurrSymTable().resolveSymbol(SymbolTable.createKey(t.id(), Type.VARIABLE));
        if (symbol == null) {
            // TODO - handle error
            return;
        }
        symbol.addProperty(t);
    }
}
