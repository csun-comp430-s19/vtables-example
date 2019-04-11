package vtables_example.syntax;

import java.util.Arrays;

public class MethodDefinition {
    // unlike C++, this must be set all the way down
    public boolean isVirtual;
    public final TypeVariable[] typeVariables;
    public final Type returnType;
    public final MethodName name;
    public final VarDec[] params;
    public final Stmt body;

    public MethodDefinition(final boolean isVirtual,
                            final TypeVariable[] typeVariables,
                            final Type returnType,
                            final MethodName name,
                            final VarDec[] params,
                            final Stmt body) {
        this.isVirtual = isVirtual;
        this.typeVariables = typeVariables;
        this.returnType = returnType;
        this.name = name;
        this.params = params;
        this.body = body;
    }

    public int hashCode() {
        final int virt = (isVirtual) ? 1 : 0;
        return (virt +
                Arrays.deepHashCode(typeVariables) +
                returnType.hashCode() +
                name.hashCode() +
                Arrays.deepHashCode(params) +
                body.hashCode());
    }

    public boolean equals(final Object other) {
        if (other instanceof MethodDefinition) {
            final MethodDefinition otherMethod = (MethodDefinition)other;
            return (otherMethod.isVirtual == isVirtual &&
                    Arrays.deepEquals(otherMethod.typeVariables, typeVariables) &&
                    otherMethod.returnType.equals(returnType) &&
                    otherMethod.name.equals(name) &&
                    Arrays.deepEquals(otherMethod.params, params) &&
                    otherMethod.body.equals(body));
        } else {
            return false;
        }
    }

    public String toString() {
        final String virt = (isVirtual) ? "virtual " : "";
        return (virt +
                "<" +
                Join.join(", ", typeVariables) +
                "> " +
                returnType.toString() +
                " " +
                name.toString() +
                "(" +
                Join.join(", ", params) +
                ") {" +
                body.toString() +
                "}");
    }
}

                    
