package vtables_example.codegen;

import vtables_example.syntax.*;
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

    @Test
    public void testSingleVirtualNoInheritanceInstanceVariable() throws IOException {
        // class Base {
        //   int x;
        //   init(int x) {
        //     this.x = x;
        //   }
        //   virtual void doPrint() {
        //     print(this.x);
        //   }
        // }
        // Base b = new Base(1);
        // void v = b.doPrint();

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
                                                       new Exp[0]);
        call.setOnClass(baseClass);

        assertResultC(1,
                      stmts(new NewStmt(new VarDec(new ClassType(baseClass), b),
                                        baseClass,
                                        new Exp[] {
                                            new IntExp(1)
                                        }),
                            call),
                      new ClassDefinition(baseClass,
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
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(accessX)))
                                          }));
    }

    @Test
    public void testSingleVirtualWithInheritanceUsesBase() throws IOException {
        // class Base {
        //   init() {}
        //   virtual void doPrint() {
        //     print(1);
        //   }
        // }
        // class Sub extends Base {
        //   int x;
        //   init(int x) {
        //     super();
        //     this.x = x;
        //   }
        //   virtual void doPrint() {
        //     print(this.x);
        //   }
        // }
        // Base b = new Base();
        // void v = b.doPrint();

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
                                          }),
                      new ClassDefinition(subClass,
                                          baseClass,
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
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(accessX)))
                                          }));
    }

    @Test
    public void testSingleVirtualWithInheritanceUsesSub() throws IOException {
        // class Base {
        //   init() {}
        //   virtual void doPrint() {
        //     print(1);
        //   }
        // }
        // class Sub extends Base {
        //   int x;
        //   init(int x) {
        //     super();
        //     this.x = x;
        //   }
        //   virtual void doPrint() {
        //     print(this.x);
        //   }
        // }
        // Base b = new Sub(2);
        // void v = b.doPrint();

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
                                                       new Exp[0]);
        call.setOnClass(baseClass);

        assertResultC(2,
                      stmts(new NewStmt(new VarDec(new ClassType(baseClass), b),
                                        subClass,
                                        new Exp[] {
                                            new IntExp(2)
                                        }),
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
                                          }),
                      new ClassDefinition(subClass,
                                          baseClass,
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
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(accessX)))
                                          }));
    }

    @Test
    public void testHasInheritedMethod() throws IOException {
        // class Base {
        //   init() {}
        //   virtual void doPrint() {
        //     print(1);
        //   }
        //   virtual void doOtherPrint() {
        //     print(3);
        //   }
        // }
        // class Sub extends Base {
        //   int x;
        //   init(int x) {
        //     super();
        //     this.x = x;
        //   }
        //   virtual void doPrint() {
        //     print(this.x);
        //   }
        // }
        // Base b = new Sub(2);
        // void v = b.doOtherPrint();

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
                                                       new Exp[0]);
        call.setOnClass(baseClass);

        assertResultC(3,
                      stmts(new NewStmt(new VarDec(new ClassType(baseClass), b),
                                        subClass,
                                        new Exp[] {
                                            new IntExp(2)
                                        }),
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
                                                                   new PrintStmt(new IntExp(1))),
                                              new MethodDefinition(true,
                                                                   new VoidType(),
                                                                   doOtherPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(3)))
                                          }),
                      new ClassDefinition(subClass,
                                          baseClass,
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
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(accessX)))
                                          }));
    }

    @Test
    public void testCanAddVirtualInSubclass() throws IOException {
        // class Base {
        //   init() {}
        //   virtual void doPrint() {
        //     print(1);
        //   }
        //   virtual void doOtherPrint() {
        //     print(3);
        //   }
        // }
        // class Sub extends Base {
        //   int x;
        //   init(int x) {
        //     super();
        //     this.x = x;
        //   }
        //   virtual void doPrint() {
        //     print(this.x);
        //   }
        //   virtual int getX() {
        //     return this.x;
        //   }
        // }
        // Sub s = new Sub(2);
        // int v = s.getX();
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
                                                       new Exp[0]);
        call.setOnClass(subClass);

        assertResultC(2,
                      stmts(new NewStmt(new VarDec(new ClassType(subClass), s),
                                        subClass,
                                        new Exp[] {
                                            new IntExp(2)
                                        }),
                            call,
                            new PrintStmt(new LhsExp(new VariableLhs(v)))),
                      new ClassDefinition(baseClass,
                                          null,
                                          new VarDec[0],
                                          new Constructor(new VarDec[0], new EmptyStmt()),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(1))),
                                              new MethodDefinition(true,
                                                                   new VoidType(),
                                                                   doOtherPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(3)))
                                          }),
                      new ClassDefinition(subClass,
                                          baseClass,
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
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(accessX))),
                                              new MethodDefinition(true,
                                                                   new IntType(),
                                                                   getXMethod,
                                                                   new VarDec[0],
                                                                   new ReturnStmt(new LhsExp(accessX)))
                                          }));
    }

    @Test
    public void testCanAddNonVirtualInSubclass() throws IOException {
        // class Base {
        //   init() {}
        //   virtual void doPrint() {
        //     print(1);
        //   }
        //   virtual void doOtherPrint() {
        //     print(3);
        //   }
        // }
        // class Sub extends Base {
        //   int x;
        //   init(int x) {
        //     super();
        //     this.x = x;
        //   }
        //   virtual void doPrint() {
        //     print(this.x);
        //   }
        //   int getX() {
        //     return this.x;
        //   }
        // }
        // Sub s = new Sub(2);
        // int v = s.getX();
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
                                                       new Exp[0]);
        call.setOnClass(subClass);

        assertResultC(2,
                      stmts(new NewStmt(new VarDec(new ClassType(subClass), s),
                                        subClass,
                                        new Exp[] {
                                            new IntExp(2)
                                        }),
                            call,
                            new PrintStmt(new LhsExp(new VariableLhs(v)))),
                      new ClassDefinition(baseClass,
                                          null,
                                          new VarDec[0],
                                          new Constructor(new VarDec[0], new EmptyStmt()),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(1))),
                                              new MethodDefinition(true,
                                                                   new VoidType(),
                                                                   doOtherPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(3)))
                                          }),
                      new ClassDefinition(subClass,
                                          baseClass,
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
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(accessX))),
                                              new MethodDefinition(false,
                                                                   new IntType(),
                                                                   getXMethod,
                                                                   new VarDec[0],
                                                                   new ReturnStmt(new LhsExp(accessX)))
                                          }));
    }

    @Test
    public void testCanInheritFromBaseMultipleTimes() throws IOException {
        // class Base {
        //   init() {}
        //   virtual void doPrint() {
        //     print(1);
        //   }
        //   virtual void doOtherPrint() {
        //     print(3);
        //   }
        // }
        // class Sub1 extends Base {
        //   int x;
        //   init(int x) {
        //     super();
        //     this.x = x;
        //   }
        //   virtual void doPrint() {
        //     print(this.x);
        //   }
        //   int getX() {
        //     return this.x;
        //   }
        // }
        // class Sub2 extends Base {
        //   init() {
        //     super();
        //   }
        //   virtual void doPrint() {
        //     print(4);
        //   }
        // }
        // Base b = new Sub2();
        // void v = b.doPrint();

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
                                                       new Exp[0]);
        call.setOnClass(baseClass);

        assertResultC(4,
                      stmts(new NewStmt(new VarDec(new ClassType(baseClass), s),
                                        sub2Class,
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
                                                                   new PrintStmt(new IntExp(1))),
                                              new MethodDefinition(true,
                                                                   new VoidType(),
                                                                   doOtherPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(3)))
                                          }),
                      new ClassDefinition(sub1Class,
                                          baseClass,
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
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new LhsExp(accessX))),
                                              new MethodDefinition(false,
                                                                   new IntType(),
                                                                   getXMethod,
                                                                   new VarDec[0],
                                                                   new ReturnStmt(new LhsExp(accessX)))
                                          }),
                      new ClassDefinition(sub2Class,
                                          baseClass,
                                          new VarDec[0],
                                          new Constructor(new VarDec[0], new SuperStmt(new Exp[0])),
                                          new MethodDefinition[] {
                                              new MethodDefinition(true,
                                                                   new VoidType(),
                                                                   doPrintMethod,
                                                                   new VarDec[0],
                                                                   new PrintStmt(new IntExp(4)))
                                          }));
    }
}
