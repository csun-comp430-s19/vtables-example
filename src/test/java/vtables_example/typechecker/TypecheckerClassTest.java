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
    public void testWellTypedPrintStmt() {
        assertWellTyped(mkProgram(new PrintStmt(new IntExp(0))));
    }
} // TypecheckerClassTest
