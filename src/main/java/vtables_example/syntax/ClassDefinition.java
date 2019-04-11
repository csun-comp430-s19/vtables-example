package vtables_example.syntax;

import java.util.Arrays;

public class ClassDefinition {
    public final ClassName myName;
    public final TypeVariable[] typeVariables;
    public final Extends doesExtend; // null if nothing
    public final VarDec[] instanceVariables;
    public final Constructor constructor;
    public final MethodDefinition[] methods;

    public ClassDefinition(final ClassName myName,
                           final TypeVariable[] typeVariables,
                           final Extends doesExtend,
                           final VarDec[] instanceVariables,
                           final Constructor constructor,
                           final MethodDefinition[] methods) {
        this.myName = myName;
        this.typeVariables = typeVariables;
        this.doesExtend = doesExtend;
        this.instanceVariables = instanceVariables;
        this.constructor = constructor;
        this.methods = methods;
    }

    public int hashCode() {
        final int extendsHash = (doesExtend == null) ? 0 : doesExtend.hashCode();
        return (myName.hashCode() +
                Arrays.deepHashCode(typeVariables) +
                extendsHash +
                Arrays.deepHashCode(instanceVariables) +
                constructor.hashCode() +
                Arrays.deepHashCode(methods));
    }

    public boolean extendsSame(final ClassDefinition other) {
        if (doesExtend == null) {
            return other.doesExtend == null;
        } else {
            if (other.doesExtend != null) {
                return doesExtend.equals(other.doesExtend);
            } else {
                return false;
            }
        }
    }
    
    public boolean equals(final Object other) {
        if (other instanceof ClassDefinition) {
            final ClassDefinition otherDef = (ClassDefinition)other;
            return (myName.equals(otherDef.myName) &&
                    Arrays.deepEquals(typeVariables, otherDef.typeVariables) &&
                    extendsSame(otherDef) &&
                    Arrays.deepEquals(instanceVariables, otherDef.instanceVariables) &&
                    constructor.equals(otherDef.constructor) &&
                    Arrays.deepEquals(methods, otherDef.methods));
        } else {
            return false;
        }
    }

    public String toString() {
        final String extendsString = (doesExtend == null) ? "" : doesExtend.toString();
        return ("class " +
                myName.toString() +
                "<" +
                Join.join(", ", typeVariables) +
                "> " +
                extendsString +
                " {\n    " +
                Join.join(";\n    ", instanceVariables) +
                "\n    " + 
                constructor.toString() +
                "\n    " +
                Join.join("\n    ", methods) +
                "\n}");
    }
}
