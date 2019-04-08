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
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(lhs)))
                                          }));
    }

    @Test
    public void testReturnMethod() throws IOException {
        // class Foo {
        //   int x;
        //   init(int x) {
        //     this.x = x;
        //   }
        //   int foo() {
        //     return this.x;
        //   }
        // }
        // Foo f = new Print(1);
        // int v = f.foo();
        // print(v);

        final ClassName fooClass = new ClassName("Foo");
        final MethodName fooMethod = new MethodName("foo");
        final Variable f = new Variable("f");
        final Variable x = new Variable("x");
        final Variable v = new Variable("v");
        final MethodCallStmt call = new MethodCallStmt(new VarDec(new IntType(),
                                                                  v),
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
                            call,
                            new PrintStmt(new LhsExp(new VariableLhs(v)))),
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
                                                                   new IntType(),
                                                                   fooMethod,
                                                                   new VarDec[0],
                                                                   new ReturnStmt(new LhsExp(lhs)))
                                          }));
    }

    @Test
    public void testSuperGetFirst() throws IOException {
        // class Base {
        //   int x;
        //   init(int x) {
        //     this.x = x;
        //   }
        // }
        // class Sub extends Base {
        //   int y;
        //   init(int x, int y) {
        //     super(x);
        //     this.y = y;
        //   }
        // }
        // Sub s = new Sub(1, 2);
        // print(s.x);

        final ClassName baseClass = new ClassName("Base");
        final ClassName subClass = new ClassName("Sub");
        final Variable x = new Variable("x");
        final Variable y = new Variable("y");
        final Variable s = new Variable("s");
        final FieldAccessLhs accessThisX = new FieldAccessLhs(new ThisLhs(), x);
        accessThisX.setLhsClass(baseClass);
        final FieldAccessLhs accessThisY = new FieldAccessLhs(new ThisLhs(), y);
        accessThisY.setLhsClass(subClass);
        final FieldAccessLhs accessSX = new FieldAccessLhs(new VariableLhs(s), x);
        accessSX.setLhsClass(subClass);

        assertResultC(1,
                      stmts(new NewStmt(new VarDec(new ClassType(subClass),
                                                   s),
                                        subClass,
                                        new Exp[] {
                                            new IntExp(1),
                                            new IntExp(2)
                                        }),
                            new PrintStmt(new LhsExp(accessSX))),
                      new ClassDefinition(baseClass,
                                          null,
                                          new VarDec[] {
                                              new VarDec(new IntType(), x)
                                          },
                                          new Constructor(new VarDec[] {
                                                  new VarDec(new IntType(), x)
                                              },
                                              new AssignStmt(accessThisX,
                                                             new LhsExp(new VariableLhs(x)))),
                                          new MethodDefinition[0]),
                      new ClassDefinition(subClass,
                                          baseClass,
                                          new VarDec[] {
                                              new VarDec(new IntType(), y)
                                          },
                                          new Constructor(new VarDec[] {
                                                  new VarDec(new IntType(), x),
                                                  new VarDec(new IntType(), y)
                                              },
                                              stmts(new SuperStmt(new Exp[] {
                                                          new LhsExp(new VariableLhs(x))
                                                      }),
                                                  new AssignStmt(accessThisY,
                                                                 new LhsExp(new VariableLhs(y))))),
                                          new MethodDefinition[0]));
    }

    @Test
    public void testSuperGetSecond() throws IOException {
        // class Base {
        //   int x;
        //   init(int x) {
        //     this.x = x;
        //   }
        // }
        // class Sub extends Base {
        //   int y;
        //   init(int x, int y) {
        //     super(x);
        //     this.y = y;
        //   }
        // }
        // Sub s = new Sub(1, 2);
        // print(s.y);

        final ClassName baseClass = new ClassName("Base");
        final ClassName subClass = new ClassName("Sub");
        final Variable x = new Variable("x");
        final Variable y = new Variable("y");
        final Variable s = new Variable("s");
        final FieldAccessLhs accessThisX = new FieldAccessLhs(new ThisLhs(), x);
        accessThisX.setLhsClass(baseClass);
        final FieldAccessLhs accessThisY = new FieldAccessLhs(new ThisLhs(), y);
        accessThisY.setLhsClass(subClass);
        final FieldAccessLhs accessSY = new FieldAccessLhs(new VariableLhs(s), y);
        accessSY.setLhsClass(subClass);

        assertResultC(2,
                      stmts(new NewStmt(new VarDec(new ClassType(subClass),
                                                   s),
                                        subClass,
                                        new Exp[] {
                                            new IntExp(1),
                                            new IntExp(2)
                                        }),
                            new PrintStmt(new LhsExp(accessSY))),
                      new ClassDefinition(baseClass,
                                          null,
                                          new VarDec[] {
                                              new VarDec(new IntType(), x)
                                          },
                                          new Constructor(new VarDec[] {
                                                  new VarDec(new IntType(), x)
                                              },
                                              new AssignStmt(accessThisX,
                                                             new LhsExp(new VariableLhs(x)))),
                                          new MethodDefinition[0]),
                      new ClassDefinition(subClass,
                                          baseClass,
                                          new VarDec[] {
                                              new VarDec(new IntType(), y)
                                          },
                                          new Constructor(new VarDec[] {
                                                  new VarDec(new IntType(), x),
                                                  new VarDec(new IntType(), y)
                                              },
                                              stmts(new SuperStmt(new Exp[] {
                                                          new LhsExp(new VariableLhs(x))
                                                      }),
                                                  new AssignStmt(accessThisY,
                                                                 new LhsExp(new VariableLhs(y))))),
                                          new MethodDefinition[0]));
    }

    @Test
    public void testSingleVirtualNoInheritance() throws IOException {
        // class Base {
        //   init() {}
        //   virtual void doPrint() {
        //     print(1);
        //   }
        // }
        // Base b = new Base();
        // void v = b.doPrint();

        final ClassName baseClass = new ClassName("Base");
        final MethodName doPrintMethod = new MethodName("doPrint");
        final Variable b = new Variable("b");
        final Variable v = new Variable("v");

        final MethodCallStmt call = new MethodCallStmt(new VarDec(new VoidType(), v),
                                                       new LhsExp(new VariableLhs(b)),
                                                       doPrintMethod,
                                                       new Exp[0]);
        call.setOnClass(baseClass);

        assertResultC(1,
                      stmts(new NewStmt(new VarDec(new ClassType(baseClass), b),
                                        baseClass,
                                        new Exp[0]),
                            call),
                      new ClassDefinition(baseClass,
                                          null,
                                          new VarDec[0],
                                          new Constructor(new VarDec[0], new EmptyStmt()),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(1)))
                                          }));
    }
}
