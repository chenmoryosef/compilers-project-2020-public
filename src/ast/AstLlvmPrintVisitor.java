package ast;

import symbolTable.Symbol;
import symbolTable.SymbolTable;
import symbolTable.SymbolTableUtils;
import symbolTable.Type;

public class AstLlvmPrintVisitor implements Visitor {
    private StringBuilder builder = new StringBuilder();

    private int indent = 0;
    private int ifCnt = 0;
    private int regCnt = 0;
    private String currentClass;
    private String currentLv;

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
        builder.append(firstArg);
        builder.append(",");
        builder.append(secondArg);
        builder.append("\n");
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
        // TODO EX2 - straight forward according to SCE examplegit s
        visitBinaryExpr(e, "&&");
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
        // TODO EX2 - replace with Gali's code to resolve vtable
        // Assume this was done in accept:
        // %_6 = load i8*, i8** %b
        e.ownerExpr().accept(this);
        // %_7 = bitcast i8* %_6 to i8***
        int loadedRegister = this.getLastRegisterCount();
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
        // TODO EX2 - fill with Gali's value
        // TODO EX2 - resolve method offset according to map
        int methodOffset = 17;
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
        // TODO EX2 - get from Gali
        String returnTypeValue = "i32";
        String args = "i8*, i32";
        bitcastRegister = this.invokeRegisterCount();
        builder.append("%_");
        builder.append(bitcastRegister);
        builder.append(" = bitcast i8*, %_");
        builder.append(actualFunctionPointerRegister);
        builder.append(" to ");
        builder.append(returnTypeValue);
        builder.append(" (");
        builder.append(args);
        builder.append(" )");
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
        builder.append("i8* ");
        builder.append(loadedRegister);
        builder.append(arguments);
        builder.append(")\n");
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
        // store i32* %_3, i32** %x
        builder.append("store i32* %_");
        builder.append(bitcastRegister);
        builder.append(", i32** %");
        builder.append(this.currentLv);
    }

    @Override
    public void visit(NewObjectExpr e) {
        // TODO EX2 - use struct from pre-ex2 visitor + use Yotam's example
        int objectReg = this.invokeRegisterCount();
        int vTable = this.invokeRegisterCount();
        int vTableFirstElement = this.invokeRegisterCount();
        int sizeOfObject = 17;
        int methodsCount = 17;
        builder.append("%_");
        builder.append(objectReg);
        builder.append("= call i8* @calloc(i32 1, i32 ");
        // TODO EX2 - integration with Gali
        //           builder.append(e.classId());
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
        // store i8* %_0, i8** %b
        builder.append("store i8* %_");
        builder.append(objectReg);
        builder.append(", i8** %_");
        builder.append(this.currentLv);
        builder.append("\n");
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
