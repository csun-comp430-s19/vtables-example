package vtables_example.codegen;

import vtables_example.syntax.*;

import java.io.IOException;

import java.util.Map;
import java.util.HashMap;

import org.junit.Test;

public class MIPSCodeGeneratorClassTest extends MIPSCodeGeneratorTestBase<Program> {
    protected void doCompile(final MIPSCodeGenerator gen, final Program program) {
        gen.compileProgram(program);
    }

    public static Map<ClassName, ClassDefinition> makeMapping(final ClassDefinition[] classes) {
        final Map<ClassName, ClassDefinition> result = new HashMap<ClassName, ClassDefinition>();
        for (final ClassDefinition def : classes) {
            assert(!result.containsKey(def.myName));
            result.put(def.myName, def);
        }
        return result;
    }
    
    public void assertResultC(final int expected,
                              final Stmt entryPoint,
                              final ClassDefinition... classes) throws IOException {
        assertResult(expected,
                     new Program(classes, entryPoint),
                     makeMapping(classes));
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
    public void testPrintInMethodNoParams() throws IOException {
        // class Foo {
        //   init() {}
        //   void foo() {
        //     print(1);
        //   }
        // }
        // Foo f = new Print();
        // void v = f.foo();

        final ClassName fooClass = new ClassName("Foo");
        final MethodName fooMethod = new MethodName("foo");
        final Variable f = new Variable("f");
        final MethodCallStmt call = new MethodCallStmt(new VarDec(new VoidType(),
                                                                  new Variable("v")),
                                                       new LhsExp(new VariableLhs(f)),
                                                       fooMethod,
                                                       new Exp[0]);
        call.setOnClass(fooClass);
        
        assertResultC(1,
                      stmts(new NewStmt(new VarDec(new ClassType(fooClass),
                                                   f),
                                        fooClass,
                                        new Exp[0]),
                            call),
                      new ClassDefinition(fooClass,
                                          null,
                                          new VarDec[0],
                                          new Constructor(new VarDec[0],
                                                          new EmptyStmt()),
                                          new MethodDefinition[] {
                                              new MethodDefinition(false,
                                                                   new VoidType(),
                                                                   fooMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(1)))
                                          }));
    }

    @Test
    public void testPrintInMethodOneParam() throws IOException {
        // class Foo {
        //   init() {}
        //   void foo(int x) {
        //     print(x);
        //   }
        // }
        // Foo f = new Print();
        // void v = f.foo(1);

        final ClassName fooClass = new ClassName("Foo");
        final MethodName fooMethod = new MethodName("foo");
        final Variable f = new Variable("f");
        final Variable x = new Variable("x");
        final MethodCallStmt call = new MethodCallStmt(new VarDec(new VoidType(),
                                                                  new Variable("v")),
                                                       new LhsExp(new VariableLhs(f)),
                                                       fooMethod,
                                                       new Exp[] {
                                                           new IntExp(1)
                                                       });
        call.setOnClass(fooClass);
        
        assertResultC(1,
                      stmts(new NewStmt(new VarDec(new ClassType(fooClass),
                                                   f),
                                        fooClass,
                                        new Exp[0]),
                            call),
                      new ClassDefinition(fooClass,
                                          null,
                                          new VarDec[0],
                                          new Constructor(new VarDec[0],
                                                          new EmptyStmt()),
                                          new MethodDefinition[] {
                                              new MethodDefinition(false,
                                                                   new VoidType(),
                                                                   fooMethod,
                                                                   new VarDec[] {
                                                                       new VarDec(new IntType(), x)
                                                                   },
                                                                   new PrintStmt(new LhsExp(new VariableLhs(x))))
                                          }));
    }

    @Test
    public void testPrintInMethodInstanceVariable() throws IOException {
        // class Foo {
        //   int x;
        //   init(int x) {
        //     this.x = x;
        //   }
        //   void foo() {
        //     print(this.x);
        //   }
        // }
        // Foo f = new Print(1);
        // void v = f.foo();

        final ClassName fooClass = new ClassName("Foo");
        final MethodName fooMethod = new MethodName("foo");
        final Variable f = new Variable("f");
        final Variable x = new Variable("x");
        final MethodCallStmt call = new MethodCallStmt(new VarDec(new VoidType(),
                                                                  new Variable("v")),
                                                       new LhsExp(new VariableLhs(f)),
                                                       fooMethod,
                                                       new Exp[0]);
        call.setOnClass(fooClass);

        final FieldAccessLhs lhs = new FieldAccessLhs(new ThisLhs(), x);
        lhs.setLhsClass(fooClass);
        
        assertResultC(1,
                      stmts(new NewStmt(new VarDec(new ClassType(fooClass),
                                                   f),
                                        fooClass,
                                        new Exp[] {
                                            new IntExp(1)
                                        }),
                            call),
                      new ClassDefinition(fooClass,
                                          null,
                                          new VarDec[] {
                                              new VarDec(new IntType(), x)
                                          },
                                          new Constructor(new VarDec[] {
                                                  new VarDec(new IntType(), x)
                                              },
                                              new AssignStmt(lhs,
                                                             new LhsExp(new VariableLhs(x)))),
                                          new MethodDefinition[] {
                                              new MethodDefinition(false,
                                                                   new VoidType(),
                                                                   fooMethod,
                                                                   new VarDec[] {
                                                                       new VarDec(new IntType(), x)
                                                                   },
                                                                   new PrintStmt(new LhsExp(lhs)))
                                          }));
    }
}
