package ast;

import symbolTable.Symbol;
import symbolTable.SymbolTable;
import symbolTable.SymbolTableUtils;

import java.util.Map;

public class AstLlvmPrintVisitor implements Visitor {
    private StringBuilder builder = new StringBuilder();
    private int ifCnt = 0;
    private int regCnt = 0;
    private String regType;
    private String currentClass;
    private String currentMethod;
    private String currentRegisterToAssign;

    private String resolveVariable(String variableName, String methodName) {
        SymbolTable currentSymbolTable = SymbolTableUtils.getSymbolTableClassWithMethodMap().get(methodName + currentClass);
        String type;
        type = isVariableInSymbolTable(variableName, currentSymbolTable);
        boolean isField = type == null;
        String classId = "";

        if (isField) {
            SymbolTable parentSymbolTable = currentSymbolTable.getParentSymbolTable();
            boolean foundField = false;
            while (parentSymbolTable != null && !foundField) {
                type = isVariableInSymbolTable(variableName, currentSymbolTable);
                foundField = type != null;
                parentSymbolTable = parentSymbolTable.getParentSymbolTable();
            }

            if (foundField) {
                classId = VtableCreator.getSymbolTableClassesMap().get(parentSymbolTable);
            } else {
                // TODO handle error
                System.out.println("ERRORRRRRRRR");
            }
            retrieveField(classId, variableName);
        } else {
            // %_3 = load i32, i32* %e.id()
            int register = invokeRegisterCount(type);
            builder.append("%_");
            builder.append(register);
            builder.append(" = load ");
            builder.append(type);
            builder.append(", ");
            builder.append(type);
            builder.append("* %");
            builder.append(variableName);
            builder.append("\n");
            classId = currentClass;
        }
        return classId;
    }

    private String isVariableInSymbolTable(String variableName, SymbolTable currentSymbolTable) {
        for (Map.Entry<String, Symbol> entry : currentSymbolTable.getEntries().entrySet()) {
            if (entry.getValue().getSymbolName().equals(variableName)) {
                return VtableCreator.convertAstTypeToLLVMRepresention(entry.getValue().getDecl().get(0));
            }
        }
        return null;
    }

    private void retrieveField(String classId, String fieldName) {
        FieldInfo fieldInfo = VtableCreator.getObjectStructMap().get(classId).getFieldInfoMap().get(fieldName);
        int offset = fieldInfo.getOffset();
        String type = fieldInfo.getFieldType();
        // %_3 = getelementptr i8, i8* %this, i32 8
        int registerImplement = invokeRegisterCount("i8");
        builder.append("%_");
        builder.append(registerImplement);
        builder.append(" = getelementptr i8, i8* %this, ");
        builder.append(type);
        builder.append(" ");
        builder.append(offset);
        builder.append("\n");
        //	%_4 = bitcast i8* %_3 to i32*
        int bitcastRegister = invokeRegisterCount("i8*");
        builder.append("%_");
        builder.append(bitcastRegister);
        builder.append(" = bitcast i8* %_");
        builder.append(registerImplement);
        builder.append(" to ");
        builder.append(type);
        builder.append("*\n");
        //	%_5 = load i32, i32* %_4
        int loadRegister = invokeRegisterCount(type);
        builder.append("%_");
        builder.append(loadRegister);
        builder.append(" = load ");
        builder.append(type);
        builder.append(", ");
        builder.append(type);
        builder.append("* %_");
        builder.append(bitcastRegister);
        builder.append("\n");
    }

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

    // functions to keep trace of registers and ifs counts
    private int invokeRegisterCount(String type) {
        regType = type;
        return regCnt++;
    }

    private int invokeIfRegisterCount() {
        return ifCnt++;
    }

    private int getLastRegisterCount() {
        return regCnt - 1;
    }

    private String getLastRegisterType() {
        return regType;
    }

    private int getLastIfRegisterCount() {
        return ifCnt - 1;
    }

    private void resetRegisterCount() {
        regCnt = 0;
    }

    private void resetIfRegisterCount() {
        ifCnt = 0;
    }

    public String getString() {
        return builder.toString();
    }


