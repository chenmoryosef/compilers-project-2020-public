package ast;

public class AstLlvmPrintVisitor implements Visitor {
    private StringBuilder builder = new StringBuilder();

    private int indent = 0;
    private int ifCnt = 0;
    private int regCnt = 0;
    private String currentClass;
    private String currentRegisterToAssign;

    private String astNodeToLlvmType(AstNode node) {
        if (node instanceof IntAstType ||
                node instanceof SubtractExpr ||
                node instanceof AddExpr ||
                node instanceof MultExpr || node instanceof IntegerLiteralExpr) {
            return "i32";
        } else if (node instanceof BoolAstType || node instanceof AndExpr || node instanceof LtExpr) {
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
        String firstArg, secondArg;
        e.e1().accept(this);
        if (e.e1() instanceof IntegerLiteralExpr) {
            firstArg = "i32 " + ((IntegerLiteralExpr) e.e1()).num();
        } else {
            firstArg = "i32 %_" + this.getLastRegisterCount();
        }
        e.e2().accept(this);
        if (e.e2() instanceof IntegerLiteralExpr) {
            secondArg = "" + ((IntegerLiteralExpr) e.e2()).num();
        } else {
            secondArg = "%_" + this.getLastRegisterCount();
        }
        int resultRegister = this.invokeRegisterCount();
        builder.append("%_");
        builder.append(resultRegister);
        builder.append(" = ");
        builder.append(infixSymbol);
        builder.append(" ");
        builder.append(firstArg);
        builder.append(",");
        builder.append(secondArg);
        builder.append("\n");
    }


    String verbatim = "declare i8* @calloc(i32, i32)\n" +
            "declare i32 @printf(i8*, ...)\n" +
            "declare void @exit(i32)\n" +
            "\n" +
            "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n" +
            "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n" +
            "define void @print_int(i32 %i) {\n" +
            "\t%_str = bitcast [4 x i8]* @_cint to i8*\n" +
            "\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n" +
            "\tret void\n" +
            "}\n" +
            "\n" +
            "define void @throw_oob() {\n" +
            "\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n" +
            "\tcall i32 (i8*, ...) @printf(i8* %_str)\n" +
            "\tcall void @exit(i32 1)\n" +
            "\tret void\n" +
            "}\n\n";

    @Override
    public void visit(Program program) {
        // TODO EX2 - concat vtable to llvm code output  string
        builder.append(verbatim);
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
        builder.append("define i32 @main() {\n");
        mainClass.mainStatement().accept(this);
        builder.append("ret i32 0\n");
        builder.append("}\n\n");
        ifCnt = 0;
        regCnt = 0;
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        // TODO EX2 - print declaration
        String returntype = astNodeToLlvmType(methodDecl.returnType());
        String formaltype;
        String delim = ", ";
        // define i32 @Base.set(i8* %this, i32 %.x) {
        builder.append("define " + returntype + " @" + this.currentClass + "." + methodDecl.name() + "(i8* %this");
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
        builder.append(returntype);
        if (methodDecl.ret() instanceof IntegerLiteralExpr) {
            builder.append(" ");
            builder.append(((IntegerLiteralExpr) methodDecl.ret()).num());
        } else if (methodDecl.ret() instanceof TrueExpr) {
            builder.append(" 1");
        } else if (methodDecl.ret() instanceof FalseExpr) {
            builder.append(" 0");
        } else {
            builder.append(" %_");
            builder.append(this.getLastRegisterCount());
        }
        builder.append("\n}\n");

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
        builder.append(" = ");
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
        builder.append("\n");
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
        // TODO EX2 - handle ref-id or int-literal
        sysoutStatement.arg().accept(this);
        if (sysoutStatement.arg() instanceof IntegerLiteralExpr) {
            //print-int(sysoutStatement.arg());
            builder.append("call i32 @print_int(i32 " + sysoutStatement.arg() + ")");
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
        if (sysoutStatement.arg() instanceof IdentifierExpr) {
            // TODO: type variable for objects
            // Resolve Variable Type
            // Load that variable into register

        }
        builder.append("\n");
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        // TODO EX2 - handle ref-id or int-literal

        //Todo : update currentlvalue
        assignStatement.rv().accept(this);
        if (assignStatement.rv() instanceof AddExpr || assignStatement.rv() instanceof SubtractExpr || assignStatement.rv() instanceof MultExpr || assignStatement.rv() instanceof ArrayAccessExpr) {
            builder.append("store i32 %_" + getLastRegisterCount() + ", i32* %" + assignStatement.lv());
            builder.append("\n");
        }
        if (assignStatement.rv() instanceof IntegerLiteralExpr) {
            //print-int(sysoutStatement.arg());
            builder.append("store i32 " + ((IntegerLiteralExpr) assignStatement.rv()).num() + ", i32* %" + assignStatement.lv());
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
        if(assignStatement.rv() instanceof NewObjectExpr) {
            // store i8* %_0, i8** %b
            builder.append("store i8* %_");
            builder.append(this.currentRegisterToAssign);
            builder.append(", i8** %_");
            builder.append(assignStatement.lv());
            builder.append("\n");
        }
        if (assignStatement.rv() instanceof NewIntArrayExpr) {
            // store i32* %_3, i32** %x
            builder.append("store i32* %_");
            builder.append(this.currentRegisterToAssign);
            builder.append(", i32** %");
            builder.append(assignStatement.lv());
        }
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
        visitBinaryExpr(e, "icmp slt");
    }

    @Override
    public void visit(AddExpr e) {
        visitBinaryExpr(e, "add");
    }

    @Override
    public void visit(SubtractExpr e) {
        visitBinaryExpr(e, "sub");
    }

    @Override
    public void visit(MultExpr e) {
        visitBinaryExpr(e, "mul");
    }

    @Override
    public void visit(ArrayAccessExpr e) {
        // TODO EX2 - %_reg = get element name of array in 1+e.indexExpr().accept index
        int arrayRegister = this.invokeRegisterCount();
        // TODO EX2 - store array to array register
        if (e.arrayExpr() instanceof IdentifierExpr) {
            // %_1 = load i32*, i32** %x
            builder.append("%_");
            builder.append(arrayRegister);
            builder.append(" = load i32*, i32** %");
            builder.append(((IdentifierExpr) e.arrayExpr()).id());
            builder.append("\n");
        } else {
            // TODO EX2 - think of how to get array register
            e.arrayExpr().accept(this);
        }


        // TODO EX2 - make sure to handle int-literal
        int indexResultRegister = this.invokeRegisterCount();
        int labelIllegalIndex = this.ifCnt++;
        int labelLegalIndex = this.ifCnt++;
        String indexValue;
        e.indexExpr().accept(this);
        // check len size is not negative
        // %_0 = icmp slt i32 %_2, %_size
        builder.append("%_");
        builder.append(indexResultRegister);
        builder.append(" = icmp slt i32 ");
        // TODO EX2 - notice to handle %refId.name separately and int-literal
        if (e.indexExpr() instanceof IntegerLiteralExpr) {
            indexValue = "i32 " + ((IntegerLiteralExpr) e.indexExpr()).num();
        } else {
            indexValue = "i32 %_" + this.getLastRegisterCount();
        }
        builder.append(indexValue);
        builder.append(", 0\n");
        //	br i1 %_0, label %arr_alloc0, label %arr_alloc1
        builder.append("br i1 %_");
        builder.append(indexResultRegister);
        builder.append(", label %if");
        builder.append(labelIllegalIndex);
        builder.append(", label %if");
        builder.append(labelLegalIndex);
        builder.append("\n");
        builder.append("if");
        builder.append(labelIllegalIndex);
        builder.append(":\n");
        builder.append("call void @throw_oob()\n");
        builder.append("br label %if");
        builder.append(labelLegalIndex);
        builder.append("\n");
        builder.append("if");
        builder.append(labelLegalIndex);
        builder.append(":\n");

        // TODO EX2 - make sure index is not OOB


        // TODO EX2 - check array access is valid

        // %_9 = add i32 0, 1
        int indexRegister = this.invokeRegisterCount();
        builder.append("%_");
        builder.append(indexRegister);
        builder.append(" = add ");
        builder.append(indexValue);
        builder.append(", 1\n");
        // %_10 = getelementptr i32, i32* %_4, i32 %_9
        int valueRegister = this.invokeRegisterCount();
        builder.append("%_");
        builder.append(valueRegister);
        builder.append(" = getelementptr i32, i32* %_");
        builder.append(arrayRegister);
        builder.append(", %_");
        builder.append(indexRegister);
        builder.append("\n");
        // %_11 = load i32, i32* %_10
        int loadRegister = this.invokeRegisterCount();
        builder.append("%_");
        builder.append(loadRegister);
        builder.append(" = load i32, i32* %_");
        builder.append(valueRegister);
        builder.append("\n");
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        // TODO EX2 - %_reg = get element name of array in 0 index
        int lengthRegister = this.invokeRegisterCount();
        String lengthValue;
        if (e.arrayExpr() instanceof NewIntArrayExpr) {
            AstNode lengthAst = ((NewIntArrayExpr) e.arrayExpr()).lengthExpr();
            if (lengthAst instanceof IntegerLiteralExpr) {
                lengthValue = "i32 " + ((IntegerLiteralExpr) lengthAst).num();
            } else {
                lengthAst.accept(this);
                lengthValue = "i32 %_" + this.getLastRegisterCount();
            }
            builder.append("%_");
            builder.append(lengthRegister);
            builder.append(" = ");
            builder.append(lengthValue);
            builder.append("\n");
        } else if (e.arrayExpr() instanceof IdentifierExpr) {
            // %_4 = load i32*, i32** %x
            int arrayRegister = this.invokeRegisterCount();
            builder.append("%_");
            builder.append(arrayRegister);
            builder.append(" = ");
            builder.append("load i32*, i32** %");
            builder.append(((IdentifierExpr) e.arrayExpr()).id());
            builder.append("\n");
            // %_10 = getelementptr i32, i32* %_4, i32 0
            builder.append("%_");
            builder.append(lengthRegister);
            builder.append(" = ");
            builder.append("getelementptr i32, i32* %_");
            builder.append(arrayRegister);
            builder.append(", i32 0");
            builder.append("\n");
        }
    }

    @Override
    public void visit(MethodCallExpr e) {
        AstNode ownerExp = e.ownerExpr();
        String classId;
        // TODO EX2 - resolve class of owner (this, new, ref-id)  -> class
        if (ownerExp instanceof ThisExpr) {
            classId = this.currentClass;
        } else if (ownerExp instanceof NewObjectExpr) {
            classId = ((NewObjectExpr) ownerExp).classId();
        } else {
            // TODO EX2 - think how to resolve class id of this case....
            classId = "";
        }
        // Assume this was done in accept:
        // %_6 = load i8*, i8** %b
        e.ownerExpr().accept(this);
        // %_7 = bitcast i8* %_6 to i8***
        String loadedRegister = this.currentRegisterToAssign;
        int bitcastRegister = this.invokeRegisterCount();
        builder.append("%_");
        builder.append(bitcastRegister);
        builder.append(" = bitcast i8* %_");
        builder.append(loadedRegister);
        builder.append(" to i8***\n");
        //	%_8 = load i8**, i8*** %_7
        int vtableRegister = this.invokeRegisterCount();
        builder.append("%_");
        builder.append(vtableRegister);
        builder.append(" = load i8**, i8*** %_");
        builder.append(bitcastRegister);
        builder.append("\n");
        // %_9 = getelementptr i8*, i8** %_8, i32 0
        int methodRegister = this.invokeRegisterCount();
        // Resolve method offset according object struct to map
        MethodeInfo methodeInfo = VtableCreator.getObjectStructMap().get(classId).getMethodeInfoMap().get(e.methodId());
        int methodOffset = methodeInfo.getOffset();
        builder.append("%_");
        builder.append(methodRegister);
        builder.append(" = getelementptr i8*, i8** %_");
        builder.append(vtableRegister);
        builder.append(", i32 ");
        builder.append(methodOffset);
        builder.append("\n");
        // %_10 = load i8*, i8** %_9
        int actualFunctionPointerRegister = this.invokeRegisterCount();
        builder.append("%_");
        builder.append(actualFunctionPointerRegister);
        builder.append(" = load i8*, i8** %_");
        builder.append(methodRegister);
        builder.append("\n");
        //	%_11 = bitcast i8* %_10 to i32 (i8*, i32)*
        String returnTypeValue = methodeInfo.getRet();
        String args = methodeInfo.getArgs();
        bitcastRegister = this.invokeRegisterCount();
        builder.append("%_");
        builder.append(bitcastRegister);
        builder.append(" = bitcast i8* %_");
        builder.append(actualFunctionPointerRegister);
        builder.append(" to ");
        builder.append(returnTypeValue);
        builder.append(args);
        builder.append("*\n");

        String delim = "";
        StringBuilder arguments = new StringBuilder();
        for (Expr arg : e.actuals()) {
            arguments.append(delim);
            // TODO EX2 - insert tmp call regCnt - 1
            if (arg instanceof IntegerLiteralExpr) {
                arguments.append("i32 ");
                arguments.append(((IntegerLiteralExpr) arg).num());
            } else if (arg instanceof TrueExpr) {
                arguments.append("i1 1");
            } else if (arg instanceof FalseExpr) {
                arguments.append("i1 0");
            } else {
                arg.accept(this);
                // TODO EX2 - remove comment out when implemented
                // tmp.append(this.getLastRegisterType());
                // tmp.append(" ");
                arguments.append("%_");
                arguments.append(this.getLastRegisterCount());
            }
            delim = ", ";
        }
        // TODO EX2 - print tmp call to llvm code
        // %_12 = call i32 %_11(i8* %_6, i32 1)
        int callRegister = this.invokeRegisterCount();
        builder.append("%_");
        builder.append(callRegister);
        builder.append(" = call ");
        builder.append(returnTypeValue);
        builder.append(" %_");
        builder.append(bitcastRegister);
        builder.append(" (i8* %_");
        builder.append(loadedRegister);
        builder.append(arguments);
        builder.append(")\n");
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
        // TODO EX2 -   %_3 = load i32, i32* %e.id()
        int register = this.invokeRegisterCount();
        builder.append("%_");
        builder.append(register);
        builder.append(" = load ");
        // TODO - resolve type from symbol table
        // String type = resolveType(symbol);
        String type = "i32";
        builder.append(type);
        builder.append(", ");
        builder.append(type);
        builder.append("* %");
        builder.append(e.id());
        builder.append("\n");
    }

    public void visit(ThisExpr e) {

    }

    @Override
    public void visit(NewIntArrayExpr e) {
        // TODO EX2 - according to example in class
        int lengthResultRegister = this.invokeRegisterCount();
        int labelIllegalLength = this.ifCnt++;
        int labelLegalLength = this.ifCnt++;
        String lengthValue;
        e.lengthExpr().accept(this);
        // check len size is not negative
        // %_0 = icmp slt i32 %_2, 0
        builder.append("%_");
        builder.append(lengthResultRegister);
        builder.append(" = icmp slt i32 ");
        // TODO EX2 - notice to handle %refId.name separately and int-literal
        if (e.lengthExpr() instanceof IntegerLiteralExpr) {
            lengthValue = "i32 " + ((IntegerLiteralExpr) e.lengthExpr()).num();
        } else {
            lengthValue = "i32 %_" + this.getLastRegisterCount();
        }
        builder.append(lengthValue);
        builder.append(", 0\n");
        //	br i1 %_0, label %arr_alloc0, label %arr_alloc1
        builder.append("br i1 %_");
        builder.append(lengthResultRegister);
        builder.append(", label %if");
        builder.append(labelIllegalLength);
        builder.append(", label %if");
        builder.append(labelLegalLength);
        builder.append("\n");
        builder.append("if");
        builder.append(labelIllegalLength);
        builder.append(":\n");
        builder.append("call void @throw_oob()\n");
        builder.append("br label %if");
        builder.append(labelLegalLength);
        builder.append("\n");
        builder.append("if");
        builder.append(labelLegalLength);
        builder.append(":\n");
        // %_1 = add i32 2, 1
        int sizeOfArrayRegister = this.invokeRegisterCount();
        builder.append("%_");
        builder.append(sizeOfArrayRegister);
        builder.append(" = add i32 ");
        builder.append(lengthValue);
        builder.append(", 1\n");
        // 	%_2 = call i8* @calloc(i32 4, i32 %_1)
        int callocRegister = this.invokeRegisterCount();
        builder.append("%_");
        builder.append(callocRegister);
        builder.append(" = call i8* @calloc(i32 4, i32 ");
        builder.append(lengthValue);
        builder.append(")\n");
        // %_3 = bitcast i8* %_2 to i32*
        int bitcastRegister = this.invokeRegisterCount();
        builder.append("%_");
        builder.append(bitcastRegister);
        builder.append(" = bitcast i8* %_");
        builder.append(callocRegister);
        builder.append(" to i32*)\n");
        // store i32 2, i32* %_3
        builder.append("store i32 ");
        builder.append(lengthValue);
        builder.append(", i32* %_");
        builder.append(bitcastRegister);
        this.currentRegisterToAssign = "" + bitcastRegister;
    }

    @Override
    public void visit(NewObjectExpr e) {
        // TODO EX2 - use struct from pre-ex2 visitor + use Yotam's example
        int objectReg = this.invokeRegisterCount();
        int vTable = this.invokeRegisterCount();
        int vTableFirstElement = this.invokeRegisterCount();
        ObjectStruct objectStruct = VtableCreator.getObjectStructMap().get(e.classId());
        int sizeOfObject = objectStruct.getSizeInBytes();
        int methodsCount = objectStruct.getMethodeInfoMap().size();
        builder.append("%_");
        builder.append(objectReg);
        builder.append(" = call i8* @calloc(i32 1, i32 ");
        builder.append(sizeOfObject);
        builder.append(")\n");
        builder.append("%_");
        builder.append(vTable);
        builder.append(" = bitcast i8* %_");
        builder.append(objectReg);
        builder.append(" to i8***\n");
        // %_2 = getelementptr [2 x i8*], [2 x i8*]* @.Base_vtable, i32 0, i32 0
        builder.append("%_");
        builder.append(vTableFirstElement);
        builder.append(" = getelementptr [");
        builder.append(methodsCount);
        builder.append(" x i8*], [");
        builder.append(methodsCount);
        builder.append(" x i8*]* @.");
        builder.append(e.classId());
        builder.append("_vtable, i32 0, i32 0\n");
        // store i8** %_2, i8*** %_1
        builder.append("store i8** %_");
        builder.append(vTableFirstElement);
        builder.append(", i8*** %_");
        builder.append(vTable);
        builder.append("\n");
        this.currentRegisterToAssign = "" + objectReg;
    }

    @Override
    public void visit(NotExpr e) {
        // TODO EX2 - branch over accept() result
        StringBuilder tmpBuilder = new StringBuilder();
        if (e.e() instanceof TrueExpr || e.e() instanceof FalseExpr) {
            tmpBuilder.append("%_");
            tmpBuilder.append(this.invokeRegisterCount());
            tmpBuilder.append("=");
            tmpBuilder.append(e.e() instanceof TrueExpr ? "i1 0" : "i1 1");
            tmpBuilder.append("\n");
        } else {
            e.e().accept(this);
            int trueIfCnt = this.ifCnt++;
            int falseIfCnt = this.ifCnt++;
            int branchIfCnt = this.ifCnt++;
            tmpBuilder.append("br i1");
            tmpBuilder.append("%_");
            tmpBuilder.append(this.getLastRegisterCount());
            tmpBuilder.append(" label %if");
            tmpBuilder.append(trueIfCnt);
            tmpBuilder.append(", label %if");
            tmpBuilder.append(falseIfCnt);
            tmpBuilder.append("\n");
            tmpBuilder.append("if");
            tmpBuilder.append(trueIfCnt);
            tmpBuilder.append(":\n");
            tmpBuilder.append("br label %if");
            tmpBuilder.append(branchIfCnt);
            tmpBuilder.append("\n");
            tmpBuilder.append("if");
            tmpBuilder.append(falseIfCnt);
            tmpBuilder.append(":\n");
            tmpBuilder.append("br label %if");
            tmpBuilder.append(branchIfCnt);
            tmpBuilder.append("\n");
            tmpBuilder.append("if");
            tmpBuilder.append(branchIfCnt);
            tmpBuilder.append(":\n");
            tmpBuilder.append("%_");
            tmpBuilder.append(this.invokeRegisterCount());
            tmpBuilder.append("= phi [0, %if");
            tmpBuilder.append(trueIfCnt);
            tmpBuilder.append("], [1, %if");
            tmpBuilder.append(falseIfCnt);
            tmpBuilder.append("]");
        }
        builder.append(tmpBuilder);
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

    private void printCheckIndex() {

    }
}
