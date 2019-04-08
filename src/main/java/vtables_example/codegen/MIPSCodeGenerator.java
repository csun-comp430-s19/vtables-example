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

    // returns -1 if this class doesn't contain this field
    private int selfFieldOffset(final ClassDefinition def, final Variable field) {
        int offset = 0;
        for (final VarDec dec : def.instanceVariables) {
            if (dec.variable.equals(field)) {
                return offset;
            }
            offset += 4;
        }
        return -1;
    }
    
    private FieldOffsetResult fieldOffsetHelper(final ClassName className,
                                                final Variable variable) {
        final ClassDefinition def = classes.get(className);
        if (def.extendsName != null) {
            // see if my parent has it
            final FieldOffsetResult fromParent = fieldOffsetHelper(def.extendsName, variable);
            if (fromParent.found) {
                return fromParent;
            } else {
                final int fromSelf = selfFieldOffset(def, variable);
                if (fromSelf == -1) {
                    // I don't have it and neither did my parent
                    return new FieldOffsetResult(false, 0);
                } else {
                    // I do have it
                    return new FieldOffsetResult(true, sizeofClass(def.extendsName) + fromSelf);
                }
            }
        } else {
            // no parent class
            final int fromSelf = selfFieldOffset(def, variable);
            if (fromSelf == -1) {
                // I don't have it
                return new FieldOffsetResult(false, 0);
            } else {
                // I do have it
                return new FieldOffsetResult(true, fromSelf);
            }
        }
    }
    
    public int fieldOffset(final ClassName className,
                           final Variable variable) {
        final FieldOffsetResult res = fieldOffsetHelper(className, variable);
        assert(res.found);
        return res.offset;
    }

    public void putLhsAddressIntoRegister(final MIPSRegister destination,
                                          final Lhs lhs) {
        if (lhs instanceof VariableLhs) {
            final int offset = variables.variableOffset(((VariableLhs)lhs).variable);
            add(new Addi(destination, MIPSRegister.SP, offset));
        } else if (lhs instanceof FieldAccessLhs) {
            final FieldAccessLhs asField = (FieldAccessLhs)lhs;
            final int offsetFromField = fieldOffset(asField.getLhsClass(),
                                                    asField.field);
            putLhsAddressIntoRegister(destination, asField.lhs);
            add(new Lw(destination, 0, destination));
            add(new Addi(destination, destination, offsetFromField));
        } else if (lhs instanceof ThisLhs) {
            // direct assignment to this is disallowed (typechecker makes sure of this)
            final int offset = variables.variableOffset(THIS_VARIABLE);
            add(new Addi(destination, MIPSRegister.SP, offset));
        } else {
            assert(false);
        }
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
            add(new MIPSComment("lhs offset"));
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
        } else if (exp instanceof LhsExp) {
            compileLhsAsExpression(((LhsExp)exp).lhs, resultIn);
        }
    }

    public static MIPSLabel constructorLabel(final ClassName forClass) {
        return new MIPSLabel("new_" + forClass.name, -1);
    }

    public static MIPSLabel nonVirtualMethodLabel(final ClassName forClass,
                                                  final MethodName forMethod) {
        return new MIPSLabel(forClass.name + "_" + forMethod.name, 0);
    }
    
    public void compileParams(final Exp[] params, final MIPSRegister temp) {
        final MIPSRegister sp = MIPSRegister.SP;
        int offset = -4;
        for (final Exp param : params) {
            compileExpression(param, temp);
            add(new Sw(temp, offset, sp));
            offset -= 4;
        }
        if (params.length > 0) {
            add(new Addi(sp, sp, -(params.length * 4)));
        }
    }

    private void doReturn() {
        // the stack looks like the following at this point:
        //
        // before_call
        // this
        // argument1
        // argument2
        // ...
        // argumentN
        // return_address
        // local_variable_1
        // local_variable_2
        // ...
        // local_variable_N
        //
        //
        // we need to adjust it so it looks like the following:
        //
        // before_call
        //

        // Unlike the previous code generator, because the return value is
        // not on the stack, this is very straightforward
        
        // save return value in a register
        final int raOffset = variables.variableOffset(RA_VARIABLE);
        final MIPSRegister sp = MIPSRegister.SP;
        final MIPSRegister ra = MIPSRegister.RA;
        add(new Lw(ra, raOffset, sp));

        // move stack pointer to final position
        final int sizeOfAllVariables = variables.totalSizeOfAllVariables();
        add(new Addi(sp, sp, sizeOfAllVariables));

        // do the return
        add(new Jr(ra));
    }

    private void compileFunction(final MIPSLabel label,
                                 final ClassName forClass,
                                 final VarDec[] params,
                                 final Stmt body) {
        add(label);
        callEntrySetup(forClass, params);
        compileStatement(forClass, body);
        callExitSetup();
    }
    
    private void callEntrySetup(final ClassName forClass,
                                final VarDec[] params) {
        assert(variables.isEmpty());

        // this is always first
        final ClassType classType = new ClassType(forClass);
        variables.pushVariable(THIS_VARIABLE,
                               classType,
                               4);

        // extract parameters
        for (final VarDec param : params) {
            variables.pushVariable(param.variable,
                                   param.type,
                                   4);
        }

        // return address follows parameters
        push(MIPSRegister.RA);
        variables.pushVariable(RA_VARIABLE,
                               classType, // meaningless
                               4);
    }

    private void callExitSetup() {
        doReturn();
        variables.clear();
    }

    public void compileProgram(final Program program) {
        add(new MIPSLabel("main", -1));
        compileStatement(null, program.entryPoint);
        variables.clear();
        mainEnd();
        for (final ClassDefinition def : program.classes) {
            compileClass(def);
        }
    }
    
    public void compileClass(final ClassDefinition def) {
        compileConstructor(def.myName, def.constructor);
        for (final MethodDefinition method : def.methods) {
            compileMethod(def.myName, method);
        }
    }
        
    public void compileMethod(final ClassName forClass,
                              final MethodDefinition method) {
        assert(!method.isVirtual); // TODO
        compileFunction(nonVirtualMethodLabel(forClass, method.name),
                        forClass,
                        method.params,
                        method.body);
    }
        
    public void compileConstructor(final ClassName forClass,
                                   final Constructor constructor) {
        compileFunction(constructorLabel(forClass),
                        forClass,
                        constructor.params,
                        constructor.body);
    }

    public void compileNewStmt(final NewStmt stmt) {
        // allocate space for the class on the heap
        final int size = sizeofClass(stmt.name);
        add(new Li(MIPSRegister.A0, size));
        add(new Li(MIPSRegister.V0, 9));
        add(new Syscall());

        // put this address into it's place on the stack
        push(MIPSRegister.V0);
        variables.pushVariable(stmt.vardec.variable,
                               stmt.vardec.type,
                               4);

        // first parameter is always this
        final VariableTableResetPoint resetPoint = variables.makeResetPoint();
        push(MIPSRegister.V0);
        variables.pushDummy(4);

        // evaluate the remaining parameters, putting them on the stack
        compileParams(stmt.params, MIPSRegister.T0);

        // call the constructor
        add(new Jal(constructorLabel(stmt.name)));
        variables.resetTo(resetPoint);
    }

    public FindMethodResult findMethod(final ClassName onClass, final MethodName methodName) {
        final ClassDefinition def = classes.get(onClass);
        for (final MethodDefinition method : def.methods) {
            if (method.name.equals(methodName)) {
                return new FindMethodResult(method.isVirtual, onClass);
            }
        }

        // if I don't have it, my parent should (typechecker ensures this)
        return findMethod(def.extendsName, methodName);
    }
    
    public void compileMethodCallStmt(final MethodCallStmt stmt) {
        final MIPSRegister t0 = MIPSRegister.T0;
        
        // this is always first
        final VariableTableResetPoint resetPoint = variables.makeResetPoint();
        compileExpression(stmt.exp, t0);
        push(t0);
        variables.pushDummy(4);
        
        // put parameters on the stack
        compileParams(stmt.params, t0);

        final FindMethodResult find = findMethod(stmt.getOnClass(), stmt.name);
        final MIPSLabel jumpTo;
        if (find.isVirtual) {
            // virtual calls need the vtable
            assert(false); // TODO
            jumpTo = null;
        } else {
            // non-virtual calls behave as normal function calls
            jumpTo = nonVirtualMethodLabel(stmt.getOnClass(), stmt.name);
        }

        add(new Jal(jumpTo));

        // make space for the variable
        variables.resetTo(resetPoint);
        push(MIPSRegister.V0);
        variables.pushVariable(stmt.vardec.variable,
                               stmt.vardec.type,
                               4);
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
        // Typechecker ensures this is the last bit in a method.
        // Methods now only have exactly one return, right at the end.
        // As such, we just put the return value in the right register, and
        // allow compileFunction to do the proper return setup
        if (stmt.exp != null) {
            compileExpression(stmt.exp, MIPSRegister.V0);
        } else {
            // not strictly necessary
            add(new Li(MIPSRegister.V0, 0));
        }
    }

    public void compileAssignStmt(final AssignStmt stmt) {
        final MIPSRegister t0 = MIPSRegister.T0;
        final MIPSRegister t1 = MIPSRegister.T1;
        compileExpression(stmt.exp, t0);
        putLhsAddressIntoRegister(t1, stmt.lhs);
        add(new Sw(t0, 0, t1));
    }

    public void compileSuperStmt(final ClassName forClass, final SuperStmt stmt) {
        assert(forClass != null); // typechecker checks this
        final ClassDefinition def = classes.get(forClass);
        assert(def.extendsName != null); // typechecker checks this
        
        // this is always first
        final VariableTableResetPoint resetPoint = variables.makeResetPoint();
        final MIPSRegister t0 = MIPSRegister.T0;
        compileVariableAccess(THIS_VARIABLE, t0);
        push(t0);
        variables.pushDummy(4);
        
        // put parameters on the stack
        compileParams(stmt.params, MIPSRegister.T0);

        // call superclass' constructor
        add(new Jal(constructorLabel(def.extendsName)));
        variables.resetTo(resetPoint);
    }

    public void compileSequenceStmt(final ClassName forClass, final SequenceStmt stmt) {
        compileStatement(forClass, stmt.first);
        compileStatement(forClass, stmt.second);
    }

    public void compileEmptyStmt(final EmptyStmt stmt) {
        // do nothing
    }
    
    public void compileStatement(final ClassName forClass, final Stmt stmt) {
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
            compileSuperStmt(forClass, (SuperStmt)stmt);
        } else if (stmt instanceof SequenceStmt) {
            compileSequenceStmt(forClass, (SequenceStmt)stmt);
        } else if (stmt instanceof EmptyStmt) {
            compileEmptyStmt((EmptyStmt)stmt);
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
        try {
            output.println(".data");
            output.println("newline:");
            output.println(MIPSInstruction.INDENT + ".asciiz \"\\n\"");
            output.println(".text");
            for (final MIPSEntry entry : entries) {
                output.println(entry.toString());
            }
        } finally {
            output.close();
        }
    } // writeCompleteFile
}
