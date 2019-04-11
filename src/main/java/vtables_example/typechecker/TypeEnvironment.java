package vtables_example.typechecker;

import vtables_example.syntax.*;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class TypeEnvironment {
    private final Set<TypeVariable> inScope;     // type variables in scope
    private final Map<Variable, Type> variables; // variables in scope
    private final ClassType thisType;           // null if outside of method
    
    public TypeEnvironment(final Set<TypeVariable> inScope,
                           final Map<Variable, Type> variables,
                           final ClassType thisType) {
        this.inScope = inScope;
        this.variables = variables;
        this.thisType = thisType;
    }

    public void typeInScope(final Type type) throws TypeErrorException {
        Typechecker.typeInScope(inScope, type);
    }

    public void typesInScope(final Type[] types) throws TypeErrorException {
        for (final Type type : types) {
            typeInScope(type);
        }
    }
    
    public Type thisType() throws TypeErrorException {
        if (thisType == null) {
            throw new TypeErrorException("this used outside of class");
        } else {
            return thisType;
        }
    }
    
    public Type lookup(final Variable variable) throws TypeErrorException {
        final Type result = variables.get(variable);
        if (result == null) {
            throw new TypeErrorException("No such variable: " + variable);
        } else {
            return result;
        }
    }

    public TypeEnvironment addVariable(final Variable variable,
                                       final Type type) throws TypeErrorException {
        if (!variables.containsKey(variable)) {
            final Map<Variable, Type> newVariables = new HashMap<Variable, Type>(variables);
            newVariables.put(variable, type);
            return new TypeEnvironment(inScope, newVariables, thisType);
        } else {
            throw new TypeErrorException("Redefinition of variable: " + variable);
        }
    }

    public TypeEnvironment addVariable(final VarDec vardec) throws TypeErrorException {
        return addVariable(vardec.variable, vardec.type);
    }

    public static Map<Variable, Type> variableMapping(final VarDec[] params) throws TypeErrorException {
        Typechecker.noDuplicates(params);
        final Map<Variable, Type> result = new HashMap<Variable, Type>();
        for (final VarDec param : params) {
            result.put(param.variable, param.type);
        }
        return result;
    } // variableMapping

    public static TypeEnvironment initialEnv(final Set<TypeVariable> inScope,
                                             final VarDec[] params,
                                             final ClassType thisType) throws TypeErrorException {
        return new TypeEnvironment(inScope,
                                   variableMapping(params),
                                   thisType);
    } // initialEnv
    
    public static TypeEnvironment initialEnv(final TypeVariable[] typeVariables,
                                             final VarDec[] params,
                                             final ClassType thisType) throws TypeErrorException {
        return initialEnv(Typechecker.asSet(typeVariables),
                          params,
                          thisType);
    } // initialEnv
}