    private void visitBinaryExpr(BinaryExpr e, String infixSymbol) {
        String firstArg, secondArg;
        e.e1().accept(this);
        if (e.e1() instanceof IntegerLiteralExpr) {
            firstArg = "i32 " + ((IntegerLiteralExpr) e.e1()).num();
        } else if (e.e1() instanceof IdentifierExpr) {
            resolveVariable(((IdentifierExpr) e.e1()).id(), currentMethod);
            firstArg = "i32 %_" + getLastRegisterCount();
        } else {
            firstArg = "i32 %_" + getLastRegisterCount();
        }
        e.e2().accept(this);
        if (e.e2() instanceof IntegerLiteralExpr) {
            secondArg = "" + ((IntegerLiteralExpr) e.e2()).num();
        } else if (e.e2() instanceof IdentifierExpr) {
            resolveVariable(((IdentifierExpr) e.e1()).id(), currentMethod);
            secondArg = "%_" + getLastRegisterCount();
        } else {
            secondArg = "%_" + getLastRegisterCount();
        }

        int resultRegister = invokeRegisterCount("i32");
        // %_2 = add i32 %_1, 7
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
        // concat mandatory string (@throw_oob, print_int)
        builder.append(verbatim);

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
            builder.append("\n");
        }
    }

    @Override
    public void visit(MainClass mainClass) {
        // define i32 @main() {
        builder.append("define i32 @main() {\n");
        // main statements
        mainClass.mainStatement().accept(this);
        // TODO - can main returns value
        builder.append("ret i32 0\n");
        builder.append("}\n\n");
        resetIfRegisterCount();
        resetRegisterCount();
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        String returntype = astNodeToLlvmType(methodDecl.returnType());
        String formaltype;
        String delim = ", ";
        // define i32 @Base.set(i8* %this, i32 %.x) {
        builder.append("define ");
        builder.append(returntype);
        builder.append(" @");
        builder.append(currentClass);
        builder.append(".");
        builder.append(methodDecl.name());
        builder.append("(i8* %this");
        for (FormalArg formal : methodDecl.formals()) {
            builder.append(delim);
            formaltype = astNodeToLlvmType(formal.type());
            builder.append(formaltype);
            builder.append(" %.");
            builder.append(formal.name());
        }
        builder.append(") {");
        builder.append("\n");

        // alloca every formal variable
        for (FormalArg formal : methodDecl.formals()) {
            formal.accept(this);
        }

        // alloca every local variable
        for (var varDecl : methodDecl.vardecls()) {
            varDecl.accept(this);
        }

        // write method statements
        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }

        // compute return value
        methodDecl.ret().accept(this);

        // return value can be:
        // ret i1 1
        // ret i32 %_3
        returntype = astNodeToLlvmType(methodDecl.ret());
        builder.append("ret ");
        builder.append(returntype);
        if (methodDecl.ret() instanceof IntegerLiteralExpr) {
            builder.append(" ");
            builder.append(((IntegerLiteralExpr) methodDecl.ret()).num());
        } else if (methodDecl.ret() instanceof TrueExpr) {
            builder.append(" 1");
        } else if (methodDecl.ret() instanceof FalseExpr) {
            builder.append(" 0");
        } else if (methodDecl.ret() instanceof IdentifierExpr) {
            // Resolve Variable Type
            // Load that variable into register
            this.resolveVariable(((IdentifierExpr) methodDecl.ret()).id(), this.currentMethod);
        } else {
            builder.append(" %_");
            builder.append(getLastRegisterCount());
        }
        builder.append("\n}\n");

        // Reset ifCnt and regCnt
        resetIfRegisterCount();
        resetIfRegisterCount();
    }

    @Override
    public void visit(FormalArg formalArg) {
        // %varDecl.name = alloca varDecl.type()
        builder.append("%");
        builder.append(formalArg.name());
        builder.append(" = ");
        builder.append("alloca ");
        builder.append(astNodeToLlvmType(formalArg.type()));
        builder.append("\n");
    }

    @Override
    public void visit(VarDecl varDecl) {
        // %varDecl.name = alloca varDecl.type()
        builder.append("%");
        builder.append(varDecl.name());
        builder.append(" = ");
        builder.append("alloca ");
        builder.append(astNodeToLlvmType(varDecl.type()));
        builder.append("\n");
    }

    @Override
    public void visit(BlockStatement blockStatement) {
    }

    @Override
    public void visit(IfStatement ifStatement) {
        int trueLabel = invokeIfRegisterCount();
        int falseLabel = invokeIfRegisterCount();

        // handle True and False Expr
        ifStatement.cond().accept(this);
        if (ifStatement.cond() instanceof TrueExpr) {
            // br label %if1
            builder.append("br label %if");
            builder.append(trueLabel);

        } else if (ifStatement.cond() instanceof FalseExpr) {
            // br label %if2
            builder.append("br label %if");
            builder.append(falseLabel);
        } else {
            // cond accept writes the bool result to the last register
            // br i1 %_1, label %if0, label %if1
            builder.append("br ");
            builder.append("i1 ");
            builder.append("%_");
            builder.append(getLastRegisterCount());
            builder.append(", label %if");
            builder.append(trueLabel);
            builder.append(", label %if");
            builder.append(falseLabel);
        }
        builder.append("\n");

        // if0:
        builder.append("if");
        builder.append(trueLabel);
        builder.append(":\n");
        // then statements
        ifStatement.thencase().accept(this);
        // br label %if2
        builder.append("br ");
        int thirdLabel = invokeIfRegisterCount();
        builder.append("%if");
        builder.append(thirdLabel);
        builder.append("\n");
        // if1:
        builder.append("if");
        builder.append(falseLabel);
        builder.append(":\n");
        // else statement
        ifStatement.elsecase().accept(this);
        // br label %if2
        builder.append("br ");
        builder.append("%if");
        builder.append(thirdLabel);
        builder.append("\n");
        // if2:
        builder.append("if");
        builder.append(thirdLabel);
        builder.append(":\n");
    }

    private void handleWhileCond(int whileLabel, int outLabel, WhileStatement whileStatement) {
        whileStatement.cond().accept(this);
        if (whileStatement.cond() instanceof TrueExpr) {
            // br label %if1
            builder.append("br label %if");
            builder.append(whileLabel);
        } else if (whileStatement.cond() instanceof FalseExpr) {
            // br label %if2
            builder.append("br label %if");
            builder.append(outLabel);
        } else {
            // compute cond
            whileStatement.cond().accept(this);
            // br i1 %_1, label %if1, label %if2
            builder.append("br i1 %_");
            builder.append(getLastRegisterCount());
            builder.append(", label %if");
            builder.append(whileLabel);
            builder.append(", label %if");
            builder.append(outLabel);
        }
        builder.append("\n");
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        // create while and out labels
        int whileLabel = invokeIfRegisterCount();
        int outLabel = invokeIfRegisterCount();
        // compute condition and branch over condition value
        handleWhileCond(whileLabel, outLabel, whileStatement);
        // if1: (== whileLabel)
        builder.append("if").append(whileLabel).append(":");
        builder.append("\n");
        // while statements
        whileStatement.body().accept(this);
        // re-check cond
        handleWhileCond(whileLabel, outLabel, whileStatement);
        // if2:  (== outLabel)
        builder.append("if").append(outLabel).append(":");
        builder.append("\n");
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        // handle ref-id or int-literal
        sysoutStatement.arg().accept(this);
        if (sysoutStatement.arg() instanceof IntegerLiteralExpr) {
            builder.append("call void (i32) @print_int(i32 ").append(sysoutStatement.arg()).append(")");
        }
        if (sysoutStatement.arg() instanceof MethodCallExpr) {
            builder.append("call void (i32) @print_int(i32 %_").append(getLastRegisterCount()).append(")");
        }
        if (sysoutStatement.arg() instanceof IdentifierExpr) {
            // Resolve Variable Type
            // Load that variable into register
            this.resolveVariable(((IdentifierExpr) sysoutStatement.arg()).id(), this.currentMethod);
            builder.append("call void (i32) @print_int(i32 %_").append(getLastRegisterCount()).append(")");
        }
        builder.append("\n");
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        // handle ref-id or int-literal
        assignStatement.rv().accept(this);
        if (assignStatement.rv() instanceof AddExpr || assignStatement.rv() instanceof SubtractExpr || assignStatement.rv() instanceof MultExpr || assignStatement.rv() instanceof ArrayAccessExpr) {
            // store i32 %_4, i32* %x
            builder.append("store i32 %_");
            builder.append(getLastRegisterCount());
            builder.append(", i32* %");
        }
        if (assignStatement.rv() instanceof IntegerLiteralExpr) {
            // store i32 4, i32* %x
            builder.append("store i32 ");
            builder.append(((IntegerLiteralExpr) assignStatement.rv()).num());
            builder.append(", i32* %");
        }
        if (assignStatement.rv() instanceof LtExpr) {
            // store i1 %_4, i1* %x
            builder.append("store i1 %_");
            builder.append(getLastRegisterCount());
            builder.append(", i1* %");
        }
        if (assignStatement.rv() instanceof TrueExpr) {
            // store i1 1, i1* %x
            builder.append("store i1 1, i1* %");
        }
        if (assignStatement.rv() instanceof FalseExpr) {
            // store i1 0, i1* %x
            builder.append("store i1 0, i1* %");
        }
        if (assignStatement.rv() instanceof NewObjectExpr) {
            // store i8* %_0, i8** %b
            builder.append("store i8* %_");
            builder.append(this.currentRegisterToAssign);
            builder.append(", i8** %_");
        }
        if (assignStatement.rv() instanceof NewIntArrayExpr) {
            // store i32* %_3, i32** %x
            builder.append("store i32* %_");
            builder.append(this.currentRegisterToAssign);
            builder.append(", i32** %");
        }
        builder.append(assignStatement.lv());
        builder.append("\n");
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
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
        int assignedVal;
        int andCond0 = invokeIfRegisterCount();
        int andCond1 = invokeIfRegisterCount();
        int andCond2 = invokeIfRegisterCount();
        int andCond3 = invokeIfRegisterCount();

        // compute e1
        e.e1().accept(this);

        // br label %andcond0
        builder.append("br label %if");
        builder.append(andCond1);
        builder.append("\n");
        // andcond0:
        builder.append("if");
        builder.append(andCond0);
        builder.append(":\n");

        // br i1 %_0, label %andcond1, label %andcond3
        if (e.e1() instanceof TrueExpr) {
            builder.append("br i1 1 label %if").append(andCond1).append(", label %if").append(andCond3);
        } else if (e.e1() instanceof FalseExpr) {
            builder.append("br i1 0 label %if").append(andCond1).append(", label %if").append(andCond3);
        } else if (e.e1() instanceof IdentifierExpr) {
            resolveVariable(((IdentifierExpr) e.e1()).id(), currentMethod);
            builder.append("br i1 %_").append(getLastRegisterCount()).append(" label %if").append(andCond1).append(", label %if").append(andCond3);
        } else {
            builder.append("br i1 %_").append(getLastRegisterCount()).append(" label %if").append(andCond1).append(", label %if").append(andCond3);
        }
        builder.append("\n");

        // andcond1:
        builder.append("if").append(andCond1).append(":\n");

        // compute e2
        e.e2().accept(this);
        if (e.e2() instanceof TrueExpr) {
            assignedVal = invokeRegisterCount("i1");
            builder.append("%_").append(getLastRegisterCount()).append("= i1 1\n");
        } else if (e.e2() instanceof FalseExpr) {
            assignedVal = invokeRegisterCount("i1");
            builder.append("%_").append(getLastRegisterCount()).append("= i1 0\n");
        } else if (e.e2() instanceof IdentifierExpr) {
            resolveVariable(((IdentifierExpr) e.e2()).id(), currentMethod);
            int lastVal = getLastRegisterCount();
            assignedVal = invokeRegisterCount("i1");
            builder.append("%_").append(getLastRegisterCount()).append("= ").append(lastVal);
        } else {
            int lastVal = getLastRegisterCount();
            assignedVal = invokeRegisterCount("i1");
            builder.append("%_").append(getLastRegisterCount()).append("= ").append(lastVal);
        }
        builder.append("\n");

        // br label %andcond2
        builder.append("br label %if").append(andCond2).append("\n");

        // andcond2:
        builder.append("if").append(andCond2).append(":\n");

        // br label %andcond3
        builder.append("br label %if").append(andCond3).append("\n");

        // andcond3:
        builder.append("if").append(andCond3).append(":\n");

        invokeRegisterCount("i1");
        builder.append("%_").append(getLastRegisterCount()).append("= phi i1 [").append(assignedVal).append(",%if").append(andCond2).append("], [0, %").append(andCond0);
        builder.append("\n");
    }

    @Override
    public void visit(LtExpr e) {
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
        int arrayRegister = invokeRegisterCount("i32*");
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
        int indexResultRegister = invokeRegisterCount("i1");
        int labelIllegalIndex = invokeIfRegisterCount();
        int labelLegalIndex = invokeIfRegisterCount();
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
            indexValue = "i32 %_" + getLastRegisterCount();
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
        int indexRegister = invokeRegisterCount("i32");
        builder.append("%_");
        builder.append(indexRegister);
        builder.append(" = add ");
        builder.append(indexValue);
        builder.append(", 1\n");
        // %_10 = getelementptr i32, i32* %_4, i32 %_9
        int valueRegister = invokeRegisterCount("i32*");
        builder.append("%_");
        builder.append(valueRegister);
        builder.append(" = getelementptr i32, i32* %_");
        builder.append(arrayRegister);
        builder.append(", %_");
        builder.append(indexRegister);
        builder.append("\n");
        // %_11 = load i32, i32* %_10
        int loadRegister = invokeRegisterCount("i32");
        builder.append("%_");
        builder.append(loadRegister);
        builder.append(" = load i32, i32* %_");
        builder.append(valueRegister);
        builder.append("\n");
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        // TODO EX2 - %_reg = get element name of array in 0 index
        int lengthRegister = invokeRegisterCount("i32");
        String lengthValue;
        if (e.arrayExpr() instanceof NewIntArrayExpr) {
            AstNode lengthAst = ((NewIntArrayExpr) e.arrayExpr()).lengthExpr();
            if (lengthAst instanceof IntegerLiteralExpr) {
                lengthValue = "i32 " + ((IntegerLiteralExpr) lengthAst).num();
            } else {
                lengthAst.accept(this);
                lengthValue = "i32 %_" + getLastRegisterCount();
            }
            builder.append("%_");
            builder.append(lengthRegister);
            builder.append(" = ");
            builder.append(lengthValue);
            builder.append("\n");
        } else if (e.arrayExpr() instanceof IdentifierExpr) {
            // %_4 = load i32*, i32** %x
            int arrayRegister = invokeRegisterCount("i32*");
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
        String classId = "";
        // resolve class of owner (this, new, ref-id)  -> class
        if (ownerExp instanceof ThisExpr) {
            classId = this.currentClass;
        } else if (ownerExp instanceof NewObjectExpr) {
            classId = ((NewObjectExpr) ownerExp).classId();
        } else if (ownerExp instanceof IdentifierExpr) {
            classId = resolveVariable(((IdentifierExpr) ownerExp).id(), e.methodId());
        }
        // Assume this was done in accept:
        // %_6 = load i8*, i8** %b
        e.ownerExpr().accept(this);
        // %_7 = bitcast i8* %_6 to i8***
        String loadedRegister = this.currentRegisterToAssign;
        int bitcastRegister = invokeRegisterCount("i8*");
        builder.append("%_");
        builder.append(bitcastRegister);
        builder.append(" = bitcast i8* %_");
        builder.append(loadedRegister);
        builder.append(" to i8***\n");
        //	%_8 = load i8**, i8*** %_7
        int vtableRegister = invokeRegisterCount("i8**");
        builder.append("%_");
        builder.append(vtableRegister);
        builder.append(" = load i8**, i8*** %_");
        builder.append(bitcastRegister);
        builder.append("\n");
        // %_9 = getelementptr i8*, i8** %_8, i32 0
        int methodRegister = invokeRegisterCount("i8*");
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
        int actualFunctionPointerRegister = invokeRegisterCount("i8*");
        builder.append("%_");
        builder.append(actualFunctionPointerRegister);
        builder.append(" = load i8*, i8** %_");
        builder.append(methodRegister);
        builder.append("\n");
        //	%_11 = bitcast i8* %_10 to i32 (i8*, i32)*
        String returnTypeValue = methodeInfo.getRet();
        String args = methodeInfo.getArgs();
        bitcastRegister = invokeRegisterCount("i32 ()");
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
            if (arg instanceof IntegerLiteralExpr) {
                arguments.append("i32 ");
                arguments.append(((IntegerLiteralExpr) arg).num());
            } else if (arg instanceof TrueExpr) {
                arguments.append("i1 1");
            } else if (arg instanceof FalseExpr) {
                arguments.append("i1 0");
            } else {
                arg.accept(this);
                arguments.append(this.getLastRegisterType());
                arguments.append(" ");
                arguments.append("%_");
                arguments.append(getLastRegisterCount());
            }
            delim = ", ";
        }
        // %_12 = call i32 %_11(i8* %_6, i32 1)
        int callRegister = invokeRegisterCount("i32");
        builder.append("%_");
        builder.append(callRegister);
        builder.append(" = call ");
        builder.append(returnTypeValue);
        builder.append(" %_");
        builder.append(bitcastRegister);
        builder.append("(i8* %_");
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
    }

    public void visit(ThisExpr e) {
    }

    @Override
    public void visit(NewIntArrayExpr e) {
        // TODO EX2 - according to example in class
        int lengthResultRegister = invokeRegisterCount("i1");
        int labelIllegalLength = invokeIfRegisterCount();
        int labelLegalLength = invokeIfRegisterCount();
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
        } else if (e.lengthExpr() instanceof IdentifierExpr) {
            resolveVariable(((IdentifierExpr) e.lengthExpr()).id(), currentMethod);
            lengthValue = "i32 %_" + getLastRegisterCount();
        } else {
            lengthValue = "i32 %_" + getLastRegisterCount();
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
        int sizeOfArrayRegister = invokeRegisterCount("i32");
        builder.append("%_");
        builder.append(sizeOfArrayRegister);
        builder.append(" = add i32 ");
        builder.append(lengthValue);
        builder.append(", 1\n");
        // 	%_2 = call i8* @calloc(i32 4, i32 %_1)
        int callocRegister = invokeRegisterCount("i8*");
        builder.append("%_");
        builder.append(callocRegister);
        builder.append(" = call i8* @calloc(i32 4, i32 ");
        builder.append(lengthValue);
        builder.append(")\n");
        // %_3 = bitcast i8* %_2 to i32*
        int bitcastRegister = invokeRegisterCount("i8*");
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
        int objectReg = invokeRegisterCount("i8*");
        int vTable = invokeRegisterCount("i8**");
        int vTableFirstElement = invokeRegisterCount("i8*");
        ObjectStruct objectStruct = VtableCreator.getObjectStructMap().get(e.classId());
        int sizeOfObject = objectStruct.getSizeInBytes();
        int methodsCount = objectStruct.getMethodeInfoMap().size();
        // %_0 = call i8* @calloc(i32 1, i32 12)
        builder.append("%_");
        builder.append(objectReg);
        builder.append(" = call i8* @calloc(i32 1, i32 ");
        builder.append(sizeOfObject);
        builder.append(")\n");
        // %_1 = bitcast i8* %_0 to i8***
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
        // %_8 = sub i1 1, %_7
        e.e().accept(this);
        if (e.e() instanceof TrueExpr || e.e() instanceof FalseExpr) {
            builder.append("%_");
            builder.append(invokeRegisterCount("i1"));
            builder.append(" = sub i1 1, ");
            builder.append(e.e() instanceof TrueExpr ? "1" : "0");
            builder.append("\n");
        } else {
            builder.append("%_");
            builder.append(invokeRegisterCount("i1"));
            builder.append(" = sub i1 1, %_");
            builder.append(getLastRegisterCount());
            builder.append("\n");
        }
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
