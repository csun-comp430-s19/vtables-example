package vtables_example.codegen;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;

import vtables_example.syntax.*;

public class MIPSCodeGenerator {
    // ---BEGIN CONSTANTS---
    public static final Variable RA_VARIABLE = new Variable("$ra");
    public static final Variable THIS_VARIABLE = new Variable("$this");
    // ---END CONSTANTS---
    
    // ---BEGIN INSTANCE VARIABLES---
    private final Map<ClassName, ClassDefinition> classes;
    private final List<MIPSEntry> entries;
    private final VariableTable variables;
    // ---END INSTANCE VARIABLES
    
    public MIPSCodeGenerator(final Map<ClassName, ClassDefinition> classes) {
        this.classes = classes;
        entries = new ArrayList<MIPSEntry>();
        variables = new VariableTable();
    }

    public void add(final MIPSEntry i) {
        entries.add(i);
    } // add

    // pushes the contents of this register onto the stack
    public void push(final MIPSRegister register) {
        // addi $sp, $sp, -4
        // sw register, 0($sp)
        final MIPSRegister sp = MIPSRegister.SP;
        add(new Addi(sp, sp, -4));
        add(new Sw(register, 0, sp));
    } // push

    public void pop(final MIPSRegister register) {
        // lw register, 0($sp)
        // addi $sp, $sp, 4
        final MIPSRegister sp = MIPSRegister.SP;
        add(new Lw(register, 0, sp));
        add(new Addi(sp, sp, 4));
    } // pop

    // each variable can hold either an int (4 bytes), or a reference to a class (4 bytes)
    public int sizeofClass(final ClassName className) {
        final ClassDefinition def = classes.get(className);
        final int parentSize = (def.extendsName == null) ? 0 : sizeofClass(def.extendsName);
        return parentSize + def.instanceVariables.length * 4;
    }
            
    public int fieldOffset(final ClassName className,
                           final Variable variable) {
        final VarDec[] vars = classes.get(className).instanceVariables;
        int offset = 0;
        for (final VarDec dec : vars) {
            if (dec.variable.equals(variable)) {
                return offset;
            }
            offset += 4;
        }

        assert(false);
        return 0;
    }

    public void compileVariableAccess(final Variable variable, final MIPSRegister resultIn) {
        final int offset = variables.variableOffset(variable);
        add(new Lw(resultIn, offset, MIPSRegister.SP));
    }

    public void compileLhsAsExpression(final Lhs lhs, final MIPSRegister resultIn) {
        if (lhs instanceof VariableLhs) {
            compileVariableAccess(((VariableLhs)lhs).variable, resultIn);
        } else if (lhs instanceof FieldAccessLhs) {
            final FieldAccessLhs asField = (FieldAccessLhs)lhs;
            compileLhsAsExpression(asField.lhs, resultIn);
            final int offset = fieldOffset(asField.getLhsClass(), asField.field);
            add(new Lw(resultIn, offset, resultIn));
        } else if (lhs instanceof ThisLhs) {
            compileVariableAccess(THIS_VARIABLE, resultIn);
        } else {
            assert(false);
        }
    }
    
    public void compileExpression(final Exp exp, final MIPSRegister resultIn) {
        if (exp instanceof IntExp) {
            final int value = ((IntExp)exp).value;
            add(new Li(resultIn, value));
        } else {
            compileLhsAsExpression(exp, resultIn);
        }
    }

    public static MIPSLabel constructorLabel(final ClassName forClass) {
        return new MIPSLabel("new_" + forClass.name, 0);
    }

    public void compileNewStmt(final NewStmt stmt) {
        assert(false);
    }

    public void compileMethodCallStmt(final MethodCallStmt stmt) {
        assert(false);
    }

    public void printA0() {
        add(new Li(MIPSRegister.V0, 1));
        add(new Syscall());

        // print a newline
        add(new Li(MIPSRegister.V0, 4));
        add(new La(MIPSRegister.A0, "newline"));
        add(new Syscall());
    }

    public void compilePrintStmt(final PrintStmt stmt) {
        compileExpression(stmt.exp, MIPSRegister.A0);
        printA0();
    }

    public void compileReturnStmt(final ReturnStmt stmt) {
        assert(false);
    }

    public void compileAssignStmt(final AssignStmt stmt) {
        assert(false);
    }

    public void compileSuperStmt(final SuperStmt stmt) {
        assert(false);
    }

    public void compileSequenceStmt(final SequenceStmt stmt) {
        compileStatement(stmt.first);
        compileStatement(stmt.second);
    }
    
    public void compileStatement(final Stmt stmt) {
        if (stmt instanceof NewStmt) {
            compileNewStmt((NewStmt)stmt);
        } else if (stmt instanceof MethodCallStmt) {
            compileMethodCallStmt((MethodCallStmt)stmt);
        } else if (stmt instanceof PrintStmt) {
            compilePrintStmt((PrintStmt)stmt);
        } else if (stmt instanceof ReturnStmt) {
            compileReturnStmt((ReturnStmt)stmt);
        } else if (stmt instanceof AssignStmt) {
            compileAssignStmt((AssignStmt)stmt);
        } else if (stmt instanceof SuperStmt) {
            compileSuperStmt((SuperStmt)stmt);
        } else if (stmt instanceof SequenceStmt) {
            compileSequenceStmt((SequenceStmt)stmt);
        } else {
            assert(false);
        }
    }

    private void mainEnd() {
        // exit
        add(new Li(MIPSRegister.V0, 10));
        add(new Syscall());
    } // mainEnd
    
    public void writeCompleteFile(final File file) throws IOException {
        final PrintWriter output =
            new PrintWriter(new BufferedWriter(new FileWriter(file)));
        mainEnd();
        try {
            output.println(".data");
            output.println("newline:");
            output.println(MIPSInstruction.INDENT + ".asciiz \"\\n\"");
            output.println(".text");
            output.println("main:");
            for (final MIPSEntry entry : entries) {
                output.println(entry.toString());
            }
        } finally {
            output.close();
        }
    } // writeCompleteFile
}
