package ast;

public class AstLlvmPrintVisitor implements Visitor {
    private StringBuilder builder = new StringBuilder();

    private int indent = 0;
    private int ifCnt = 0;
    private int regCnt = 0;
    private String currentClass;

    private String astNodeToLlvmType(AstNode node) {
        if (node instanceof IntAstType) {
            return "i32";
        } else if (node instanceof BoolAstType) {
            return "i1";
        } else if (node instanceof IntArrayAstType) {
            return "i32*";
        } else if (node instanceof RefType) {
            return "i8*";
        } else {
            return "void";
        }
    }

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
        String returntype = astNodeToLlvmType(methodDecl.returnType());
        String formaltype;
        String delim = ", ";
        // define i32 @Base.set(i8* %this, i32 %.x) {
        builder.append("define " + returntype + " @" + this.currentClass + "." + methodDecl.name() + "(i8* this");
        for (FormalArg formal : methodDecl.formals()) {
            builder.append(delim);
            if (formal.type() instanceof IntAstType) {
                formaltype = "i32";
            } else if (formal.type() instanceof BoolAstType) {
                formaltype = "i1";
            } else if (formal.type() instanceof IntArrayAstType) {
                formaltype = "i32*";
            } else {
                formaltype = "i82*";
            }
            builder.append(formaltype + " %" + formal.name());
        }
        builder.append(") {");
        builder.append("\n");

