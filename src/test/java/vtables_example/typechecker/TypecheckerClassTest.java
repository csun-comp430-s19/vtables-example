package vtables_example.typechecker;

import vtables_example.syntax.*;

import static vtables_example.typechecker.TypecheckerExpTest.EMPTY_CLASS_NAME;
import static vtables_example.typechecker.TypecheckerExpTest.EMPTY_CLASS;
import static vtables_example.typechecker.TypecheckerExpTest.FIELD_CLASS_NAME;
import static vtables_example.typechecker.TypecheckerExpTest.FIELD_CLASS;
import static vtables_example.typechecker.TypecheckerExpTest.mkChecker;
import static vtables_example.typechecker.TypecheckerExpTest.initialEnv;

import static org.junit.Assert.fail;
import org.junit.Test;

public class TypecheckerClassTest {
    // ---BEGIN CONSTANTS---
    public static final ClassName BASE_CLASS_NAME = new ClassName("Base");
    public static final ClassName SUB_CLASS_NAME = new ClassName("Sub");
    // ---END CONSTANTS---

    // Cannot be constants, as the typechecker will fill in types for certain parts.
    // It expects that these types have not been filled in before.

    // class Base {
    //   int b;
    //   init(int b) {
    //     this.b = b;
    //   }
    // }
    public static ClassDefinition baseClass() {
        return new ClassDefinition(BASE_CLASS_NAME,
                                   null,
                                   new VarDec[] {
                                       new VarDec(new IntType(), new Variable("b"))
                                   },
                                   new Constructor(new VarDec[] {
                                           new VarDec(new IntType(), new Variable("b"))
                                       },
                                       new AssignStmt(new FieldAccessLhs(new ThisLhs(),
                                                                         new Variable("b")),
                                                      new LhsExp(new VariableLhs(new Variable("b"))))),
                                   new MethodDefinition[0]);
    }

    // class Sub extends Base {
    //   int s;
    //   init(int x, int y) {
    //     super(x);
    //     this.s = y;
    //   }
    // }
    public static ClassDefinition subClass() {
        return new ClassDefinition(SUB_CLASS_NAME,
                                   BASE_CLASS_NAME,
                                   new VarDec[] {
                                       new VarDec(new IntType(), new Variable("s"))
                                   },
                                   new Constructor(new VarDec[] {
                                           new VarDec(new IntType(), new Variable("x")),
                                           new VarDec(new IntType(), new Variable("y"))
                                       },
                                       stmts(new SuperStmt(new Exp[] { new LhsExp(new VariableLhs(new Variable("x"))) }),
                                             new AssignStmt(new FieldAccessLhs(new ThisLhs(),
                                                                               new Variable("s")),
                                                            new LhsExp(new VariableLhs(new Variable("y")))))),
                                   new MethodDefinition[0]);
    }
    
    public void assertWellTyped(final Program program) {
        try {
            Typechecker.typecheckProgram(program);
        } catch (final TypeErrorException e) {
            fail("expected well-typed; " + e.getMessage());
        }
    }

    public void assertIllTyped(final Program program) {
        try {
            Typechecker.typecheckProgram(program);
            fail("expected ill-typed");
        } catch (final TypeErrorException e) {
        }
    }

    public static Program mkProgram(final Stmt entryPoint,
                                    final ClassDefinition... classes) {
        return new Program(classes, entryPoint);
    }

    public static Stmt stmts(final Stmt... stmts) {
        assert(stmts.length > 0);
        Stmt result = stmts[stmts.length - 1];

        for (int index = stmts.length - 2; index >= 0; index--) {
            result = new SequenceStmt(stmts[index], result);
        }
        return result;
    }

    @Test
    public void testWellTypedNewStmt() {
        final NewStmt stmt =
            new NewStmt(new VarDec(new ClassType(EMPTY_CLASS_NAME),
                                   new Variable("x")),
                        EMPTY_CLASS_NAME,
                        new Exp[0]);
        assertWellTyped(mkProgram(stmt, EMPTY_CLASS));
    }

    @Test
    public void testIllTypedNewStmtNoSuchClass() {
        final NewStmt stmt =
            new NewStmt(new VarDec(new ClassType(EMPTY_CLASS_NAME),
                                   new Variable("x")),
                        EMPTY_CLASS_NAME,
                        new Exp[] { new IntExp(0) });
        assertIllTyped(mkProgram(stmt));
    }

    @Test
    public void testIllTypedNewStmtWrongParams() {
        final NewStmt stmt =
            new NewStmt(new VarDec(new ClassType(EMPTY_CLASS_NAME),
                                   new Variable("x")),
                        EMPTY_CLASS_NAME,
                        new Exp[] { new IntExp(0) });
        assertIllTyped(mkProgram(stmt, EMPTY_CLASS));
    }

    @Test
    public void testIllTypedNewStmtAssignToIncompatible() {
        final NewStmt stmt =
            new NewStmt(new VarDec(new ClassType(FIELD_CLASS_NAME),
                                   new Variable("x")),
                        EMPTY_CLASS_NAME,
                        new Exp[] { new IntExp(0) });
        assertIllTyped(mkProgram(stmt, EMPTY_CLASS, FIELD_CLASS));
    }

    @Test
    public void testWellTypedNewStmtSubtyping() {
        final NewStmt stmt =
            new NewStmt(new VarDec(new ClassType(BASE_CLASS_NAME),
                                   new Variable("x")),
                        SUB_CLASS_NAME,
                        new Exp[] { new IntExp(0), new IntExp(1) });
        assertWellTyped(mkProgram(stmt, baseClass(), subClass()));
    }

    @Test
    public void testIllTypedNewStmtSubtyping() {
        final NewStmt stmt =
            new NewStmt(new VarDec(new ClassType(SUB_CLASS_NAME),
                                   new Variable("x")),
                        BASE_CLASS_NAME,
                        new Exp[] { new IntExp(0), new IntExp(1) });
        assertIllTyped(mkProgram(stmt, baseClass(), subClass()));
    }
    
    @Test
    public void testWellTypedPrintStmt() {
        assertWellTyped(mkProgram(new PrintStmt(new IntExp(0))));
    }
} // TypecheckerClassTest
