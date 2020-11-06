package ast;


import symbolTable.*;

public class AstVisitor implements Visitor {
    private StringBuilder builder = new StringBuilder();

    private int indent = 0;

    public String getString() {
        return builder.toString();
    }

    private void appendWithIndent(String str) {
        builder.append("\t".repeat(indent));
        builder.append(str);
    }

    private void visitBinaryExpr(BinaryExpr e, String infixSymbol) {
        builder.append("(");
        e.e1().accept(this);
        builder.append(")");
        builder.append(" " + infixSymbol + " ");
        builder.append("(");
        e.e2().accept(this);
        builder.append(")");
    }


    @Override
    public void visit(Program program) {
        SymbolTable root = new SymbolTable(null);
        SymbolTableUtils.setRoot(root);

        program.mainClass().accept(this);
        for (ClassDecl classdecl : program.classDecls()) {
            SymbolTableUtils.setCurrSymTable(root);
            classdecl.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {
        appendWithIndent("class ");
        builder.append(classDecl.name());
        if (classDecl.superName() != null) {
            builder.append(" extends ");
            builder.append(classDecl.superName());
        }
        builder.append(" {\n");

        indent++;
        for (var fieldDecl : classDecl.fields()) {
            fieldDecl.accept(this);
            builder.append("\n");
        }
        for (var methodDecl : classDecl.methoddecls()) {
            methodDecl.accept(this);
            builder.append("\n");
        }
        indent--;
        appendWithIndent("}\n");
    }

    @Override
    public void visit(MainClass mainClass) {
        // This is a new scope -> create new symbol table
        SymbolTable symbolTable = new SymbolTable(SymbolTableUtils.getRoot());
        SymbolTableUtils.addSymbolTable(mainClass.name(), symbolTable);
        // MainClass has argsName parameter only - create symbol
        SymbolTableUtils.getCurrSymTable().addSymbol(mainClass, mainClass.argsName(), Type.VARIABLE, "String[]");
        mainClass.mainStatement().accept(this);
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        appendWithIndent("");
        methodDecl.returnType().accept(this);
        builder.append(" ");
        builder.append(methodDecl.name());
        builder.append("(");

        String delim = "";
        for (var formal : methodDecl.formals()) {
            builder.append(delim);
            formal.accept(this);
            delim = ", ";
        }
        builder.append(") {\n");

        indent++;

        for (var varDecl : methodDecl.vardecls()) {
            varDecl.accept(this);
        }
        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }

        appendWithIndent("return ");
        methodDecl.ret().accept(this);
        builder.append(";");
        builder.append("\n");

        indent--;
        appendWithIndent("}\n");
    }

    @Override
    public void visit(FormalArg formalArg) {
        formalArg.type().accept(this);
        builder.append(" ");
        builder.append(formalArg.name());
    }

    @Override
    public void visit(VarDecl varDecl) {
        appendWithIndent("");
        varDecl.type().accept(this);
        builder.append(" ");
        builder.append(varDecl.name());
        builder.append(";\n");
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        appendWithIndent("{");
        indent++;
        for (var s : blockStatement.statements()) {
            builder.append("\n");
            s.accept(this);
        }
        indent--;
        builder.append("\n");
        appendWithIndent("}\n");
    }

    @Override
    public void visit(IfStatement ifStatement) {
        appendWithIndent("if (");
        ifStatement.cond().accept(this);
        builder.append(")\n");
        indent++;
        ifStatement.thencase().accept(this);
        indent--;
        appendWithIndent("else\n");
        indent++;
        ifStatement.elsecase().accept(this);
        indent--;
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        appendWithIndent("while (");
        whileStatement.cond().accept(this);
        builder.append(") {");
        indent++;
        whileStatement.body().accept(this);
        indent--;
        builder.append("\n");
        appendWithIndent("}\n");
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        sysoutStatement.arg().accept(this);
        builder.append(");\n");
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        appendWithIndent("");
        builder.append(assignStatement.lv());
        builder.append(" = ");
        assignStatement.rv().accept(this);
        builder.append(";\n");
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        appendWithIndent("");
        builder.append(assignArrayStatement.lv());
        builder.append("[");
        assignArrayStatement.index().accept(this);
        builder.append("]");
        builder.append(" = ");
        assignArrayStatement.rv().accept(this);
        builder.append(";\n");
    }

    @Override
    public void visit(AndExpr e) {
    }

    @Override
    public void visit(LtExpr e) {
        visitBinaryExpr(e, "<");
        ;
    }

    @Override
    public void visit(AddExpr e) {
    }

    @Override
    public void visit(SubtractExpr e) {
    }

    @Override
    public void visit(MultExpr e) {
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
        Symbol symbol = SymbolTableUtils.getCurrSymTable().resolveKey(SymbolTable.createKey(e.methodId(), Type.METHOD));
        if (symbol == null) {
            // TODO - handle error
            return;
        }
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
        Symbol symbol = SymbolTableUtils.getCurrSymTable().resolveKey(SymbolTable.createKey(e.id(), Type.VARIABLE));
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
        builder.append(e.classId());

    }

    @Override
    public void visit(NotExpr e) {
        builder.append("!(");
        e.e().accept(this);
        builder.append(")");
    }

    @Override
    public void visit(IntAstType t) {
        builder.append("int");
    }

    @Override
    public void visit(BoolAstType t) {
        builder.append("boolean");
    }

    @Override
    public void visit(IntArrayAstType t) {
        builder.append("int[]");
    }

    @Override
    public void visit(RefType t) {
        builder.append(t.id());
    }
}
