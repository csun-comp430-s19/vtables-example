package vtables_example.typechecker;

import vtables_example.syntax.*;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Typechecker {
    public final Map<ClassName, ClassDefinition> classes;

    public Typechecker(final Map<ClassName, ClassDefinition> classes) {
        this.classes = classes;
    }

    public ClassDefinition getClass(final ClassName name) throws TypeErrorException {
        final ClassDefinition result = classes.get(name);
        if (result == null) {
            throw new TypeErrorException("No such class defined: " + name);
        } else {
            return result;
        }
    } // getClass

    public static <A> A deadCode() throws TypeErrorException {
        assert(false);
        throw new TypeErrorException("Should be unreachable");
    } // deadCode
    
    public Type typeofField(final ClassName onClass,
                            final Variable fieldName) throws TypeErrorException {
        if (onClass == null) {
            throw new TypeErrorException("No instance variable defined: " + fieldName);
        } else {
            final ClassDefinition classDef = getClass(onClass);
            for (final VarDec vardec : classDef.instanceVariables) {
                if (vardec.variable.equals(fieldName)) {
                    return vardec.type;
                }
            }
            // if we get here, this class lacks this instance variable
            // see if a parent has it
            return typeofField(classDef.extendsName, fieldName);
        }
    } // typeofField
    
    public Type typeofLhs(final TypeEnvironment env,
                          final Lhs lhs) throws TypeErrorException {
        if (lhs instanceof VariableLhs) {
            final Variable variable = ((VariableLhs)lhs).variable;
            return env.lookup(variable);
        } else if (lhs instanceof FieldAccessLhs) {
            final FieldAccessLhs asField = (FieldAccessLhs)lhs;
            final Type lhsType = typeofLhs(env, asField.lhs);
            if (lhsType instanceof ClassType) {
                final ClassName accessName = ((ClassType)lhsType).name;
                asField.setLhsClass(accessName);
                return typeofField(accessName, asField.field);
            } else {
                throw new TypeErrorException("Expected class type; got: " + lhsType);
            }
        } else if (lhs instanceof ThisLhs) {
            return env.thisType();
        } else {
            return deadCode();
        }
    } // typeofLhs
    
    public Type typeofExp(final TypeEnvironment env,
                          final Exp exp) throws TypeErrorException {
        if (exp instanceof IntExp) {
            return new IntType();
        } else if (exp instanceof LhsExp) {
            return typeofLhs(env, ((LhsExp)exp).lhs);
        } else {
            return deadCode();
        }
    } // typeofExp

    public void typesOk(final Type baseType, final Type subType) throws TypeErrorException {
        if (!baseType.equals(subType)) {
            // see if subType is a subtype of base type
            if (baseType instanceof ClassType && subType instanceof ClassType) {
                final ClassName subName = ((ClassType)subType).name;
                final ClassDefinition subClassDef = getClass(subName);
                if (subClassDef.extendsName != null) {
                    typesOk(baseType, new ClassType(subClassDef.extendsName));
                } else {
                    throw new TypeErrorException(subType.toString() + " is not a subtype of " + baseType.toString());
                }
            } else {
                throw new TypeErrorException("Base type " + baseType + " is not comparable to " + subType);
            }
        }
    } // typesOk
    
    public void paramTypesOk(final TypeEnvironment env,
                             final VarDec[] formalParams,
                             final Exp[] actualParams) throws TypeErrorException {
        if (formalParams.length != actualParams.length) {
            throw new TypeErrorException("Arity mismatch in parameters; expected: " +
                                         formalParams.length +
                                         "; received: " +
                                         actualParams.length);
        } else {
            for (int index = 0; index < formalParams.length; index++) {
                final Type expected = formalParams[index].type;
                final Type actual = typeofExp(env, actualParams[index]);
                typesOk(expected, actual);
            }
        }
    } // paramTypesOk

    // returns null if it couldn't find it
    public MethodDefinition findMethodDirect(final ClassName onClass,
                                             final MethodName methodName) throws TypeErrorException {
        final ClassDefinition classDef = getClass(onClass);
        for (final MethodDefinition methodDef : classDef.methods) {
            if (methodDef.name.equals(methodName)) {
                return methodDef;
            }
        }
        return null;
    } // findMethodDirect
    
    public MethodDefinition findMethod(final ClassName onClass,
                                       final MethodName methodName) throws TypeErrorException {
        if (onClass == null) {
            throw new TypeErrorException("No such method: " + methodName);
        }

        final MethodDefinition result = findMethodDirect(onClass, methodName);
        if (result == null) {
            final ClassDefinition classDef = getClass(onClass);
            // Not on me; see if it's on my parent
            return findMethod(classDef.extendsName, methodName);
        } else {
            return result;
        }
    } // findMethod
    
    public TypeEnvironment typecheckNewStmt(final TypeEnvironment env,
                                            final NewStmt stmt) throws TypeErrorException {
        final ClassDefinition classDef = getClass(stmt.name);
        paramTypesOk(env, classDef.constructor.params, stmt.params);
        typesOk(stmt.vardec.type, new ClassType(stmt.name));
        return env.addVariable(stmt.vardec);
    } // typecheckNewStmt

    public TypeEnvironment typecheckMethodCallStmt(final TypeEnvironment env,
                                                   final MethodCallStmt stmt) throws TypeErrorException {
        final Type expType = typeofExp(env, stmt.exp);
        if (expType instanceof ClassType) {
            final ClassName onClass = ((ClassType)expType).name;
            stmt.setOnClass(onClass);
            final MethodDefinition calling = findMethod(onClass, stmt.name);
            paramTypesOk(env, calling.params, stmt.params);
            typesOk(stmt.vardec.type, calling.returnType);
            return env.addVariable(stmt.vardec);
        } else {
            throw new TypeErrorException("Expected class type; received; " + expType);
        }
    } // typecheckMethodCallStmt

    public void typecheckPrintStmt(final TypeEnvironment env,
                                   final PrintStmt stmt) throws TypeErrorException {
        final Type printType = typeofExp(env, stmt.exp);
        if (!printType.equals(new IntType())) {
            throw new TypeErrorException("print can only print integers; got: " + printType);
        }
    } // typecheckPrintStmt

    public void typecheckReturnStmt(final TypeEnvironment env,
                                    final Type returnType, // null if return is not ok
                                    final ReturnStmt stmt) throws TypeErrorException {
        assert(returnType != null);
        typesOk(returnType, typeofExp(env, stmt.exp));
    } // typecheckReturnStmt

    public void typecheckAssignStmt(final TypeEnvironment env,
                                    final AssignStmt stmt) throws TypeErrorException {
        final Type lhsType = typeofLhs(env, stmt.lhs);
        final Type expType = typeofExp(env, stmt.exp);
        typesOk(lhsType, expType);
    } // typecheckAssignStmt

    public void typecheckSuperStmt(final TypeEnvironment env,
                                   final VarDec[] superParams,
                                   final SuperStmt stmt) throws TypeErrorException {
        assert(superParams != null);
        paramTypesOk(env, superParams, stmt.params);
    } // typecheckSuperStmt

    private static void extractSequenceStmts(final List<Stmt> result, final Stmt stmt) {
        if (stmt instanceof SequenceStmt) {
            final SequenceStmt asSeq = (SequenceStmt)stmt;
            extractSequenceStmts(result, asSeq.first);
            extractSequenceStmts(result, asSeq.second);
        } else {
            result.add(stmt);
        }
    } // extractSequenceStmts
    
    public static List<Stmt> extractSequenceStmts(final Stmt stmt) {
        final List<Stmt> result = new ArrayList<Stmt>();
        extractSequenceStmts(result, stmt);
        return result;
    } // extractSequenceStmts

    public static boolean containsNoReturns(final List<Stmt> statements, final int endPos) {
        int curPos = 0;
        for (final Stmt stmt : statements) {
            if (curPos > endPos) {
                break;
            }
            assert(!(stmt instanceof SequenceStmt));
            if (stmt instanceof ReturnStmt) {
                return false;
            }
            curPos++;
        }
        return true;
    } // containsNoReturns

    public static boolean containsNoSupers(final List<Stmt> statements, final int startPos) {
        int curPos = 0;
        for (final Stmt stmt : statements) {
            assert(!(stmt instanceof SequenceStmt));
            if (curPos++ < startPos) {
                continue;
            }
            if (stmt instanceof SuperStmt) {
                return false;
            }
        }
        return true;
    } // containsNoSupers
    
    public static void superReturnOkInConstructor(final boolean isBaseClass,
                                                  final Stmt stmt) throws TypeErrorException {
        final List<Stmt> statements = extractSequenceStmts(stmt);
        if (isBaseClass && !containsNoSupers(statements, 0)) {
            throw new TypeErrorException("base classes cannot contain super");
        }
        if (!isBaseClass &&
            (statements.size() == 0 ||
             !(statements.get(0) instanceof SuperStmt))) {
            throw new TypeErrorException("super needs to be first in subclass constructor");
        }
        if (!containsNoReturns(statements, statements.size() - 1)) {
            throw new TypeErrorException("return in constructor");
        }
    } // superReturnOkInConstructor

    public static void superReturnOkInMethod(final Stmt stmt) throws TypeErrorException {
        final List<Stmt> statements = extractSequenceStmts(stmt);
        final int numStatements = statements.size();
        if (numStatements == 0 ||
            !(statements.get(numStatements - 1) instanceof ReturnStmt)) {
            throw new TypeErrorException("Missing return at method end");
        }
        if (!containsNoReturns(statements, statements.size() - 2)) {
            throw new TypeErrorException("Early return in method");
        }
        if (!containsNoSupers(statements, 0)) {
            throw new TypeErrorException("methods cannot contain super");
        }
    } // superReturnOkInMethod

    public static void superReturnOkInEntryPoint(final Stmt stmt) throws TypeErrorException {
        final List<Stmt> statements = extractSequenceStmts(stmt);
        if (!containsNoReturns(statements, statements.size() - 1)) {
            throw new TypeErrorException("return in entry point");
        }
        if (!containsNoSupers(statements, 0)) {
            throw new TypeErrorException("super in entry point");
        }
    } // superReturnOkInEntryPoint
    
    public TypeEnvironment typecheckSequenceStmt(final TypeEnvironment env,
                                                 final Type returnType,         // null if return is not ok
                                                 final VarDec[] superParams, // null if not expecting super
                                                 final SequenceStmt stmt) throws TypeErrorException {
        final TypeEnvironment newEnv = typecheckStmt(env, returnType, superParams, stmt.first);
        return typecheckStmt(newEnv, returnType, superParams, stmt.second);
    } // typecheckSequenceStmt
    
    public TypeEnvironment typecheckStmt(final TypeEnvironment env,
                                         final Type returnType,      // null if return is not ok
                                         final VarDec[] superParams, // null if not expecting super
                                         final Stmt stmt) throws TypeErrorException {
        if (stmt instanceof NewStmt) {
            return typecheckNewStmt(env, (NewStmt)stmt);
        } else if (stmt instanceof MethodCallStmt) {
            return typecheckMethodCallStmt(env, (MethodCallStmt)stmt);
        } else if (stmt instanceof PrintStmt) {
            typecheckPrintStmt(env, (PrintStmt)stmt);
            return env;
        } else if (stmt instanceof ReturnStmt) {
            typecheckReturnStmt(env, returnType, (ReturnStmt)stmt);
            return env;
        } else if (stmt instanceof AssignStmt) {
            typecheckAssignStmt(env, (AssignStmt)stmt);
            return env;
        } else if (stmt instanceof SuperStmt) {
            typecheckSuperStmt(env, superParams, (SuperStmt)stmt);
            return env;
        } else if (stmt instanceof SequenceStmt) {
            return typecheckSequenceStmt(env, returnType, superParams, (SequenceStmt)stmt);
        } else if (stmt instanceof EmptyStmt) {
            return env;
        } else {
            return deadCode();
        }
    } // typecheckStmt

    public static void noDuplicates(final VarDec[] params) throws TypeErrorException {
        final Set<Variable> seen = new HashSet<Variable>();
        for (final VarDec current : params) {
            if (seen.contains(current.variable)) {
                throw new TypeErrorException("Duplicate variable: " + current.variable);
            }
            seen.add(current.variable);
        }
    } // noDuplicates

    // if I'm virtual, all my superclasses need to be virtual
    // similarly, if I'm not virtual, all my superclasses need to not be virtual
    public void virtualOk(final ClassName onClass,
                          final boolean isVirtual,
                          final MethodName methodName) throws TypeErrorException {
        if (onClass != null) {
            final MethodDefinition current = findMethodDirect(onClass, methodName);
            if (current != null && current.isVirtual != isVirtual) {
                throw new TypeErrorException("virtual disagreement on " + methodName);
            } else {
                // Either I don't have the method, or virtual agreed.  Make sure
                // the parent agrees.
                final ClassDefinition classDef = getClass(onClass);
                virtualOk(classDef.extendsName, isVirtual, methodName);
            }
        }
    } // virtualOk

    public VarDec[] getSuperParams(final ClassName forClass) throws TypeErrorException {
        final ClassDefinition classDef = getClass(forClass);
        if (classDef.extendsName != null) {
            return getClass(classDef.extendsName).constructor.params;
        } else {
            return null;
        }
    } // getSuperParams
    
    public void typecheckMethod(final ClassName onClass,
                                final MethodDefinition methodDef) throws TypeErrorException {
        virtualOk(onClass, methodDef.isVirtual, methodDef.name);
        noDuplicates(methodDef.params);
        superReturnOkInMethod(methodDef.body);
        typecheckStmt(TypeEnvironment.initialEnv(methodDef.params, onClass),
                      methodDef.returnType,
                      null,
                      methodDef.body);
    } // typecheckMethod
    
    public void typecheckConstructor(final ClassName onClass,
                                     final Constructor constructor) throws TypeErrorException {
        final ClassDefinition classDef = getClass(onClass);
        noDuplicates(constructor.params);
        superReturnOkInConstructor(classDef.extendsName == null, constructor.body);
        typecheckStmt(TypeEnvironment.initialEnv(constructor.params, onClass),
                      null,
                      getSuperParams(onClass),
                      constructor.body);
    } // typecheckConstructor
    
    public static void noDuplicateMethodNames(final MethodDefinition[] methods) throws TypeErrorException {
        final Set<MethodName> seen = new HashSet<MethodName>();
        for (final MethodDefinition method : methods) {
            if (seen.contains(method.name)) {
                throw new TypeErrorException("Duplicate method name: " + method.name);
            }
            seen.add(method.name);
        }
    } // noDuplicateMethodNames

    public void noCyclicInheritance(final ClassName className) throws TypeErrorException {
        final Set<ClassName> seen = new HashSet<ClassName>();
        ClassName current = className;

        while (current != null) {
            if (seen.contains(current)) {
                throw new TypeErrorException("Cyclic inheritance on " + className);
            }
            seen.add(current);
            current = getClass(className).extendsName;
        }
    } // noCyclicInheritance

    // checks that subclasses don't redefined parent class instance variables
    public void instanceVariablesOk(final Set<Variable> seen, final ClassName current) throws TypeErrorException {
        if (current != null) {
            final ClassDefinition classDef = getClass(current);
            for (final VarDec param : classDef.instanceVariables) {
                if (seen.contains(param.variable)) {
                    throw new TypeErrorException("Instance variable seen: " + param.variable);
                }
                seen.add(param.variable);
            }
            instanceVariablesOk(seen, classDef.extendsName);
        }
    } // instanceVariablesOk

    // checks that subclasses don't redefined parent class instance variables
    public void instanceVariablesOk(final ClassName current) throws TypeErrorException {
        instanceVariablesOk(new HashSet<Variable>(), current);
    } // instanceVariablesOk
    
    public void typecheckClass(final ClassName className) throws TypeErrorException {
        final ClassDefinition classDef = getClass(className);
        noDuplicateMethodNames(classDef.methods);
        noDuplicates(classDef.instanceVariables);
        instanceVariablesOk(className);
        typecheckConstructor(className, classDef.constructor);
        for (final MethodDefinition methodDef : classDef.methods) {
            typecheckMethod(className, methodDef);
        }
    } // typecheckClass

    public void typecheckAllClasses() throws TypeErrorException {
        // cyclic checks go first, as all downstream code assumes acyclic
        // inheritance
        for (final ClassName className : classes.keySet()) {
            noCyclicInheritance(className);
        }
        for (final ClassName className : classes.keySet()) {
            typecheckClass(className);
        }
    } // typecheckAllClasses

    public static Map<ClassName, ClassDefinition> classMapping(final ClassDefinition[] classes) throws TypeErrorException {
        final Map<ClassName, ClassDefinition> mapping = new HashMap<ClassName, ClassDefinition>();
        for (final ClassDefinition classDef : classes) {
            if (mapping.containsKey(classDef.myName)) {
                throw new TypeErrorException("Duplicate class name: " + classDef.myName);
            }
            mapping.put(classDef.myName, classDef);
        }
        return mapping;
    } // classMapping
    
    public static void typecheckProgram(final Program program) throws TypeErrorException {
        final Typechecker typechecker = new Typechecker(classMapping(program.classes));
        typechecker.typecheckAllClasses();
        typechecker.typecheckStmt(TypeEnvironment.initialEnv(new VarDec[0], null),
                                  null,
                                  null,
                                  program.entryPoint);
    } // typecheckProgram
} // Typechecker
