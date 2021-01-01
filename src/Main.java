import ast.*;
import symbolTable.Symbol;
import symbolTable.FlowUtils;
import symbolTable.SymbolTableUtils;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        try {
            var inputMethod = args[0];
            var action = args[1];
            var filename = args[args.length - 2];
            var outfilename = args[args.length - 1];

            Program prog;

            if (inputMethod.equals("parse")) {
                FileReader fileReader = new FileReader(new File(filename));
                Parser p = new Parser(new Lexer(fileReader));
                prog = (Program) p.parse().value;
            } else if (inputMethod.equals("unmarshal")) {
                AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                prog = xmlSerializer.deserialize(new File(filename));
            } else {
                throw new UnsupportedOperationException("unknown input method " + inputMethod);
            }
            var outFile = new PrintWriter(outfilename);
            try {
                boolean validToContinue = true;
                SymbolTableUtils.buildSymbolTables(prog);
                if (SymbolTableUtils.isERROR()) {
//                    System.out.println(SymbolTableUtils.getERRORReasons());
                    outFile.write("ERROR\n");
                    validToContinue = false;
                }

                if (action.equals("marshal")) {
                    AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                    xmlSerializer.serialize(prog, outfilename);
                } else if (action.equals("print")) {
                    AstPrintVisitor astPrinter = new AstPrintVisitor();
                    astPrinter.visit(prog);
                    outFile.write(astPrinter.getString());

                } else if (action.equals("semantic")) {
                    if (validToContinue) {
                        AstTypesVisitor astTypeVisitor = new AstTypesVisitor();
                        astTypeVisitor.visit(prog);
                        if (astTypeVisitor.isError()) {
//                            System.out.println(astTypeVisitor.getErrorMsg());
                            outFile.write("ERROR\n");
                        } else {
                            AstInitializedVisitor astInitVisitor = new AstInitializedVisitor();
                            astInitVisitor.visit(prog);
                            if (astInitVisitor.isError()) {
//                                System.out.println(astInitVisitor.getErrorMsg());
                                outFile.write("ERROR\n");
                            }
                            else{
                                outFile.write("OK\n");
                            }
                        }
                    }
                } else if (action.equals("compile")) {
                    // VtableCreator - create vtables + Class->data-structure(vtable-method/field -> offset) and probably more...
                    VtableCreator v = new VtableCreator();
                    String llvmVtables = v.createVtableAndObjectsStruct();
                    // LLVM Print Visitor
                    AstLlvmPrintVisitor astLlvmPrintVisitor = new AstLlvmPrintVisitor();
                    astLlvmPrintVisitor.visit(prog);
                    // Concat v tables and visitor's result
                    String llvmOutput = astLlvmPrintVisitor.getString();
                    outFile.write(llvmVtables + "\n" + llvmOutput);
                } else if (action.equals("rename")) {
                    var type = args[2];
                    var originalName = args[3];
                    var originalLine = args[4];
                    var newName = args[5];

                    boolean isMethod;
                    if (type.equals("var")) {
                        isMethod = false;
                    } else if (type.equals("method")) {
                        isMethod = true;
                    } else {
                        throw new IllegalArgumentException("unknown rename type " + type);
                    }

                    try {
                        Symbol symbol = FlowUtils.findSymbolToRename(Integer.parseInt(originalLine), originalName, isMethod);
                        FlowUtils.rename(symbol.getProperties(), newName);
                        AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                        xmlSerializer.serialize(prog, outfilename);
                    } catch (UnsupportedOperationException e) {
                        throw new UnsupportedOperationException(e.getMessage());
                    } catch (Exception e) {
                        // TODO error handling
                        throw new UnsupportedOperationException(e.getMessage());
                    }


                } else {
                    throw new IllegalArgumentException("unknown command line action " + action);
                }
            } finally {
                outFile.flush();
                outFile.close();
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error reading file: " + e);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("General error: " + e);
            e.printStackTrace();
        }
    }
}
