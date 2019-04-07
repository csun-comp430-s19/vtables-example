package vtables_subtyping_example.syntax;

public class PrintStmt implements Stmt {
    public final Exp exp;

    public PrintStmt(final Exp exp) {
        this.exp = exp;
    }

    public int hashCode() {
        return exp.hashCode();
    }

    public boolean equals(final Object other) {
        return (other instanceof PrintStmt &&
                ((PrintStmt)other).exp.equals(exp));
    }

    public String toString() {
        return "print(" + exp.toString() + ")";
    }
}
