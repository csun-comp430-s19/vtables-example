package vtables_subtyping_example.syntax;

import java.util.Arrays;

public class MethodCallStmt implements Stmt {
    public final VarDec vardec;
    public final Exp exp;
    public final MethodName name;
    public final Exp[] params;

    public MethodCallStmt(final VarDec vardec,
                          final Exp exp,
                          final MethodName name,
                          final Exp[] params) {
        this.vardec = vardec;
        this.exp = exp;
        this.name = name;
        this.params = params;
    }

    public int hashCode() {
        return (vardec.hashCode() +
                exp.hashCode() +
                name.hashCode() +
                Arrays.deepHashCode(params));
    }

    public boolean equals(final Object other) {
        if (other instanceof MethodCallStmt) {
            final MethodCallStmt otherCall = (MethodCallStmt)other;
            return (otherCall.vardec.equals(vardec) &&
                    otherCall.exp.equals(exp) &&
                    otherCall.name.equals(name) &&
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
                "(" +
                Join.join(", ", params) +
                ")");
    }
}
