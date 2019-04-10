package vtables_example.typechecker;

import vtables_example.syntax.*;

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
    
    @Test
    public void testWellTypedPrintStmt() {
        assertWellTyped(mkProgram(new PrintStmt(new IntExp(0))));
    }
} // TypecheckerClassTest
