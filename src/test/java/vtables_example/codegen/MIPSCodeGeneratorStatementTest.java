package vtables_example.codegen;

import vtables_example.syntax.*;

import java.io.IOException;

import org.junit.Test;

public class MIPSCodeGeneratorStatementTest extends MIPSCodeGeneratorTestBase<Stmt> {
    protected void doCompile(final MIPSCodeGenerator gen, final Stmt stmt) {
        gen.compileStatement(stmt);
    }

    @Test
    public void testPrintInt() throws IOException {
        assertResult(1, new PrintStmt(new IntExp(1)));
    }
}