        for (var varDecl : methodDecl.vardecls()) {
            varDecl.accept(this);
        }

        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }

        returntype = astNodeToLlvmType(methodDecl.ret());
        methodDecl.ret().accept(this);
        builder.append("ret ");
        builder.append(returntype + " ");
        builder.append(this.getLastRegisterCount());
        builder.append(";");
        builder.append("\n");

        // TODO EX2 - Reset ifCnt and regCnt
        ifCnt = 0;
        regCnt = 0;


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
        builder.append("%" + varDecl.name());
        builder.append("=");
        builder.append("alloca ");
        if (varDecl.type() instanceof IntAstType) {
            builder.append("i32");
        }
        if (varDecl.type() instanceof BoolAstType) {
            builder.append("i1");
        }
        if (varDecl.type() instanceof IntArrayAstType) {
            builder.append("i32*");
        }
        if (varDecl.type() instanceof RefType) {
            builder.append("i8*");
        }


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
        builder.append("br ");
        builder.append("i1 ");
        builder.append("%_" + getLastRegisterCount() + ", ");
        builder.append("label ");
        ifCnt++;
        int firstLabel = ifCnt;
        builder.append("%if" + ifCnt + ", ");
        builder.append("label ");
        ifCnt++;
        int secondLabel = ifCnt;
        builder.append("%if" + ifCnt + " ");
        builder.append("\n");
        ifCnt++;
        int thirdLabel = ifCnt;

        builder.append("%if" + firstLabel + ":");
        builder.append("\n");
        // TODO EX2 - label #0
        ifStatement.thencase().accept(this);
        builder.append("br ");
        builder.append("%if" + thirdLabel);
        builder.append("\n");
        appendWithIndent("else\n");
        builder.append("%if" + secondLabel + ":");
        builder.append("\n");
        ifStatement.elsecase().accept(this);
        builder.append("br ");
        builder.append("%if" + thirdLabel);
        builder.append("\n");
        builder.append("%if" + thirdLabel + ":");
        builder.append("\n");
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        appendWithIndent("while (");
        // TODO EX2 - create whileBody label
        // TODO EX2 - compute condition
        // TODO EX2 - branch over condition value

        ifCnt++;
        int whileLabel = ifCnt;
        builder.append("%if" + ifCnt + ":");
        builder.append("\n");
        whileStatement.body().accept(this);
        ifCnt++;
        int outLabel = ifCnt;
        builder.append("%if" + ifCnt + ":");
        builder.append("\n");
        whileStatement.cond().accept(this);
        if (whileStatement.cond() instanceof TrueExpr || whileStatement.cond() instanceof FalseExpr) {
            builder.append("br label %if" + whileLabel);
            builder.append("\n");
        } else {
            whileStatement.cond().accept(this);
            builder.append("br i1 %_" + getLastRegisterCount() + ", label %if" + whileLabel + ", label %if" + outLabel);
        }
        indent++;
        indent--;
        builder.append("\n");
        appendWithIndent("}\n");
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        appendWithIndent("System.out.println(");
        // TODO EX2 - handle ref-id or int-literal
        sysoutStatement.arg().accept(this);
        if (sysoutStatement.arg() instanceof IntegerLiteralExpr) {
            //print-int(sysoutStatement.arg());
            builder.append("call i32 @print-int(i32 " + sysoutStatement.arg() + ")");
            builder.append("\n");
        }
        if (sysoutStatement.arg() instanceof LtExpr) {
            builder.append("call i32 @puts(i8* " + "%_" + getLastRegisterCount() + ")");
            builder.append("\n");
        }
        if (sysoutStatement.arg() instanceof AddExpr || sysoutStatement.arg() instanceof SubtractExpr || sysoutStatement.arg() instanceof MultExpr || sysoutStatement.arg() instanceof ArrayAccessExpr) {
            builder.append("call i32 @puts(i32 " + "%_" + getLastRegisterCount() + ")");
            builder.append("\n");
        }
        if (sysoutStatement.arg() instanceof MethodCallExpr) {
            //Todo : access symboltable to get type
        }

        if (sysoutStatement.arg() instanceof TrueExpr) {
            builder.append("call i32 @puts(i1 " + "1");
            builder.append("\n");
        }
        if (sysoutStatement.arg() instanceof FalseExpr) {
            builder.append("call i32 @puts(i1 " + "0");
            builder.append("\n");
        }
        // TODO: type variable for objects


        builder.append(");\n");
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        appendWithIndent("");
        // TODO EX2 - handle ref-id or int-literal

        //Todo : update currentlvalue
        assignStatement.rv().accept(this);
        if (assignStatement.rv() instanceof AddExpr || assignStatement.rv() instanceof SubtractExpr || assignStatement.rv() instanceof MultExpr || assignStatement.rv() instanceof ArrayAccessExpr) {
            builder.append("store i32 %_" + getLastRegisterCount() + ", i32* %" + assignStatement.lv());
            builder.append("\n");
        }
        if (assignStatement.rv() instanceof IntegerLiteralExpr) {
            //print-int(sysoutStatement.arg());
            builder.append("store i32 %_" + ((IntegerLiteralExpr) assignStatement.rv()).num() + ", i32* %" + assignStatement.lv());
            builder.append("\n");
        }
        if (assignStatement.rv() instanceof LtExpr) {
            builder.append("store i1 %_" + getLastRegisterCount() + ", i1 %" + assignStatement.lv());
            builder.append("\n");
        }
        if (assignStatement.rv() instanceof TrueExpr) {
            builder.append("store i1 1 " + ", i1 %" + assignStatement.lv());
            builder.append("\n");
        }
        if (assignStatement.rv() instanceof FalseExpr) {
            builder.append("store i1 0 " + ", i1 %" + assignStatement.lv());
            builder.append("\n");
        }


        builder.append(";\n");
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        appendWithIndent("");
        assignArrayStatement.index().accept(this);
        if (assignArrayStatement.index() instanceof IntegerLiteralExpr) {


            // TODO EX2 - compute index - handle ref-id or int-literal
            regCnt++;
            builder.append("%_" + regCnt + " = load i32*, i32** %" + assignArrayStatement.lv());
            builder.append("\n");
            // %_5 = icmp slt i32 0, 0
            regCnt++;
            int IsPositive = regCnt;
            ifCnt++;
            int bad_index = ifCnt;
            ifCnt++;
            int goodindex = ifCnt;

            builder.append("%_" + regCnt + " = icmp slt i32 " + assignArrayStatement.index() + ", 0");
            builder.append("\n");
            // br i1 %_5, label %arr_alloc2, label %arr_alloc3

            builder.append("br i1 %_" + IsPositive + ", label %if" + bad_index + ", label %if" + goodindex);
            builder.append("\n");
            builder.append("if" + bad_index + ":");
            builder.append("\n");
            builder.append("call void @throw_oob()");
            builder.append("br label %if" + goodindex);
            builder.append("\n");
            builder.append("if" + goodindex + ":");
            builder.append("\n");


        }

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
        int assignedval;
        ifCnt++;
        int andcond1 = ifCnt;
        ifCnt++;
        int andcond2 = ifCnt;
        ifCnt++;
        int andcond3 = ifCnt;

        e.e1().accept(this);

        if (e.e1() instanceof TrueExpr) {
            builder.append("br i1 1 label %if" + andcond1 + ", label %if" + andcond3);
        } else if (e.e1() instanceof FalseExpr) {

            builder.append("br i1 0 label %if" + andcond1 + ", label %if" + andcond3);
        } else {
            builder.append("br i1 %_" + getLastRegisterCount() + " label %if" + andcond1 + ", label %if" + andcond3);

        }
        builder.append("\n");
        builder.append("if" + andcond1 + ":");
        builder.append("\n");
        e.e2().accept(this);
        if (e.e2() instanceof TrueExpr) {
            regCnt++;
            assignedval = regCnt;
            builder.append("%_" + getLastRegisterCount() + "= i1 1");
            builder.append("\n");
            builder.append("br  label %if" + andcond3);
        } else if (e.e2() instanceof FalseExpr) {
            regCnt++;
            assignedval = regCnt;
            builder.append("%_" + getLastRegisterCount() + "= i1 0");
            builder.append("\n");
            builder.append("br  label %if" + andcond3);
        } else {
            int lastval = getLastRegisterCount();
            regCnt++;
            assignedval = regCnt;
            builder.append("%_" + getLastRegisterCount() + "= " + lastval);
            builder.append("\n");
            builder.append("br i1 %_" + getLastRegisterCount() + " label %if" + andcond1 + ", label %if" + andcond3);

        }
        builder.append("\n");
        builder.append("if" + andcond2 + ":");
        builder.append("\n");
        builder.append("br  label %if" + andcond3);
        builder.append("\n");
        builder.append("if" + andcond3 + ":");
        builder.append("\n");
        invokeRegisterCount();
        builder.append("%_" + getLastRegisterCount() + "= phi i1 [" + assignedval + ",%if" + andcond1 + "], [0, %" + andcond2);
        builder.append("\n");

    }

    @Override
    public void visit(LtExpr e) {
        // TODO EX2 - write 'icmp slt' command
        visitBinaryExpr(e, "<");
        ;
    }

    @Override
    public void visit(AddExpr e) {
        // TODO EX2 - notice - expressions can be literals (literal int) then we need to explicitly write it down
        // Example -
        //        x = x + 1
        //        %_0 = load i32, i32* %x
        //        %_1 = add i32 %_0, i32 1
        // It means we
        if (e.e1().getClass() == IntegerLiteralExpr.class) {

        } else {
            e.e1().accept(this);
        }
        visitBinaryExpr(e, "+");
        ;
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
