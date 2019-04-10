package vtables_example.typechecker;

import vtables_example.syntax.*;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TypecheckerExpTest {
    // ---BEGIN CONSTANTS---
    //
    // class Empty {
    //   init() {}
    // }
    public static final ClassName EMPTY_CLASS_NAME =
        new ClassName("Empty");
    public static final ClassDefinition EMPTY_CLASS =
        new ClassDefinition(EMPTY_CLASS_NAME,
                            null,
                            new VarDec[0],
                            new Constructor(new VarDec[0],
                                            new EmptyStmt()),
                            new MethodDefinition[0]);

    // class Field {
    //   int x;
    //   init() {}
    // }
    public static final ClassName FIELD_CLASS_NAME =
        new ClassName("Field");
    public static final ClassDefinition FIELD_CLASS =
        new ClassDefinition(FIELD_CLASS_NAME,
                            null,
                            new VarDec[] {
                                new VarDec(new IntType(), new Variable("x"))
                            },
                            new Constructor(new VarDec[0],
                                            new EmptyStmt()),
                            new MethodDefinition[0]);
    // ---END CONSTANTS---
    
    public static Typechecker mkChecker() {
        return mkChecker(new ClassDefinition[0]);
    }
    
    public static Typechecker mkChecker(final ClassDefinition[] classes) {
        try {
            return new Typechecker(Typechecker.classMapping(classes));
        } catch (final TypeErrorException e) {
            fail("Malformed test; duplicate class name");
            return null;
        }
    }
                                   
    public void assertExpType(final Typechecker typechecker,
                              final TypeEnvironment env,
                              final Exp exp,
                              final Type expectedType) {
        try {
            final Type actualType = typechecker.typeofExp(env, exp);
            if (expectedType == null) {
                fail("Expected ill-typed; got: " + actualType);
            } else {
                assertEquals(expectedType, actualType);
            }
        } catch (final TypeErrorException e) {
            if (expectedType != null) {
                fail("Expected " + expectedType + "; got ill-typed; " + e.getMessage());
            }
        }
    }

    public static TypeEnvironment initialEnv(final VarDec[] params,
                                             final ClassName onClass) {
        try {
            return TypeEnvironment.initialEnv(params, onClass);
        } catch (final TypeErrorException e) {
            fail("Malformed type environment");
            return null;
        }
    }
    
    @Test
    public void testIntExp() {
        assertExpType(mkChecker(),
                      initialEnv(new VarDec[0],
                                 null),
                      new IntExp(0),
                      new IntType());
    }

    @Test
    public void testThisExp() {
        assertExpType(mkChecker(new ClassDefinition[] { EMPTY_CLASS }),
                      initialEnv(new VarDec[0],
                                 EMPTY_CLASS_NAME),
                      new LhsExp(new ThisLhs()),
                      new ClassType(EMPTY_CLASS_NAME));
    }

    @Test
    public void testFieldAccess() {
        assertExpType(mkChecker(new ClassDefinition[] { FIELD_CLASS }),
                      initialEnv(new VarDec[0],
                                 FIELD_CLASS_NAME),
                      new LhsExp(new FieldAccessLhs(new ThisLhs(),
                                                    new Variable("x"))),
                      new IntType());
    }

    @Test
    public void testVariable() {
        assertExpType(mkChecker(),
                      initialEnv(new VarDec[] {
                              new VarDec(new IntType(), new Variable("y"))
                          },
                          null),
                      new LhsExp(new VariableLhs(new Variable("y"))),
                      new IntType());
    }

    @Test
    public void testVariableNotInScope() {
        assertExpType(mkChecker(),
                      initialEnv(new VarDec[0], null),
                      new LhsExp(new VariableLhs(new Variable("x"))),
                      null);
    }
    
    @Test
    public void testFieldAccessNonClass() {
        assertExpType(mkChecker(),
                      initialEnv(new VarDec[] {
                              new VarDec(new IntType(), new Variable("y"))
                          },
                          null),
                      new LhsExp(new FieldAccessLhs(new VariableLhs(new Variable("y")),
                                                    new Variable("x"))),
                      null);
    }

    @Test
    public void testFieldAccessNoSuchField() {
        assertExpType(mkChecker(new ClassDefinition[] { FIELD_CLASS }),
                      initialEnv(new VarDec[0],
                                 FIELD_CLASS_NAME),
                      new LhsExp(new FieldAccessLhs(new ThisLhs(),
                                                    new Variable("y"))),
                      null);
    }
} // TypecheckerExpTest

