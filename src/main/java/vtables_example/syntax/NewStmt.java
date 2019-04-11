package vtables_example.syntax;

import java.util.Arrays;

public class NewStmt implements Stmt {
    public final VarDec vardec;
    public final ClassName name;
    public final Type[] types;
    public final Exp[] params;

    public NewStmt(final VarDec vardec,
                   final ClassName name,
                   final Type[] types,
                   final Exp[] params) {
        this.vardec = vardec;
        this.name = name;
        this.types = types;
        this.params = params;
    }

    public int hashCode() {
        return (vardec.hashCode() +
                name.hashCode() +
                Arrays.deepHashCode(types) +
                Arrays.deepHashCode(params));
    }

    public boolean equals(final Object other) {
        if (other instanceof NewStmt) {
            final NewStmt otherNew = (NewStmt)other;
            return (otherNew.vardec.equals(vardec) &&
                    otherNew.name.equals(name) &&
                    Arrays.deepEquals(otherNew.types, types) &&
                    Arrays.deepEquals(otherNew.params, params));
        } else {
            return false;
        }
    }

    public String toString() {
        return (vardec.toString() +
                " = new " +
                name.toString() +
                "<" +
                Join.join(", ", types) +
                ">(" +
                Join.join(", ", params) +
                ")");
    }
}
