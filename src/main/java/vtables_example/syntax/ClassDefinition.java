package vtables_example.syntax;

import java.util.Arrays;

public class ClassDefinition {
    public final ClassName myName;
    public final ClassName extendsName; // null if nothing
    public final VarDec[] instanceVariables;
    public final Constructor constructor;
    public final MethodDefinition[] methods;

    public ClassDefinition(final ClassName myName,
                           final ClassName extendsName,
                           final VarDec[] instanceVariables,
                           final Constructor constructor,
                           final MethodDefinition[] methods) {
        this.myName = myName;
        this.extendsName = extendsName;
        this.instanceVariables = instanceVariables;
        this.constructor = constructor;
        this.methods = methods;
    }

    public int hashCode() {
        final int extendsHash = (extendsName == null) ? 0 : extendsName.hashCode();
        return (myName.hashCode() +
                extendsHash +
                Arrays.deepHashCode(instanceVariables) +
                constructor.hashCode() +
                Arrays.deepHashCode(methods));
    }

    public boolean extendsSame(final ClassDefinition other) {
        if (extendsName == null) {
            return other.extendsName == null;
        } else {
            if (other.extendsName != null) {
                return extendsName.equals(other.extendsName);
            } else {
                return false;
            }
        }
    }
    
    public boolean equals(final Object other) {
        if (other instanceof ClassDefinition) {
            final ClassDefinition otherDef = (ClassDefinition)other;
            return (myName.equals(otherDef.myName) &&
                    extendsSame(otherDef) &&
                    Arrays.deepEquals(instanceVariables, otherDef.instanceVariables) &&
                    constructor.equals(otherDef.constructor) &&
                    Arrays.deepEquals(methods, otherDef.methods));
        } else {
            return false;
        }
    }

    public String toString() {
        final String extendsString = (extendsName == null) ? "" : "extends " + extendsName.toString();
        return ("class " +
                myName.toString() +
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
