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
    public static final ClassName WITH_METHOD_CLASS_NAME = new ClassName("WithMethod");
    public static final ClassName GENERIC_METHOD_CLASS_NAME = new ClassName("GenericMethod");
    public static final ClassName GENERIC_CLASS_CLASS_NAME = new ClassName("GenericClass");
    public static final ClassName PAIR_CLASS_NAME = new ClassName("Pair");
    public static final ClassName WITH_FIRST_CLASS_NAME = new ClassName("WithFirst");
    // ---END CONSTANTS---

    // Cannot be constants, as the typechecker will fill in types for certain parts.
    // It expects that these types have not been filled in before.

    // class Base<> {
    //   int b;
    //   init(int b) {
    //     this.b = b;
    //   }
    // }
    public static ClassDefinition baseClass() {
        return new ClassDefinition(BASE_CLASS_NAME,
                                   new TypeVariable[0],
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

    // class Sub<> extends Base<> {
    //   int s;
    //   init(int x, int y) {
    //     super(x);
    //     this.s = y;
    //   }
    // }
    public static ClassDefinition subClass() {
        return new ClassDefinition(SUB_CLASS_NAME,
                                   new TypeVariable[0],
                                   new Extends(BASE_CLASS_NAME, new Type[0]),
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
            e.printStackTrace();
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
            new NewStmt(new VarDec(new ClassType(EMPTY_CLASS_NAME, new Type[0]),
                                   new Variable("x")),
                        EMPTY_CLASS_NAME,
                        new Type[0],
                        new Exp[0]);
        assertWellTyped(mkProgram(stmt, EMPTY_CLASS));
    }

    @Test
    public void testIllTypedNewStmtNoSuchClass() {
        final NewStmt stmt =
            new NewStmt(new VarDec(new ClassType(EMPTY_CLASS_NAME, new Type[0]),
                                   new Variable("x")),
                        EMPTY_CLASS_NAME,
                        new Type[0],
                        new Exp[] { new IntExp(0) });
        assertIllTyped(mkProgram(stmt));
    }

    @Test
    public void testIllTypedNewStmtWrongParams() {
        final NewStmt stmt =
            new NewStmt(new VarDec(new ClassType(EMPTY_CLASS_NAME, new Type[0]),
                                   new Variable("x")),
                        EMPTY_CLASS_NAME,
                        new Type[0],
                        new Exp[] { new IntExp(0) });
        assertIllTyped(mkProgram(stmt, EMPTY_CLASS));
    }

    @Test
    public void testIllTypedNewStmtAssignToIncompatible() {
        final NewStmt stmt =
            new NewStmt(new VarDec(new ClassType(FIELD_CLASS_NAME, new Type[0]),
                                   new Variable("x")),
                        EMPTY_CLASS_NAME,
                        new Type[0],
                        new Exp[] { new IntExp(0) });
        assertIllTyped(mkProgram(stmt, EMPTY_CLASS, FIELD_CLASS));
    }

    @Test
    public void testWellTypedNewStmtSubtyping() {
        final NewStmt stmt =
            new NewStmt(new VarDec(new ClassType(BASE_CLASS_NAME, new Type[0]),
                                   new Variable("x")),
                        SUB_CLASS_NAME,
                        new Type[0],
                        new Exp[] { new IntExp(0), new IntExp(1) });
        assertWellTyped(mkProgram(stmt, baseClass(), subClass()));
    }

    @Test
    public void testIllTypedNewStmtSubtyping() {
        final NewStmt stmt =
            new NewStmt(new VarDec(new ClassType(SUB_CLASS_NAME, new Type[0]),
                                   new Variable("x")),
                        BASE_CLASS_NAME,
                        new Type[0],
                        new Exp[] { new IntExp(0), new IntExp(1) });
        assertIllTyped(mkProgram(stmt, baseClass(), subClass()));
    }
    
    @Test
    public void testWellTypedPrintStmt() {
        assertWellTyped(mkProgram(new PrintStmt(new IntExp(0))));
    }

    // class WithMethod<> {
    //   int w;
    //   init(int w) {
    //     this.w = w;
    //   }
    //   <> int getW() {
    //     return this.w;
    //   }
    // }
    public static ClassDefinition withMethod() {
        return new ClassDefinition(WITH_METHOD_CLASS_NAME,
                                   new TypeVariable[0],
                                   null,
                                   new VarDec[] { new VarDec(new IntType(), new Variable("w")) },
                                   new Constructor(new VarDec[] { new VarDec(new IntType(), new Variable("w")) },
                                                   new AssignStmt(new FieldAccessLhs(new ThisLhs(),
                                                                                     new Variable("w")),
                                                                  new LhsExp(new VariableLhs(new Variable("w"))))),
                                   new MethodDefinition[] {
                                       new MethodDefinition(false,
                                                            new TypeVariable[0],
                                                            new IntType(),
                                                            new MethodName("getW"),
                                                            new VarDec[0],
                                                            new ReturnStmt(new LhsExp(new FieldAccessLhs(new ThisLhs(),
                                                                                                         new Variable("w")))))
                                   });
    }

    @Test
    public void testWellTypedMethodCall() {
        assertWellTyped(mkProgram(stmts(new NewStmt(new VarDec(new ClassType(WITH_METHOD_CLASS_NAME, new Type[0]),
                                                               new Variable("w")),
                                                    WITH_METHOD_CLASS_NAME,
                                                    new Type[0],
                                                    new Exp[] { new IntExp(0) }),
                                        new MethodCallStmt(new VarDec(new IntType(), new Variable("x")),
                                                           new LhsExp(new VariableLhs(new Variable("w"))),
                                                           new MethodName("getW"),
                                                           new Type[0],
                                                           new Exp[0])),
                                  withMethod()));
    }

    @Test
    public void testIllTypedMethodCallWrongParams() {
        assertIllTyped(mkProgram(stmts(new NewStmt(new VarDec(new ClassType(WITH_METHOD_CLASS_NAME, new Type[0]),
                                                              new Variable("w")),
                                                   WITH_METHOD_CLASS_NAME,
                                                   new Type[0],
                                                   new Exp[] { new IntExp(0) }),
                                       new MethodCallStmt(new VarDec(new IntType(), new Variable("x")),
                                                          new LhsExp(new VariableLhs(new Variable("w"))),
                                                          new MethodName("getW"),
                                                          new Type[0],
                                                          new Exp[] { new IntExp(0) })),
                                 withMethod()));
    }

    @Test
    public void testIllTypedMethodCallWrongReturnType() {
        assertIllTyped(mkProgram(stmts(new NewStmt(new VarDec(new ClassType(WITH_METHOD_CLASS_NAME, new Type[0]),
                                                              new Variable("w")),
                                                   WITH_METHOD_CLASS_NAME,
                                                   new Type[0],
                                                   new Exp[] { new IntExp(0) }),
                                       new MethodCallStmt(new VarDec(new ClassType(WITH_METHOD_CLASS_NAME, new Type[0]),
                                                                     new Variable("x")),
                                                          new LhsExp(new VariableLhs(new Variable("w"))),
                                                          new MethodName("getW"),
                                                          new Type[0],
                                                          new Exp[0])),
                                 withMethod()));
    }

    // class GenericMethod<> {
    //   init() {}
    //   <A> A id(A a) {
    //     return a;
    //   }
    // }
    public static ClassDefinition genericMethod() {
        return new ClassDefinition(GENERIC_METHOD_CLASS_NAME,
                                   new TypeVariable[0],
                                   null,
                                   new VarDec[0],
                                   new Constructor(new VarDec[0], new EmptyStmt()),
                                   new MethodDefinition[] {
                                       new MethodDefinition(false,
                                                            new TypeVariable[] { new TypeVariable("A") },
                                                            new TypeVariable("A"),
                                                            new MethodName("id"),
                                                            new VarDec[] {
                                                                new VarDec(new TypeVariable("A"), new Variable("a"))
                                                            },
                                                            new ReturnStmt(new LhsExp(new VariableLhs(new Variable("a")))))
                                   });
    } // genericMethod

    @Test
    public void testGenericMethod() {
        assertWellTyped(mkProgram(stmts(new NewStmt(new VarDec(new ClassType(GENERIC_METHOD_CLASS_NAME, new Type[0]),
                                                               new Variable("x")),
                                                    GENERIC_METHOD_CLASS_NAME,
                                                    new Type[0],
                                                    new Exp[0]),
                                        new MethodCallStmt(new VarDec(new IntType(), new Variable("y")),
                                                           new LhsExp(new VariableLhs(new Variable("x"))),
                                                           new MethodName("id"),
                                                           new Type[] { new IntType() },
                                                           new Exp[] { new IntExp(0) })),
                                  genericMethod()));
    }

    @Test
    public void testGenericMethodWrongReturnType() {
        assertIllTyped(mkProgram(stmts(new NewStmt(new VarDec(new ClassType(GENERIC_METHOD_CLASS_NAME, new Type[0]),
                                                              new Variable("x")),
                                                   GENERIC_METHOD_CLASS_NAME,
                                                   new Type[0],
                                                   new Exp[0]),
                                       new MethodCallStmt(new VarDec(new ClassType(GENERIC_METHOD_CLASS_NAME, new Type[0]), new Variable("y")),
                                                          new LhsExp(new VariableLhs(new Variable("x"))),
                                                          new MethodName("id"),
                                                          new Type[] { new IntType() },
                                                          new Exp[] { new IntExp(0) })),
                                 genericMethod()));
    }

    @Test
    public void testGenericMethodWrongParameterType() {
        assertIllTyped(mkProgram(stmts(new NewStmt(new VarDec(new ClassType(GENERIC_METHOD_CLASS_NAME, new Type[0]),
                                                              new Variable("x")),
                                                   GENERIC_METHOD_CLASS_NAME,
                                                   new Type[0],
                                                   new Exp[0]),
                                       new MethodCallStmt(new VarDec(new IntType(), new Variable("y")),
                                                          new LhsExp(new VariableLhs(new Variable("x"))),
                                                          new MethodName("id"),
                                                          new Type[] { new ClassType(GENERIC_METHOD_CLASS_NAME, new Type[0]) },
                                                          new Exp[] { new IntExp(0) })),
                                 genericMethod()));
    }

    // class GenericClass<A> {
    //   A a;
    //   init(A a) {
    //     this.a = a;
    //   }
    //   <> A getA() {
    //     return this.a;
    //   }
    // }
    public static ClassDefinition genericClass() {
        return new ClassDefinition(GENERIC_CLASS_CLASS_NAME,
                                   new TypeVariable[] { new TypeVariable("A") },
                                   null,
                                   new VarDec[] { new VarDec(new TypeVariable("A"), new Variable("a")) },
                                   new Constructor(new VarDec[] { new VarDec(new TypeVariable("A"), new Variable("a")) },
                                                   new AssignStmt(new FieldAccessLhs(new ThisLhs(),
                                                                                     new Variable("a")),
                                                                  new LhsExp(new VariableLhs(new Variable("a"))))),
                                   new MethodDefinition[] {
                                       new MethodDefinition(false,
                                                            new TypeVariable[0],
                                                            new TypeVariable("A"),
                                                            new MethodName("getA"),
                                                            new VarDec[0],
                                                            new ReturnStmt(new LhsExp(new FieldAccessLhs(new ThisLhs(),
                                                                                                         new Variable("a")))))
                                   });
    }

    @Test
    public void testGenericClass() {
        // GenericClass<int> g = new GenericClass<int>(7);
        // int i = g.getA<>();
        assertWellTyped(mkProgram(stmts(new NewStmt(new VarDec(new ClassType(GENERIC_CLASS_CLASS_NAME,
                                                                             new Type[]{ new IntType() }),
                                                               new Variable("g")),
                                                    GENERIC_CLASS_CLASS_NAME,
                                                    new Type[]{ new IntType() },
                                                    new Exp[] { new IntExp(7) }),
                                        new MethodCallStmt(new VarDec(new IntType(), new Variable("i")),
                                                           new LhsExp(new VariableLhs(new Variable("g"))),
                                                           new MethodName("getA"),
                                                           new Type[0],
                                                           new Exp[0])),
                                  genericClass()));
    }

    @Test
    public void testGenericClassWrongParameter() {
        // GenericClass<int> g = new GenericClass<GenericClass<int>>(7);
        // int i = g.getA<>();
        assertIllTyped(mkProgram(stmts(new NewStmt(new VarDec(new ClassType(GENERIC_CLASS_CLASS_NAME,
                                                                            new Type[]{ new IntType() }),
                                                              new Variable("g")),
                                                   GENERIC_CLASS_CLASS_NAME,
                                                   new Type[]{
                                                       new ClassType(GENERIC_CLASS_CLASS_NAME,
                                                                     new Type[]{ new IntType() })
                                                   },
                                                   new Exp[] { new IntExp(7) }),
                                       new MethodCallStmt(new VarDec(new IntType(), new Variable("i")),
                                                          new LhsExp(new VariableLhs(new Variable("g"))),
                                                          new MethodName("getA"),
                                                          new Type[0],
                                                          new Exp[0])),
                                 genericClass()));
    }

    // class Pair<A, B> {
    //   A first;
    //   B second;
    //   init(A first, B second) {
    //     this.first = first;
    //     this.second = second;
    //   }
    // }
    public static ClassDefinition pairClass() {
        return new ClassDefinition(PAIR_CLASS_NAME,
                                   new TypeVariable[] {
                                       new TypeVariable("A"),
                                       new TypeVariable("B")
                                   },
                                   null,
                                   new VarDec[] {
                                       new VarDec(new TypeVariable("A"), new Variable("first")),
                                       new VarDec(new TypeVariable("B"), new Variable("second"))
                                   },
                                   new Constructor(new VarDec[] {
                                           new VarDec(new TypeVariable("A"), new Variable("first")),
                                           new VarDec(new TypeVariable("B"), new Variable("second"))
                                       },
                                       stmts(new AssignStmt(new FieldAccessLhs(new ThisLhs(),
                                                                               new Variable("first")),
                                                            new LhsExp(new VariableLhs(new Variable("first")))),
                                             new AssignStmt(new FieldAccessLhs(new ThisLhs(),
                                                                               new Variable("second")),
                                                            new LhsExp(new VariableLhs(new Variable("second")))))),
                                   new MethodDefinition[0]);
    }

    // class WithFirst<A> {
    //   A first;
    //   init(A first) {
    //     this.first = first;
    //   }
    //   <B> Pair<A, B> withSecond(B second) {
    //     Pair<A, B> result = new Pair<A, B>(this.first, second);
    //     return result;
    //   }
    // }
    public static ClassDefinition withFirstClass() {
        return new ClassDefinition(WITH_FIRST_CLASS_NAME,
                                   new TypeVariable[]{ new TypeVariable("A") },
                                   null,
                                   new VarDec[]{ new VarDec(new TypeVariable("A"), new Variable("first")) },
                                   new Constructor(new VarDec[]{ new VarDec(new TypeVariable("A"), new Variable("first")) },
                                                   new AssignStmt(new FieldAccessLhs(new ThisLhs(),
                                                                                     new Variable("first")),
                                                                  new LhsExp(new VariableLhs(new Variable("first"))))),
                                   new MethodDefinition[] {
                                       new MethodDefinition(false,
                                                            new TypeVariable[]{ new TypeVariable("B") },
                                                            new ClassType(PAIR_CLASS_NAME,
                                                                          new Type[]{
                                                                              new TypeVariable("A"),
                                                                              new TypeVariable("B")
                                                                          }),
                                                            new MethodName("withSecond"),
                                                            new VarDec[]{ new VarDec(new TypeVariable("B"), new Variable("second")) },
                                                            stmts(new NewStmt(new VarDec(new ClassType(PAIR_CLASS_NAME,
                                                                                                       new Type[]{
                                                                                                           new TypeVariable("A"),
                                                                                                           new TypeVariable("B")
                                                                                                       }),
                                                                                         new Variable("result")),
                                                                              PAIR_CLASS_NAME,
                                                                              new Type[]{
                                                                                  new TypeVariable("A"),
                                                                                  new TypeVariable("B")
                                                                              },
                                                                              new Exp[] {
                                                                                  new LhsExp(new FieldAccessLhs(new ThisLhs(),
                                                                                                                new Variable("first"))),
                                                                                  new LhsExp(new VariableLhs(new Variable("second")))
                                                                              }),
                                                                  new ReturnStmt(new LhsExp(new VariableLhs(new Variable("result"))))))
                                   });
    }

    @Test
    public void testGenericClassAndMethod() {
        // WithFirst<int> foo = new WithFirst<int>(7);
        // Pair<int, int> bar = foo.withSecond<int>(8);
        assertWellTyped(mkProgram(stmts(new NewStmt(new VarDec(new ClassType(WITH_FIRST_CLASS_NAME,
                                                                             new Type[] { new IntType() }),
                                                               new Variable("foo")),
                                                    WITH_FIRST_CLASS_NAME,
                                                    new Type[] { new IntType() },
                                                    new Exp[] { new IntExp(7) }),
                                        new MethodCallStmt(new VarDec(new ClassType(PAIR_CLASS_NAME,
                                                                                    new Type[] {
                                                                                        new IntType(),
                                                                                        new IntType()
                                                                                    }),
                                                                      new Variable("bar")),
                                                           new LhsExp(new VariableLhs(new Variable("foo"))),
                                                           new MethodName("withSecond"),
                                                           new Type[] { new IntType() },
                                                           new Exp[] { new IntExp(8) })),
                                  pairClass(),
                                  withFirstClass()));
    }
// TODO: most tests are missing, particularly those dealing with ill-typed programs
} // TypecheckerClassTest
