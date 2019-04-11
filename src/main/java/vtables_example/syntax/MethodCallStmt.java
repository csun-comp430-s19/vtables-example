package vtables_example.syntax;

import java.util.Arrays;

public class MethodCallStmt implements Stmt {
    public final VarDec vardec;
    public final Exp exp;
    public final MethodName name;
    public final Type[] types;
    public final Exp[] params;
    private ClassName onClass; // intended to be filled in by the typechecker
    
    public MethodCallStmt(final VarDec vardec,
                          final Exp exp,
                          final MethodName name,
                          final Type[] types,
                          final Exp[] params) {
        this.vardec = vardec;
        this.exp = exp;
        this.name = name;
        this.types = types;
        this.params = params;
        onClass = null;
    }

    public int hashCode() {
        return (vardec.hashCode() +
                exp.hashCode() +
                name.hashCode() +
                Arrays.deepHashCode(types) +
                Arrays.deepHashCode(params));
    }

    public boolean equals(final Object other) {
        if (other instanceof MethodCallStmt) {
            final MethodCallStmt otherCall = (MethodCallStmt)other;
            return (otherCall.vardec.equals(vardec) &&
                    otherCall.exp.equals(exp) &&
                    otherCall.name.equals(name) &&
                    Arrays.deepEquals(otherCall.types, types) &&
                    Arrays.deepEquals(otherCall.params, params));
        } else {
            return false;
        }
    }

    public String toString() {
        return (vardec.toString() +
                " = " +
                exp.toString() +
                "." +
                name.toString() +
                "<" +
                Join.join(", ", types) +
                ">(" +
                Join.join(", ", params) +
                ")");
    }

    public void setOnClass(final ClassName onClass) {
        assert(this.onClass == null);
        this.onClass = onClass;
    }

    public ClassName getOnClass() {
        assert(onClass != null);
        return onClass;
    }
}
