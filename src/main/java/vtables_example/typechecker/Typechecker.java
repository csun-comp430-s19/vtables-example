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

    public static Set<TypeVariable> asSet(final TypeVariable[] typeVariables) throws TypeErrorException {
        final Set<TypeVariable> result = new HashSet<TypeVariable>();
        for (final TypeVariable typeVariable : typeVariables) {
            if (result.contains(typeVariable)) {
                throw new TypeErrorException("Duplicate type variable introduced: " + typeVariable);
            }
            result.add(typeVariable);
        }
        return result;
    } // asSet
    
    public Type typeofField(final ClassType onClass,
                            final Variable fieldName) throws TypeErrorException {
        final ClassDefinition abstractedDef = getClass(onClass.name);
        final ClassDefinition specializedDef = TypeRewriter.rewriteClassDefinition(abstractedDef,
                                                                                   onClass.types);
        for (final VarDec vardec : specializedDef.instanceVariables) {
            if (vardec.variable.equals(fieldName)) {
                return vardec.type;
            }
        }

        // if we get here, this class lacks this instance variable
        // try with a parent
        return typeofField(asSupertype(onClass), fieldName);
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
                final ClassType asClass = (ClassType)lhsType;
                asField.setLhsClass(asClass.name);
                return typeofField(asClass, asField.field);
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

    private ClassType asSupertype(final ClassType type) throws TypeErrorException {
        final ClassDefinition abstracted = getClass(type.name);
        if (abstracted.doesExtend == null) {
            throw new TypeErrorException("Has no supertype: " + type);
        }
        final ClassDefinition specialized = TypeRewriter.rewriteClassDefinition(abstracted,
                                                                                type.types);
        return new ClassType(specialized.doesExtend.extendsName,
                             specialized.doesExtend.types);
    } // asSupertype
    
    public void typesOk(final Type baseType, final Type subType) throws TypeErrorException {
        if (!baseType.equals(subType)) {
            // see if subtyping is involved
            if (baseType instanceof ClassType && subType instanceof ClassType) {
                typesOk(baseType, asSupertype((ClassType)subType));
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
                env.typeInScope(formalParams[index].type);
                final Type expected = formalParams[index].type;
                final Type actual = typeofExp(env, actualParams[index]);
                typesOk(expected, actual);
            }
        }
    } // paramTypesOk

    // returns null if it couldn't find it
    public MethodDefinition findMethodDirect(final ClassDefinition onClass,
                                             final MethodName methodName) {
        for (final MethodDefinition methodDef : onClass.methods) {
            if (methodDef.name.equals(methodName)) {
                return methodDef;
            }
        }
        return null;
    } // findMethodDirect

    // returns a specialized MethodDefinition
    public MethodDefinition findMethod(final ClassType onClass,
                                       final MethodName methodName) throws TypeErrorException {
        final ClassDefinition abstractedClass = getClass(onClass.name);
        final ClassDefinition specializedClass = TypeRewriter.rewriteClassDefinition(abstractedClass,
                                                                                     onClass.types);
        final MethodDefinition result = findMethodDirect(specializedClass, methodName);
        if (result == null) {
            // Not on me; see if it's on my parent
            return findMethod(asSupertype(onClass), methodName);
        } else {
            return result;
        }
    } // findMethod        
        
    public TypeEnvironment typecheckNewStmt(final TypeEnvironment env,
                                            final NewStmt stmt) throws TypeErrorException {
        final ClassDefinition abstractedClassDef = getClass(stmt.name);
        env.typeInScope(stmt.vardec.type);
        env.typesInScope(stmt.types);
        final ClassDefinition specializedClassDef = TypeRewriter.rewriteClassDefinition(abstractedClassDef,
                                                                                        stmt.types);
        paramTypesOk(env, specializedClassDef.constructor.params, stmt.params);
        typesOk(stmt.vardec.type, new ClassType(specializedClassDef.myName, stmt.types));
        return env.addVariable(stmt.vardec);
    } // typecheckNewStmt

    public TypeEnvironment typecheckMethodCallStmt(final TypeEnvironment env,
                                                   final MethodCallStmt stmt) throws TypeErrorException {
        final Type expType = typeofExp(env, stmt.exp);
        if (expType instanceof ClassType) {
            final ClassType onClass = (ClassType)expType;
            env.typeInScope(stmt.vardec.type);
            env.typesInScope(stmt.types);
            stmt.setOnClass(onClass.name);
            final MethodDefinition abstractedCalling = findMethod(onClass, stmt.name);
            final MethodDefinition specializedCalling = TypeRewriter.rewriteMethodDefinitionToplevel(abstractedCalling,
                                                                                                     stmt.types);
            paramTypesOk(env, specializedCalling.params, stmt.params);
            typesOk(stmt.vardec.type, specializedCalling.returnType);
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
        final ClassDefinition classDef = getClass(onClass);
        final MethodDefinition current = findMethodDirect(classDef, methodName);
        if (current != null && current.isVirtual != isVirtual) {
            throw new TypeErrorException("virtual disagreement on " + methodName);
        } else {
            // Either I don't have the method, or virtual agreed.  Make sure
            // the parent agrees.
            if (classDef.doesExtend != null) {
                virtualOk(classDef.doesExtend.extendsName, isVirtual, methodName);
            }
        }
    } // virtualOk

    public VarDec[] getSuperParams(final ClassType thisClass) throws TypeErrorException {
        final ClassDefinition selfAbstracted = getClass(thisClass.name);
        if (selfAbstracted.doesExtend != null) {
            final ClassDefinition selfSpecialized = TypeRewriter.rewriteClassDefinition(selfAbstracted,
                                                                                        thisClass.types);
            final ClassDefinition superAbstracted = getClass(selfAbstracted.doesExtend.extendsName);
            final ClassDefinition superSpecialized = TypeRewriter.rewriteClassDefinition(superAbstracted,
                                                                                         selfSpecialized.doesExtend.types);
            return superSpecialized.constructor.params;
        } else {
            return null;
        }
    } // getSuperParams

    public static void typeInScope(final Set<TypeVariable> inScope,
                                   final Type type) throws TypeErrorException {
        if (type instanceof IntType) {
            // do nothing
        } else if (type instanceof ClassType) {
            final ClassType asClass = (ClassType)type;
            for (final Type curType : asClass.types) {
                typeInScope(inScope, curType);
            }
        } else if (type instanceof TypeVariable) {
            if (!inScope.contains((TypeVariable)type)) {
                throw new TypeErrorException("Type variable not in scope: " + type);
            }
        } else {
            deadCode();
        }
    } // typeInScope

    public static void paramsInScope(final Set<TypeVariable> inScope,
                                     final VarDec[] params) throws TypeErrorException {
        for (final VarDec param : params) {
            typeInScope(inScope, param.type);
        }
    } // paramsInScope

    public static void paramsOk(final Set<TypeVariable> inScope,
                                final VarDec[] params) throws TypeErrorException {
        noDuplicates(params);
        paramsInScope(inScope, params);
    } // paramsOk
    
    public void typecheckMethod(final ClassType thisType,
                                final Set<TypeVariable> inScopeFromClass,
                                final MethodDefinition methodDef) throws TypeErrorException {
        final Set<TypeVariable> inScopeFromMethod = asSet(methodDef.typeVariables);
        final Set<TypeVariable> wholeScope = new HashSet<TypeVariable>(inScopeFromClass);
        wholeScope.addAll(inScopeFromMethod);
        virtualOk(thisType.name, methodDef.isVirtual, methodDef.name);
        paramsOk(wholeScope, methodDef.params);
        typeInScope(wholeScope, methodDef.returnType);
        superReturnOkInMethod(methodDef.body);
        typecheckStmt(TypeEnvironment.initialEnv(wholeScope, methodDef.params, thisType),
                      methodDef.returnType,
                      null,
                      methodDef.body);
    } // typecheckMethod
    
    public void typecheckConstructor(final ClassType thisType,
                                     final Set<TypeVariable> inScopeFromClass,
                                     final Constructor constructor) throws TypeErrorException {
        final ClassDefinition classDef = getClass(thisType.name);
        paramsOk(inScopeFromClass, constructor.params);
        superReturnOkInConstructor(classDef.doesExtend == null, constructor.body);
        typecheckStmt(TypeEnvironment.initialEnv(inScopeFromClass, constructor.params, thisType),
                      null,
                      getSuperParams(thisType),
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
            final Extends currentExtends = getClass(current).doesExtend;
            current = (currentExtends == null) ? null : currentExtends.extendsName;
        }
    } // noCyclicInheritance

    // checks that subclasses don't redefined parent class instance variables
    public void instanceVariablesOk(final Set<Variable> seen, final ClassName current) throws TypeErrorException {
        final ClassDefinition classDef = getClass(current);
        for (final VarDec param : classDef.instanceVariables) {
            if (seen.contains(param.variable)) {
                throw new TypeErrorException("Instance variable seen: " + param.variable);
            }
            seen.add(param.variable);
        }

        if (classDef.doesExtend != null) {
            instanceVariablesOk(seen, classDef.doesExtend.extendsName);
        }
    } // instanceVariablesOk

    // checks that subclasses don't redefined parent class instance variables
    public void instanceVariablesOk(final ClassName current) throws TypeErrorException {
        instanceVariablesOk(new HashSet<Variable>(), current);
    } // instanceVariablesOk
    
    public void typecheckClass(final ClassName className) throws TypeErrorException {
        final ClassDefinition classDef = getClass(className);
        final Set<TypeVariable> typeVariablesInScope = asSet(classDef.typeVariables);
        if (classDef.doesExtend != null) {
            for (final Type type : classDef.doesExtend.types) {
                typeInScope(typeVariablesInScope, type);
            }
        }
        noDuplicateMethodNames(classDef.methods);
        paramsOk(typeVariablesInScope, classDef.instanceVariables);
        instanceVariablesOk(className);
        final ClassType thisType = new ClassType(className, classDef.typeVariables);
        typecheckConstructor(thisType, typeVariablesInScope, classDef.constructor);
        for (final MethodDefinition methodDef : classDef.methods) {
            typecheckMethod(thisType, typeVariablesInScope, methodDef);
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
        typechecker.typecheckStmt(TypeEnvironment.initialEnv(new TypeVariable[0],
                                                             new VarDec[0],
                                                             null),
                                  null,
                                  null,
                                  program.entryPoint);
    } // typecheckProgram
} // Typechecker
