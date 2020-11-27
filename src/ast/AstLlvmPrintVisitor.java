package ast;

public class AstLlvmPrintVisitor implements Visitor {
    private StringBuilder builder = new StringBuilder();

    private int indent = 0;
    private int ifCnt = 0;
    private int regCnt = 0;
    private String currentClass;

    // We will use this function to create new register
    private int invokeRegisterCount() {
        return regCnt++;
    }

    private int getLastRegisterCount() {
        return regCnt - 1;
    }

    private void resetRegisterCount() {
        regCnt = 0;
    }

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
        // TODO EX2 - concat vtable to llvm code output  string
        // TODO EX2 - concat mandatory string (@throw_oob, print_int)
        program.mainClass().accept(this);
        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {
        currentClass = classDecl.name();
        for (var methodDecl : classDecl.methoddecls()) {
            methodDecl.accept(this);
            builder.append("\n");
        }
    }

    @Override
    public void visit(MainClass mainClass) {
        appendWithIndent("class ");
        builder.append(mainClass.name());
        builder.append(" {\n");
        indent++;
        appendWithIndent("public static void main(String[] ");
        builder.append(mainClass.argsName());
        builder.append(") {");
        builder.append("\n");
        indent++;
        mainClass.mainStatement().accept(this);
        indent--;
        appendWithIndent("}\n");
        indent--;
        appendWithIndent("}\n");
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        // TODO EX2 - print declaration
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
        // TODO EX2 - Reset ifCnt and regCnt
    }

    @Override
    public void visit(FormalArg formalArg) {
        formalArg.type().accept(this);
        builder.append(" ");
        builder.append(formalArg.name());
    }

    @Override
    public void visit(VarDecl varDecl) {
        // TODO EX2 - %varDecl.name = alloca varDecl.type().accept(this);
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
        // TODO EX2 - cond accept writes the bool result to a register
        ifStatement.cond().accept(this);
        // TODO EX2 - label #0
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
        // TODO EX2 - create whileBody label
        // TODO EX2 - compute condition
        // TODO EX2 - branch over condition value
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
        appendWithIndent("System.out.println(");
        // TODO EX2 - handle ref-id or int-literal
        sysoutStatement.arg().accept(this);
        builder.append(");\n");
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        appendWithIndent("");
        // TODO EX2 - handle ref-id or int-literal
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
        // TODO EX2 - compute index - handle ref-id or int-literal
        assignArrayStatement.index().accept(this);
        builder.append("]");
        // TODO EX2 - validate index - make sure it is not OOB

        // TODO EX2 - compute l value - make sure to add 1 (due to index 0 - size) to index
        // TODO EX2 - see example
        builder.append(" = ");
        assignArrayStatement.rv().accept(this);
        // TODO EX2 - put rv in lv - store value in array
        builder.append(";\n");
    }

    @Override
    public void visit(AndExpr e) {
        // TODO EX2 - straight forward according to SCE example
        visitBinaryExpr(e, "&&");
    }

    @Override
    public void visit(LtExpr e) {
        // TODO EX2 - write 'icmp slt' command
        visitBinaryExpr(e, "<");;
    }

    @Override
    public void visit(AddExpr e) {
        // TODO EX2 - notice - expressions can be literals (literal int) then we need to explicitly write it down
        // Example -
        //        x = x + 1
        //        %_0 = load i32, i32* %x
        //        %_1 = add i32 %_0, i32 1
        // It means we
        if (e.e1().getClass() == IntegerLiteralExpr.class )
        {

        }
        else {
            e.e1().accept(this);
        }
        visitBinaryExpr(e, "+");;
    }

    @Override
    public void visit(SubtractExpr e) {
        visitBinaryExpr(e, "-");
    }

    @Override
    public void visit(MultExpr e) {
        visitBinaryExpr(e, "*");
    }

    @Override
    public void visit(ArrayAccessExpr e) {
        // TODO EX2 - %_reg = get element name of array in 1+e.indexExpr().accept index
        builder.append("(");
        e.arrayExpr().accept(this);
        builder.append(")");
        builder.append("[");
        // TODO EX2 - make sure to handle int-literal
        e.indexExpr().accept(this);
        builder.append("]");
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        // TODO EX2 - %_reg = get element name of array in 0 index
        builder.append("(");
        e.arrayExpr().accept(this);
        builder.append(")");
        builder.append(".length");
    }

    @Override
    public void visit(MethodCallExpr e) {
        builder.append("(");
        // TODO EX2 - resolve class of owner (this, new, ref-id)  -> class
        e.ownerExpr().accept(this);
        builder.append(")");
        builder.append(".");
        // TODO EX2 - resolve method offset according to map
        builder.append(e.methodId());
        builder.append("(");

        String delim = "";
        for (Expr arg : e.actuals()) {
            // TODO EX2 - tmp line call
            builder.append(delim);
            arg.accept(this);
            // TODO EX2 - insert tmp call regCnt - 1
            delim = ", ";
        }
        // TODO EX2 - print tmp call to llvm code
        builder.append(")");
    }

    @Override
    public void visit(IntegerLiteralExpr e) {
        builder.append(e.num());
    }

    @Override
    public void visit(TrueExpr e) {
        builder.append("true");
    }

    @Override
    public void visit(FalseExpr e) {
        builder.append("false");
    }

    @Override
    public void visit(IdentifierExpr e) {
        // TODO EX2 - verify nothing to do here
        builder.append(e.id());
    }

    public void visit(ThisExpr e) {
        builder.append("this");
    }

    @Override
    public void visit(NewIntArrayExpr e) {
        // TODO EX2 - according to example in class
        builder.append("new int[");
        // TODO EX2 - notice to handle %refId.name separately and int-literal
        e.lengthExpr().accept(this);
        builder.append("]");
    }

    @Override
    public void visit(NewObjectExpr e) {
        builder.append("new ");
        // TODO EX2 - use struct from pre-ex2 visitor + use Yotam's example
        builder.append(e.classId());
        builder.append("()");
    }

    @Override
    public void visit(NotExpr e) {
        // TODO EX2 - branch over accept() result
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
