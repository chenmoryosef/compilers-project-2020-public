package ast;


import symbolTable.*;

import java.util.*;

public class AstVisitor implements Visitor {

    private String mainClassName;
    private Set<String> notReferenceTypes = new HashSet<>(Arrays.asList("int","boolean","intArray"));

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
        mainClassName = program.mainClass().name();
        for (ClassDecl classdecl : program.classDecls()) {
            if(SymbolTableUtils.isERROR()){return;}
            SymbolTableUtils.setCurrSymTable(root);
            SymbolTableUtils.setCurrClassID(classdecl.name());
            classdecl.accept(this);
        }

        program.mainClass().accept(this);
    }

    @Override
    public void visit(ClassDecl classDecl) {
        if(classDecl.name().equals(mainClassName)){
            SymbolTableUtils.setERROR(true);
            SymbolTableUtils.setERRORReasons("class inherits from class that hasn't been defined yet. " +
                    "or, inherits from class main");
            return;
        }
        SymbolTable parentSymbolTable = SymbolTableUtils.getCurrSymTable();
        if (classDecl.superName() != null) {
            parentSymbolTable = SymbolTableUtils.getSymbolTable(classDecl.superName());
            if(parentSymbolTable==null || classDecl.superName().equals(mainClassName)){
                SymbolTableUtils.setERROR(true);
                SymbolTableUtils.setERRORReasons("class inherits from class that hasn't been defined yet. " +
                        "or, inherits from class main");
                return;
            }
        }
        SymbolTable classSymbolTable = new SymbolTable(parentSymbolTable);
        if(SymbolTableUtils.addClassSymbolTable(classDecl.name(), classSymbolTable)){return;}
        SymbolTableUtils.addSymbolTable(classDecl.name(), classSymbolTable);
        SymbolTableUtils.addClassMethodSymbolTable(classDecl.name(), classSymbolTable);


        for (var fieldDecl : classDecl.fields()) {
            String type = fieldDecl.type().id();
            if(!notReferenceTypes.contains(type)){
                if(!SymbolTableUtils.getSymbolTableClassMap_real().containsKey(type)){
                    SymbolTableUtils.addUnresolvedClasses(type);
                }
            }
            ArrayList<String> decl = new ArrayList<>();
            decl.add(fieldDecl.type().id());
            String className = fieldDecl.name();
            if(classSymbolTable.checkFieldWasDeclaredBefore(SymbolTable.createKey(className,Type.VARIABLE))){return;}
            classSymbolTable.addSymbol(fieldDecl, fieldDecl.name(), Type.VARIABLE, decl);

        }
        for (var methodDecl : classDecl.methoddecls()) {
            if(SymbolTableUtils.isERROR()){return;}
            List<String> decl = prepareDecl(methodDecl.formals(), methodDecl.returnType());
            if(classSymbolTable.checkWasAlreadyDeclared(SymbolTable.createKey(methodDecl.name(),Type.METHOD))){return;}
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
        if(SymbolTableUtils.isERROR()){return;}
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
        SymbolTableUtils.addClassMethodSymbolTable(methodDecl.name() + SymbolTableUtils.getCurrClassId(), methodSymbolTable);
        Set<String> formals = new HashSet<>();
        for (var formal : methodDecl.formals()) {
            String type = formal.type().id();
            if(!notReferenceTypes.contains(type)){
                if(!SymbolTableUtils.getSymbolTableClassMap_real().containsKey(type)){
                    SymbolTableUtils.addUnresolvedClasses(type);
                }
            }
            if(formals.contains(formal.name())){
                SymbolTableUtils.setERROR(true);
                SymbolTableUtils.setERRORReasons("there are at last two formal params with the same name");
                return;
            }
            else{
                formals.add(formal.name());
            }
            ArrayList<String> decl = new ArrayList<>();
            decl.add(formal.type().id());
            methodSymbolTable.addSymbol(formal, formal.name(), Type.VARIABLE, decl);
        }

        for (var varDecl : methodDecl.vardecls()) {
            String type = varDecl.type().id();
            if(!notReferenceTypes.contains(type)){
                if(!SymbolTableUtils.getSymbolTableClassMap_real().containsKey(type)){
                    SymbolTableUtils.addUnresolvedClasses(type);
                }
            }
            ArrayList<String> decl = new ArrayList<>();
            decl.add(varDecl.type().id());
            String varName = varDecl.name();
            if(methodSymbolTable.checkWasAlreadyDeclared(SymbolTable.createKey(varName,Type.VARIABLE))){return;}
            methodSymbolTable.addSymbol(varDecl, varDecl.name(), Type.VARIABLE, decl);
        }

        for (var stmt : methodDecl.body()) {
            if(SymbolTableUtils.isERROR()){return;}
            stmt.accept(this);
        }
        if(SymbolTableUtils.isERROR()){return;}
        methodDecl.ret().accept(this);
    }

    @Override
    public void visit(FormalArg formalArg) {
        // Note - left empty - TODO - check if this is correct behaviour
    }

    @Override
    public void visit(VarDecl varDecl) {
        if(SymbolTableUtils.isERROR()){return;}
        varDecl.type().accept(this);
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        if(SymbolTableUtils.isERROR()){return;}
        SymbolTable blockSymbolTable = new SymbolTable(SymbolTableUtils.getCurrSymTable());
        SymbolTableUtils.addSymbolTable(String.valueOf(blockStatement.lineNumber), blockSymbolTable);
        for (var s : blockStatement.statements()) {
            if(SymbolTableUtils.isERROR()){return;}
            s.accept(this);
        }
    }

    @Override
    public void visit(IfStatement ifStatement) {
        if(SymbolTableUtils.isERROR()){return;}
        ifStatement.cond().accept(this);
        if(SymbolTableUtils.isERROR()){return;}
        ifStatement.thencase().accept(this);
        if(SymbolTableUtils.isERROR()){return;}
        ifStatement.elsecase().accept(this);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        if(SymbolTableUtils.isERROR()){return;}
        whileStatement.cond().accept(this);
        if(SymbolTableUtils.isERROR()){return;}
        whileStatement.body().accept(this);
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        if(SymbolTableUtils.isERROR()){return;}
        sysoutStatement.arg().accept(this);
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        if(SymbolTableUtils.isERROR()){return;}
        Symbol rootSymbol = SymbolTableUtils.getCurrSymTable().resolveSymbol(SymbolTable.createKey(assignStatement.lv(), Type.VARIABLE));
        if (rootSymbol != null) {
            rootSymbol.addProperty(assignStatement);
        } else {
            SymbolTableUtils.setERROR(true);
            SymbolTableUtils.setERRORReasons("reference to object that has not been declared before");
            return;
        }
        assignStatement.rv().accept(this);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        if(SymbolTableUtils.isERROR()){return;}
        Symbol rootSymbol = SymbolTableUtils.getCurrSymTable().resolveSymbol(SymbolTable.createKey(assignArrayStatement.lv(), Type.VARIABLE));
        if (rootSymbol != null) {
            rootSymbol.addProperty(assignArrayStatement);
        } else {
            SymbolTableUtils.setERROR(true);
            SymbolTableUtils.setERRORReasons("reference to object that has not been declared before");
            return;
        }
        assignArrayStatement.index().accept(this);
        if(SymbolTableUtils.isERROR()){return;}
        assignArrayStatement.rv().accept(this);
    }

    @Override
    public void visit(AndExpr e) {
        if(SymbolTableUtils.isERROR()){return;}
        e.e1().accept(this);
        if(SymbolTableUtils.isERROR()){return;}
        e.e2().accept(this);
    }

    @Override
    public void visit(LtExpr e) {
        if(SymbolTableUtils.isERROR()){return;}
        e.e1().accept(this);
        if(SymbolTableUtils.isERROR()){return;}
        e.e2().accept(this);
    }

    @Override
    public void visit(AddExpr e) {
        if(SymbolTableUtils.isERROR()){return;}
        e.e1().accept(this);
        if(SymbolTableUtils.isERROR()){return;}
        e.e2().accept(this);
    }

    @Override
    public void visit(SubtractExpr e) {
        if(SymbolTableUtils.isERROR()){return;}
        e.e1().accept(this);
        if(SymbolTableUtils.isERROR()){return;}
        e.e2().accept(this);
    }

    @Override
    public void visit(MultExpr e) {
        if(SymbolTableUtils.isERROR()){return;}
        e.e1().accept(this);
        if(SymbolTableUtils.isERROR()){return;}
        e.e2().accept(this);
    }

    @Override
    public void visit(ArrayAccessExpr e) {
        if(SymbolTableUtils.isERROR()){return;}
        e.arrayExpr().accept(this);
        if(SymbolTableUtils.isERROR()){return;}
        e.indexExpr().accept(this);
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        if(SymbolTableUtils.isERROR()){return;}
        e.arrayExpr().accept(this);
    }

    @Override
    public void visit(MethodCallExpr e) {
        if(SymbolTableUtils.isERROR()){return;}
        AstNode ownerExp = e.ownerExpr();
        Symbol symbol;
        String symbolKey = SymbolTable.createKey(e.methodId(), Type.METHOD);
        String classId;
        SymbolTable symbolTable;
        for (Expr arg : e.actuals()) {
            arg.accept(this);
        }

        if (ownerExp instanceof ThisExpr) {
            classId = SymbolTableUtils.getCurrClassId();
            symbolTable = SymbolTableUtils.getCurrSymTable();
        } else if (ownerExp instanceof NewObjectExpr) {
            classId = ((NewObjectExpr) ownerExp).classId();
            symbolTable = SymbolTableUtils.getSymbolTable(classId);
        } else if (ownerExp instanceof IdentifierExpr){
            Symbol ownerSymbol = SymbolTableUtils.getCurrSymTable().resolveSymbol(SymbolTable.createKey(((IdentifierExpr) ownerExp).id(), Type.VARIABLE));
            if(ownerSymbol == null) {
                SymbolTableUtils.setERROR(true);
                SymbolTableUtils.setERRORReasons("method call should be invoked with existing variable");
                return;
            }
            classId = ownerSymbol.getDecl().get(0);
            if(notReferenceTypes.contains(classId)){
                SymbolTableUtils.setERROR(true);
                SymbolTableUtils.setERRORReasons("method call should be invoked with this, new or variable");
                return;
            }
            else {
                symbolTable = SymbolTableUtils.getSymbolTable(classId);
            }
        }
        else{
            SymbolTableUtils.setERROR(true);
            SymbolTableUtils.setERRORReasons("method call should be invoked with this, new or variable");
            return;
        }
        if (symbolTable == null) {
            if (classId != null) {
                SymbolTableUtils.addUnresolvedParam(classId, e.methodId(), e);
                return;
            }
            // TODO - handle error why the hell should we get here-
            return;
        }
        symbol = symbolTable.resolveSymbol(symbolKey);
        if(symbol == null) {
            SymbolTableUtils.addUnresolvedParam(classId, e.methodId(), e);
            return;
        }
        symbol.addProperty(e);
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
        if(SymbolTableUtils.isERROR()){return;}
        Symbol symbol = SymbolTableUtils.getCurrSymTable().resolveSymbol(SymbolTable.createKey(e.id(), Type.VARIABLE));
        if(symbol==null){
            SymbolTableUtils.setERROR(true);
            SymbolTableUtils.setERRORReasons("reference to object that has not been declared before");
            return;
        }
        symbol.addProperty(e);
    }

    public void visit(ThisExpr e) {
    }

    @Override
    public void visit(NewIntArrayExpr e) {
        if(SymbolTableUtils.isERROR()){return;}
        e.lengthExpr().accept(this);
    }

    @Override
    public void visit(NewObjectExpr e) {
        if(!SymbolTableUtils.getSymbolTableClassMap_real().containsKey(e.classId())){
            SymbolTableUtils.addUnresolvedClasses(e.classId());
        }
    }

    @Override
    public void visit(NotExpr e) {
        if(SymbolTableUtils.isERROR()){return;}
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
            SymbolTableUtils.setERROR(true);
            SymbolTableUtils.setERRORReasons("reference to object that has not been declared before");
            return;
        }
        symbol.addProperty(t);
    }
}
