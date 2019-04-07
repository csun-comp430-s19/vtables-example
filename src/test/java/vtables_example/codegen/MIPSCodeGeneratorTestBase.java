package vtables_example.codegen;

import vtables_example.syntax.*;

import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.rules.TestName;

public abstract class MIPSCodeGeneratorTestBase<A> {
    @Rule public TestName name = new TestName();

    public int parseOutput(final String[] spimOutput) {
        assert(spimOutput.length == 2);
        return Integer.parseInt(spimOutput[1]);
    } // parseOutput

    public void assertResult(final int expected,
                             final A compileMe,
                             final Map<ClassName, ClassDefinition> classes) throws IOException {
        boolean wantToSaveFile = true; // for debugging

        final File file = File.createTempFile(name.getMethodName(),
                                              ".asm",
                                              new File("testPrograms"));
        boolean testPassed = false;
        try {
            final MIPSCodeGenerator gen = new MIPSCodeGenerator(classes);
            doCompile(gen, compileMe);
            gen.writeCompleteFile(file);
            final String[] output = SPIMRunner.runFile(file);
            final int received = parseOutput(output);
            if (wantToSaveFile) {
                assertEquals("Expected: " + expected + " Received: " + received + " File: " +
                             file.getAbsolutePath(),
                             expected,
                             received);
            } else {
                assertEquals(expected, received);
            }
            testPassed = true;
        } finally {
            if (!wantToSaveFile || testPassed) {
                file.delete();
            }
        }
    }

    public void assertResult(final int expected,
                             final A compileMe) throws IOException {
        assertResult(expected,
                     compileMe,
                     new HashMap<ClassName, ClassDefinition>());
    }
    
    // ---BEGIN ABSTRACT METHODS---
    protected abstract void doCompile(MIPSCodeGenerator gen, A input);
    // ---END ABSTRACT METHODS---
} // MIPSCodeGeneratorTestBase
