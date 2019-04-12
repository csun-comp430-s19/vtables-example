package vtables_example.typechecker;

import vtables_example.syntax.*;

import java.util.Map;
import java.util.HashMap;

public class TypeRewriter {
    public final Map<TypeVariable, Type> replacements;

    public TypeRewriter(final Map<TypeVariable, Type> replacements) {
        this.replacements = replacements;
    }

    public Type[] rewriteTypes(final Type[] types) {
        final Type[] result = new Type[types.length];
        for (int index = 0; index < result.length; index++) {
            result[index] = rewriteType(types[index]);
        }
        return result;
    } // rewriteTypes
    
    public Type rewriteType(final Type originalType) {
        if (originalType instanceof IntType) {
            return originalType;
        } else if (originalType instanceof ClassType) {
            final ClassType asClass = (ClassType)originalType;
            final Type[] newTypes = rewriteTypes(asClass.types);
            return new ClassType(asClass.name, newTypes);
        } else if (originalType instanceof TypeVariable) {
            final TypeVariable asVariable = (TypeVariable)originalType;
            assert(replacements.containsKey(asVariable));
            return replacements.get(asVariable);
        } else {
            assert(false);
            return null;
        }
    } // typeRewrite

    public VarDec rewriteVarDec(final VarDec vardec) {
        return new VarDec(rewriteType(vardec.type), vardec.variable);
    } // rewriteVarDec

    public VarDec[] rewriteVarDecs(final VarDec[] vardecs) {
        final VarDec[] result = new VarDec[vardecs.length];
        for (int index = 0; index < vardecs.length; index++) {
            result[index] = rewriteVarDec(vardecs[index]);
        }
        return result;
    } // rewriteVarDecs

    public Constructor rewriteConstructor(final Constructor constructor) {
        return new Constructor(rewriteVarDecs(constructor.params),
                               constructor.body);
    } // rewriteConstructor
    
    public MethodDefinition rewriteMethodDefinition(final MethodDefinition methodDef) {
        final Map<TypeVariable, Type> newReplacements = new HashMap<TypeVariable, Type>(replacements);

        // don't change any type variables local to the method
        for (final TypeVariable typeVariable : methodDef.typeVariables) {
            newReplacements.put(typeVariable, typeVariable);
        }

        final TypeRewriter nestedRewriter = new TypeRewriter(newReplacements);
        return new MethodDefinition(methodDef.isVirtual,
                                    methodDef.typeVariables,
                                    nestedRewriter.rewriteType(methodDef.returnType),
                                    methodDef.name,
                                    nestedRewriter.rewriteVarDecs(methodDef.params),
                                    methodDef.body);
    } // rewriteMethodDefinition

    public MethodDefinition[] rewriteMethodDefinitions(final MethodDefinition[] methodDefs) {
        final MethodDefinition[] result = new MethodDefinition[methodDefs.length];
        for (int index = 0; index < result.length; index++) {
            result[index] = rewriteMethodDefinition(methodDefs[index]);
        }
        return result;
    } // rewriteMethodDefinitions

    public Extends rewriteExtends(final Extends doesExtend) {
        if (doesExtend != null) {
            return new Extends(doesExtend.extendsName,
                               rewriteTypes(doesExtend.types));
        } else {
            return null;
        }
    } // rewriteExtends
    
    public static ClassDefinition rewriteClassDefinition(final ClassDefinition classDef,
                                                         final Type[] replacements) throws TypeErrorException {
        final Map<TypeVariable, Type> mapping = typeReplacementMapping(classDef.typeVariables, replacements);
        final TypeRewriter rewriter = new TypeRewriter(mapping);
        return new ClassDefinition(classDef.myName,
                                   classDef.typeVariables,
                                   rewriter.rewriteExtends(classDef.doesExtend),
                                   rewriter.rewriteVarDecs(classDef.instanceVariables),
                                   rewriter.rewriteConstructor(classDef.constructor),
                                   rewriter.rewriteMethodDefinitions(classDef.methods));
    } // rewriteClassDefinition
    
    public static MethodDefinition rewriteMethodDefinitionToplevel(final MethodDefinition methodDef,
                                                                   final Type[] replacements) throws TypeErrorException {
        final Map<TypeVariable, Type> mapping = typeReplacementMapping(methodDef.typeVariables, replacements);
        final TypeRewriter rewriter = new TypeRewriter(mapping);
        return new MethodDefinition(methodDef.isVirtual,
                                    methodDef.typeVariables,
                                    rewriter.rewriteType(methodDef.returnType),
                                    methodDef.name,
                                    rewriter.rewriteVarDecs(methodDef.params),
                                    methodDef.body);
    } // rewriteMethodDefinition
    
    public static Map<TypeVariable, Type> typeReplacementMapping(final TypeVariable[] typeVariables,
                                                                 final Type[] replacements) throws TypeErrorException {
        if (typeVariables.length != replacements.length) {
            throw new TypeErrorException("Type arity mismatch; expected: " + typeVariables.length +
                                         " types; received: " + replacements.length + " types");
        }

        final Map<TypeVariable, Type> mapping = new HashMap<TypeVariable, Type>();
        for (int index = 0; index < typeVariables.length; index++) {
            mapping.put(typeVariables[index], replacements[index]);
        }
        return mapping;
    } // typeReplacementMapping
} // TypeRewriter
