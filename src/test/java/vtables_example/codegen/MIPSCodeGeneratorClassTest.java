package vtables_example.codegen;

import vtables_example.syntax.*;
import vtables_example.typechecker.Typechecker;
import vtables_example.typechecker.TypeErrorException;
import vtables_example.typechecker.TypecheckerClassTest;
import static vtables_example.typechecker.TypecheckerClassTest.stmts;

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

    public void assertResultTyped(final int expected,
                                  final Stmt entryPoint,
                                  final ClassDefinition... classes) throws IOException, TypeErrorException {
        final Program program = new Program(classes, entryPoint);
        Typechecker.typecheckProgram(program);
        assertResult(expected,
                     program,
                     makeMapping(classes));
    }
    
    @Test
    public void testPrintInMethodNoParams() throws IOException {
        // class Foo<> {
        //   init() {}
        //   <> void foo() {
        //     print(1);
        //   }
        // }
        // Foo<> f = new Print<>();
        // void v = f.foo<>();

        final ClassName fooClass = new ClassName("Foo");
        final MethodName fooMethod = new MethodName("foo");
        final Variable f = new Variable("f");
        final MethodCallStmt call = new MethodCallStmt(new VarDec(new VoidType(),
                                                                  new Variable("v")),
                                                       new LhsExp(new VariableLhs(f)),
                                                       fooMethod,
                                                       new Type[0],
                                                       new Exp[0]);
        call.setOnClass(fooClass);
        
        assertResultC(1,
                      stmts(new NewStmt(new VarDec(new ClassType(fooClass, new Type[0]),
                                                   f),
                                        fooClass,
                                        new Type[0],
                                        new Exp[0]),
                            call),
                      new ClassDefinition(fooClass,
                                          new TypeVariable[0],
                                          null,
                                          new VarDec[0],
                                          new Constructor(new VarDec[0],
                                                          new EmptyStmt()),
                                          new MethodDefinition[] {
                                              new MethodDefinition(false,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   fooMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(1)))
                                          }));
    }

    @Test
    public void testPrintInMethodOneParam() throws IOException {
        // class Foo<> {
        //   init() {}
        //   <> void foo(int x) {
        //     print(x);
        //   }
        // }
        // Foo<> f = new Print<>();
        // void v = f.foo<>(1);

        final ClassName fooClass = new ClassName("Foo");
        final MethodName fooMethod = new MethodName("foo");
        final Variable f = new Variable("f");
        final Variable x = new Variable("x");
        final MethodCallStmt call = new MethodCallStmt(new VarDec(new VoidType(),
                                                                  new Variable("v")),
                                                       new LhsExp(new VariableLhs(f)),
                                                       fooMethod,
                                                       new Type[0],
                                                       new Exp[] {
                                                           new IntExp(1)
                                                       });
        call.setOnClass(fooClass);
        
        assertResultC(1,
                      stmts(new NewStmt(new VarDec(new ClassType(fooClass, new Type[0]),
                                                   f),
                                        fooClass,
                                        new Type[0],
                                        new Exp[0]),
                            call),
                      new ClassDefinition(fooClass,
                                          new TypeVariable[0],
                                          null,
                                          new VarDec[0],
                                          new Constructor(new VarDec[0],
                                                          new EmptyStmt()),
                                          new MethodDefinition[] {
                                              new MethodDefinition(false,
                                                                   new TypeVariable[0],
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
        // class Foo<> {
        //   int x;
        //   init(int x) {
        //     this.x = x;
        //   }
        //   <> void foo() {
        //     print(this.x);
        //   }
        // }
        // Foo<> f = new Print<>(1);
        // void v = f.foo<>();

        final ClassName fooClass = new ClassName("Foo");
        final MethodName fooMethod = new MethodName("foo");
        final Variable f = new Variable("f");
        final Variable x = new Variable("x");
        final MethodCallStmt call = new MethodCallStmt(new VarDec(new VoidType(),
                                                                  new Variable("v")),
                                                       new LhsExp(new VariableLhs(f)),
                                                       fooMethod,
                                                       new Type[0],
                                                       new Exp[0]);
        call.setOnClass(fooClass);

        final FieldAccessLhs lhs = new FieldAccessLhs(new ThisLhs(), x);
        lhs.setLhsClass(fooClass);
        
        assertResultC(1,
                      stmts(new NewStmt(new VarDec(new ClassType(fooClass, new Type[0]),
                                                   f),
                                        fooClass,
                                        new Type[0],
                                        new Exp[] {
                                            new IntExp(1)
                                        }),
                            call),
                      new ClassDefinition(fooClass,
                                          new TypeVariable[0],
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
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   fooMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(lhs)))
                                          }));
    }

    @Test
    public void testReturnMethod() throws IOException {
        // class Foo<> {
        //   int x;
        //   init(int x) {
        //     this.x = x;
        //   }
        //   <> int foo() {
        //     return this.x;
        //   }
        // }
        // Foo<> f = new Print<>(1);
        // int v = f.foo<>();
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
                                                       new Type[0],
                                                       new Exp[0]);
        call.setOnClass(fooClass);

        final FieldAccessLhs lhs = new FieldAccessLhs(new ThisLhs(), x);
        lhs.setLhsClass(fooClass);
        
        assertResultC(1,
                      stmts(new NewStmt(new VarDec(new ClassType(fooClass, new Type[0]),
                                                   f),
                                        fooClass,
                                        new Type[0],
                                        new Exp[] {
                                            new IntExp(1)
                                        }),
                            call,
                            new PrintStmt(new LhsExp(new VariableLhs(v)))),
                      new ClassDefinition(fooClass,
                                          new TypeVariable[0],
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
                                                                   new TypeVariable[0],
                                                                   new IntType(),
                                                                   fooMethod,
                                                                   new VarDec[0],
                                                                   new ReturnStmt(new LhsExp(lhs)))
                                          }));
    }

    @Test
    public void testSuperGetFirst() throws IOException {
        // class Base<> {
        //   int x;
        //   init(int x) {
        //     this.x = x;
        //   }
        // }
        // class Sub<> extends Base<> {
        //   int y;
        //   init(int x, int y) {
        //     super(x);
        //     this.y = y;
        //   }
        // }
        // Sub<> s = new Sub<>(1, 2);
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
                      stmts(new NewStmt(new VarDec(new ClassType(subClass, new Type[0]),
                                                   s),
                                        subClass,
                                        new Type[0],
                                        new Exp[] {
                                            new IntExp(1),
                                            new IntExp(2)
                                        }),
                            new PrintStmt(new LhsExp(accessSX))),
                      new ClassDefinition(baseClass,
                                          new TypeVariable[0],
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
                                          new TypeVariable[0],
                                          new Extends(baseClass, new Type[0]),
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
        // class Base<> {
        //   int x;
        //   init(int x) {
        //     this.x = x;
        //   }
        // }
        // class Sub<> extends Base<> {
        //   int y;
        //   init(int x, int y) {
        //     super(x);
        //     this.y = y;
        //   }
        // }
        // Sub<> s = new Sub<>(1, 2);
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
                      stmts(new NewStmt(new VarDec(new ClassType(subClass, new Type[0]),
                                                   s),
                                        subClass,
                                        new Type[0],
                                        new Exp[] {
                                            new IntExp(1),
                                            new IntExp(2)
                                        }),
                            new PrintStmt(new LhsExp(accessSY))),
                      new ClassDefinition(baseClass,
                                          new TypeVariable[0],
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
                                          new TypeVariable[0],
                                          new Extends(baseClass, new Type[0]),
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
        // class Base<> {
        //   init() {}
        //   <> virtual void doPrint() {
        //     print(1);
        //   }
        // }
        // Base<> b = new Base<>();
        // void v = b.doPrint<>();

        final ClassName baseClass = new ClassName("Base");
        final MethodName doPrintMethod = new MethodName("doPrint");
        final Variable b = new Variable("b");
        final Variable v = new Variable("v");

        final MethodCallStmt call = new MethodCallStmt(new VarDec(new VoidType(), v),
                                                       new LhsExp(new VariableLhs(b)),
                                                       doPrintMethod,
                                                       new Type[0],
                                                       new Exp[0]);
        call.setOnClass(baseClass);

        assertResultC(1,
                      stmts(new NewStmt(new VarDec(new ClassType(baseClass, new Type[0]), b),
                                        baseClass,
                                        new Type[0],
                                        new Exp[0]),
                            call),
                      new ClassDefinition(baseClass,
                                          new TypeVariable[0],
                                          null,
                                          new VarDec[0],
                                          new Constructor(new VarDec[0], new EmptyStmt()),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(1)))
                                          }));
    }

    @Test
    public void testSingleVirtualNoInheritanceInstanceVariable() throws IOException {
        // class Base<> {
        //   int x;
        //   init(int x) {
        //     this.x = x;
        //   }
        //   <> virtual void doPrint() {
        //     print(this.x);
        //   }
        // }
        // Base<> b = new Base<>(1);
        // void v = b.doPrint<>();

        final ClassName baseClass = new ClassName("Base");
        final MethodName doPrintMethod = new MethodName("doPrint");
        final Variable x = new Variable("x");
        final Variable b = new Variable("b");
        final Variable v = new Variable("v");
        final FieldAccessLhs accessX = new FieldAccessLhs(new ThisLhs(), x);
        accessX.setLhsClass(baseClass);
        
        final MethodCallStmt call = new MethodCallStmt(new VarDec(new VoidType(), v),
                                                       new LhsExp(new VariableLhs(b)),
                                                       doPrintMethod,
                                                       new Type[0],
                                                       new Exp[0]);
        call.setOnClass(baseClass);

        assertResultC(1,
                      stmts(new NewStmt(new VarDec(new ClassType(baseClass, new Type[0]), b),
                                        baseClass,
                                        new Type[0],
                                        new Exp[] {
                                            new IntExp(1)
                                        }),
                            call),
                      new ClassDefinition(baseClass,
                                          new TypeVariable[0],
                                          null,
                                          new VarDec[] {
                                              new VarDec(new IntType(), x)
                                          },
                                          new Constructor(new VarDec[] {
                                                  new VarDec(new IntType(), x)
                                              },
                                              new AssignStmt(accessX, new LhsExp(new VariableLhs(x)))),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(accessX)))
                                          }));
    }

    @Test
    public void testSingleVirtualWithInheritanceUsesBase() throws IOException {
        // class Base<> {
        //   init() {}
        //   <> virtual void doPrint() {
        //     print(1);
        //   }
        // }
        // class Sub<> extends Base<> {
        //   int x;
        //   init(int x) {
        //     super();
        //     this.x = x;
        //   }
        //   <> virtual void doPrint() {
        //     print(this.x);
        //   }
        // }
        // Base<> b = new Base<>();
        // void v = b.doPrint<>();

        final ClassName baseClass = new ClassName("Base");
        final ClassName subClass = new ClassName("Sub");
        final MethodName doPrintMethod = new MethodName("doPrint");
        final Variable x = new Variable("x");
        final Variable b = new Variable("b");
        final Variable v = new Variable("v");
        final FieldAccessLhs accessX = new FieldAccessLhs(new ThisLhs(), x);
        accessX.setLhsClass(subClass);
        
        final MethodCallStmt call = new MethodCallStmt(new VarDec(new VoidType(), v),
                                                       new LhsExp(new VariableLhs(b)),
                                                       doPrintMethod,
                                                       new Type[0],
                                                       new Exp[0]);
        call.setOnClass(baseClass);

        assertResultC(1,
                      stmts(new NewStmt(new VarDec(new ClassType(baseClass, new Type[0]), b),
                                        baseClass,
                                        new Type[0],
                                        new Exp[0]),
                            call),
                      new ClassDefinition(baseClass,
                                          new TypeVariable[0],
                                          null,
                                          new VarDec[0],
                                          new Constructor(new VarDec[0], new EmptyStmt()),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(1)))
                                          }),
                      new ClassDefinition(subClass,
                                          new TypeVariable[0],
                                          new Extends(baseClass, new Type[0]),
                                          new VarDec[] {
                                              new VarDec(new IntType(), x)
                                          },
                                          new Constructor(new VarDec[] {
                                                  new VarDec(new IntType(), x)
                                              },
                                              stmts(new SuperStmt(new Exp[0]),
                                                    new AssignStmt(accessX, new LhsExp(new VariableLhs(x))))),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(accessX)))
                                          }));
    }

    @Test
    public void testSingleVirtualWithInheritanceUsesSub() throws IOException {
        // class Base<> {
        //   init() {}
        //   <> virtual void doPrint() {
        //     print(1);
        //   }
        // }
        // class Sub<> extends Base<> {
        //   int x;
        //   init(int x) {
        //     super();
        //     this.x = x;
        //   }
        //   <> virtual void doPrint() {
        //     print(this.x);
        //   }
        // }
        // Base<> b = new Sub<>(2);
        // void v = b.doPrint<>();

        final ClassName baseClass = new ClassName("Base");
        final ClassName subClass = new ClassName("Sub");
        final MethodName doPrintMethod = new MethodName("doPrint");
        final Variable x = new Variable("x");
        final Variable b = new Variable("b");
        final Variable v = new Variable("v");
        final FieldAccessLhs accessX = new FieldAccessLhs(new ThisLhs(), x);
        accessX.setLhsClass(subClass);
        
        final MethodCallStmt call = new MethodCallStmt(new VarDec(new VoidType(), v),
                                                       new LhsExp(new VariableLhs(b)),
                                                       doPrintMethod,
                                                       new Type[0],
                                                       new Exp[0]);
        call.setOnClass(baseClass);

        assertResultC(2,
                      stmts(new NewStmt(new VarDec(new ClassType(baseClass, new Type[0]), b),
                                        subClass,
                                        new Type[0],
                                        new Exp[] {
                                            new IntExp(2)
                                        }),
                            call),
                      new ClassDefinition(baseClass,
                                          new TypeVariable[0],
                                          null,
                                          new VarDec[0],
                                          new Constructor(new VarDec[0], new EmptyStmt()),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(1)))
                                          }),
                      new ClassDefinition(subClass,
                                          new TypeVariable[0],
                                          new Extends(baseClass, new Type[0]),
                                          new VarDec[] {
                                              new VarDec(new IntType(), x)
                                          },
                                          new Constructor(new VarDec[] {
                                                  new VarDec(new IntType(), x)
                                              },
                                              stmts(new SuperStmt(new Exp[0]),
                                                    new AssignStmt(accessX, new LhsExp(new VariableLhs(x))))),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(accessX)))
                                          }));
    }

    @Test
    public void testHasInheritedMethod() throws IOException {
        // class Base<> {
        //   init() {}
        //   <> virtual void doPrint() {
        //     print(1);
        //   }
        //   <> virtual void doOtherPrint() {
        //     print(3);
        //   }
        // }
        // class Sub<> extends Base<> {
        //   int x;
        //   init(int x) {
        //     super();
        //     this.x = x;
        //   }
        //   <> virtual void doPrint() {
        //     print(this.x);
        //   }
        // }
        // Base<> b = new Sub<>(2);
        // void v = b.doOtherPrint<>();

        final ClassName baseClass = new ClassName("Base");
        final ClassName subClass = new ClassName("Sub");
        final MethodName doPrintMethod = new MethodName("doPrint");
        final MethodName doOtherPrintMethod = new MethodName("doOtherPrint");
        final Variable x = new Variable("x");
        final Variable b = new Variable("b");
        final Variable v = new Variable("v");
        final FieldAccessLhs accessX = new FieldAccessLhs(new ThisLhs(), x);
        accessX.setLhsClass(subClass);
        
        final MethodCallStmt call = new MethodCallStmt(new VarDec(new VoidType(), v),
                                                       new LhsExp(new VariableLhs(b)),
                                                       doOtherPrintMethod,
                                                       new Type[0],
                                                       new Exp[0]);
        call.setOnClass(baseClass);

        assertResultC(3,
                      stmts(new NewStmt(new VarDec(new ClassType(baseClass, new Type[0]), b),
                                        subClass,
                                        new Type[0],
                                        new Exp[] {
                                            new IntExp(2)
                                        }),
                            call),
                      new ClassDefinition(baseClass,
                                          new TypeVariable[0],
                                          null,
                                          new VarDec[0],
                                          new Constructor(new VarDec[0], new EmptyStmt()),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(1))),
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doOtherPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(3)))
                                          }),
                      new ClassDefinition(subClass,
                                          new TypeVariable[0],
                                          new Extends(baseClass, new Type[0]),
                                          new VarDec[] {
                                              new VarDec(new IntType(), x)
                                          },
                                          new Constructor(new VarDec[] {
                                                  new VarDec(new IntType(), x)
                                              },
                                              stmts(new SuperStmt(new Exp[0]),
                                                    new AssignStmt(accessX, new LhsExp(new VariableLhs(x))))),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(accessX)))
                                          }));
    }

    @Test
    public void testCanAddVirtualInSubclass() throws IOException {
        // class Base<> {
        //   init() {}
        //   <> virtual void doPrint() {
        //     print(1);
        //   }
        //   <> virtual void doOtherPrint() {
        //     print(3);
        //   }
        // }
        // class Sub<> extends Base<> {
        //   int x;
        //   init(int x) {
        //     super();
        //     this.x = x;
        //   }
        //   <> virtual void doPrint() {
        //     print(this.x);
        //   }
        //   <> virtual int getX() {
        //     return this.x;
        //   }
        // }
        // Sub<> s = new Sub<>(2);
        // int v = s.getX<>();
        // print(v);

        final ClassName baseClass = new ClassName("Base");
        final ClassName subClass = new ClassName("Sub");
        final MethodName doPrintMethod = new MethodName("doPrint");
        final MethodName doOtherPrintMethod = new MethodName("doOtherPrint");
        final MethodName getXMethod = new MethodName("getX");
        final Variable x = new Variable("x");
        final Variable s = new Variable("s");
        final Variable v = new Variable("v");
        final FieldAccessLhs accessX = new FieldAccessLhs(new ThisLhs(), x);
        accessX.setLhsClass(subClass);
        
        final MethodCallStmt call = new MethodCallStmt(new VarDec(new IntType(), v),
                                                       new LhsExp(new VariableLhs(s)),
                                                       getXMethod,
                                                       new Type[0],
                                                       new Exp[0]);
        call.setOnClass(subClass);

        assertResultC(2,
                      stmts(new NewStmt(new VarDec(new ClassType(subClass, new Type[0]), s),
                                        subClass,
                                        new Type[0],
                                        new Exp[] {
                                            new IntExp(2)
                                        }),
                            call,
                            new PrintStmt(new LhsExp(new VariableLhs(v)))),
                      new ClassDefinition(baseClass,
                                          new TypeVariable[0],
                                          null,
                                          new VarDec[0],
                                          new Constructor(new VarDec[0], new EmptyStmt()),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(1))),
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doOtherPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(3)))
                                          }),
                      new ClassDefinition(subClass,
                                          new TypeVariable[0],
                                          new Extends(baseClass, new Type[0]),
                                          new VarDec[] {
                                              new VarDec(new IntType(), x)
                                          },
                                          new Constructor(new VarDec[] {
                                                  new VarDec(new IntType(), x)
                                              },
                                              stmts(new SuperStmt(new Exp[0]),
                                                    new AssignStmt(accessX, new LhsExp(new VariableLhs(x))))),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(accessX))),
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new IntType(),
                                                                   getXMethod,
                                                                   new VarDec[0],
                                                                   new ReturnStmt(new LhsExp(accessX)))
                                          }));
    }

    @Test
    public void testCanAddNonVirtualInSubclass() throws IOException {
        // class Base<> {
        //   init() {}
        //   <> virtual void doPrint() {
        //     print(1);
        //   }
        //   <> virtual void doOtherPrint() {
        //     print(3);
        //   }
        // }
        // class Sub<> extends Base<> {
        //   int x;
        //   init(int x) {
        //     super();
        //     this.x = x;
        //   }
        //   <> virtual void doPrint() {
        //     print(this.x);
        //   }
        //   <> int getX() {
        //     return this.x;
        //   }
        // }
        // Sub<> s = new Sub<>(2);
        // int v = s.getX<>();
        // print(v);

        final ClassName baseClass = new ClassName("Base");
        final ClassName subClass = new ClassName("Sub");
        final MethodName doPrintMethod = new MethodName("doPrint");
        final MethodName doOtherPrintMethod = new MethodName("doOtherPrint");
        final MethodName getXMethod = new MethodName("getX");
        final Variable x = new Variable("x");
        final Variable s = new Variable("s");
        final Variable v = new Variable("v");
        final FieldAccessLhs accessX = new FieldAccessLhs(new ThisLhs(), x);
        accessX.setLhsClass(subClass);
        
        final MethodCallStmt call = new MethodCallStmt(new VarDec(new IntType(), v),
                                                       new LhsExp(new VariableLhs(s)),
                                                       getXMethod,
                                                       new Type[0],
                                                       new Exp[0]);
        call.setOnClass(subClass);

        assertResultC(2,
                      stmts(new NewStmt(new VarDec(new ClassType(subClass, new Type[0]), s),
                                        subClass,
                                        new Type[0],
                                        new Exp[] {
                                            new IntExp(2)
                                        }),
                            call,
                            new PrintStmt(new LhsExp(new VariableLhs(v)))),
                      new ClassDefinition(baseClass,
                                          new TypeVariable[0],
                                          null,
                                          new VarDec[0],
                                          new Constructor(new VarDec[0], new EmptyStmt()),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(1))),
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doOtherPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(3)))
                                          }),
                      new ClassDefinition(subClass,
                                          new TypeVariable[0],
                                          new Extends(baseClass, new Type[0]),
                                          new VarDec[] {
                                              new VarDec(new IntType(), x)
                                          },
                                          new Constructor(new VarDec[] {
                                                  new VarDec(new IntType(), x)
                                              },
                                              stmts(new SuperStmt(new Exp[0]),
                                                    new AssignStmt(accessX, new LhsExp(new VariableLhs(x))))),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(accessX))),
                                              new MethodDefinition(false,
                                                                   new TypeVariable[0],
                                                                   new IntType(),
                                                                   getXMethod,
                                                                   new VarDec[0],
                                                                   new ReturnStmt(new LhsExp(accessX)))
                                          }));
    }

    @Test
    public void testCanInheritFromBaseMultipleTimes() throws IOException {
        // class Base<> {
        //   init() {}
        //   <> virtual void doPrint() {
        //     print(1);
        //   }
        //   <> virtual void doOtherPrint() {
        //     print(3);
        //   }
        // }
        // class Sub1<> extends Base<> {
        //   int x;
        //   init(int x) {
        //     super();
        //     this.x = x;
        //   }
        //   <> virtual void doPrint() {
        //     print(this.x);
        //   }
        //   <> int getX() {
        //     return this.x;
        //   }
        // }
        // class Sub2<> extends Base<> {
        //   init() {
        //     super();
        //   }
        //   <> virtual void doPrint() {
        //     print(4);
        //   }
        // }
        // Base<> b = new Sub2<>();
        // void v = b.doPrint<>();

        final ClassName baseClass = new ClassName("Base");
        final ClassName sub1Class = new ClassName("Sub1");
        final ClassName sub2Class = new ClassName("Sub2");
        final MethodName doPrintMethod = new MethodName("doPrint");
        final MethodName doOtherPrintMethod = new MethodName("doOtherPrint");
        final MethodName getXMethod = new MethodName("getX");
        final Variable x = new Variable("x");
        final Variable s = new Variable("s");
        final Variable v = new Variable("v");
        final FieldAccessLhs accessX = new FieldAccessLhs(new ThisLhs(), x);
        accessX.setLhsClass(sub1Class);
        
        final MethodCallStmt call = new MethodCallStmt(new VarDec(new IntType(), v),
                                                       new LhsExp(new VariableLhs(s)),
                                                       doPrintMethod,
                                                       new Type[0],
                                                       new Exp[0]);
        call.setOnClass(baseClass);

        assertResultC(4,
                      stmts(new NewStmt(new VarDec(new ClassType(baseClass, new Type[0]), s),
                                        sub2Class,
                                        new Type[0],
                                        new Exp[0]),
                            call),
                      new ClassDefinition(baseClass,
                                          new TypeVariable[0],
                                          null,
                                          new VarDec[0],
                                          new Constructor(new VarDec[0], new EmptyStmt()),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(1))),
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doOtherPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(3)))
                                          }),
                      new ClassDefinition(sub1Class,
                                          new TypeVariable[0],
                                          new Extends(baseClass, new Type[0]),
                                          new VarDec[] {
                                              new VarDec(new IntType(), x)
                                          },
                                          new Constructor(new VarDec[] {
                                                  new VarDec(new IntType(), x)
                                              },
                                              stmts(new SuperStmt(new Exp[0]),
                                                    new AssignStmt(accessX, new LhsExp(new VariableLhs(x))))),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(accessX))),
                                              new MethodDefinition(false,
                                                                   new TypeVariable[0],
                                                                   new IntType(),
                                                                   getXMethod,
                                                                   new VarDec[0],
                                                                   new ReturnStmt(new LhsExp(accessX)))
                                          }),
                      new ClassDefinition(sub2Class,
                                          new TypeVariable[0],
                                          new Extends(baseClass, new Type[0]),
                                          new VarDec[0],
                                          new Constructor(new VarDec[0], new SuperStmt(new Exp[0])),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new TypeVariable[0],
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(4)))
                                          }));
    }

    @Test
    public void testGenericMethod() throws IOException, TypeErrorException {
        // GenericMethod<> gm = new GenericMethod<>();
        // int x = gm.id<int>(1);
        // print(x);
        assertResultTyped(1,
                          stmts(new NewStmt(new VarDec(new ClassType(TypecheckerClassTest.GENERIC_METHOD_CLASS_NAME,
                                                                     new Type[0]),
                                                       new Variable("gm")),
                                            TypecheckerClassTest.GENERIC_METHOD_CLASS_NAME,
                                            new Type[0],
                                            new Exp[0]),
                                new MethodCallStmt(new VarDec(new IntType(), new Variable("x")),
                                                   new LhsExp(new VariableLhs(new Variable("gm"))),
                                                   new MethodName("id"),
                                                   new Type[]{ new IntType() },
                                                   new Exp[]{ new IntExp(1) }),
                                new PrintStmt(new LhsExp(new VariableLhs(new Variable("x"))))),
                          TypecheckerClassTest.genericMethod());
    }

    @Test
    public void testGenericClass() throws IOException, TypeErrorException {
        // GenericClass<int> g = new GenericClass<int>(1);
        // int x = g.getA<>();
        // print(x);
        assertResultTyped(1,
                          stmts(new NewStmt(new VarDec(new ClassType(TypecheckerClassTest.GENERIC_CLASS_CLASS_NAME,
                                                                     new Type[]{ new IntType() }),
                                                       new Variable("g")),
                                            TypecheckerClassTest.GENERIC_CLASS_CLASS_NAME,
                                            new Type[]{ new IntType() },
                                            new Exp[]{ new IntExp(1) }),
                                new MethodCallStmt(new VarDec(new IntType(),
                                                              new Variable("x")),
                                                   new LhsExp(new VariableLhs(new Variable("g"))),
                                                   new MethodName("getA"),
                                                   new Type[0],
                                                   new Exp[0]),
                                new PrintStmt(new LhsExp(new VariableLhs(new Variable("x"))))),
                          TypecheckerClassTest.genericClass());
    }

    @Test
    public void testGenericClassAndMethodGetFirst() throws IOException, TypeErrorException {
        // WithFirst<int> foo = new WithFirst<int>(7);
        // Pair<int, int> bar = foo.withSecond<int>(8);
        // print(bar.first);

        assertResultTyped(7,
                          stmts(new NewStmt(new VarDec(new ClassType(TypecheckerClassTest.WITH_FIRST_CLASS_NAME,
                                                                     new Type[] { new IntType() }),
                                                       new Variable("foo")),
                                            TypecheckerClassTest.WITH_FIRST_CLASS_NAME,
                                            new Type[] { new IntType() },
                                            new Exp[] { new IntExp(7) }),
                                new MethodCallStmt(new VarDec(new ClassType(TypecheckerClassTest.PAIR_CLASS_NAME,
                                                                            new Type[] {
                                                                                new IntType(),
                                                                                new IntType()
                                                                            }),
                                                              new Variable("bar")),
                                                   new LhsExp(new VariableLhs(new Variable("foo"))),
                                                   new MethodName("withSecond"),
                                                   new Type[] { new IntType() },
                                                   new Exp[] { new IntExp(8) }),
                                new PrintStmt(new LhsExp(new FieldAccessLhs(new VariableLhs(new Variable("bar")),
                                                                            new Variable("first"))))),
                          TypecheckerClassTest.pairClass(),
                          TypecheckerClassTest.withFirstClass());
    }
}
