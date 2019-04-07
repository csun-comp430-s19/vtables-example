package vtables_example.syntax;

public class ReturnStmt implements Stmt {
    public final Exp exp;

    public ReturnStmt(final Exp exp) {
        this.exp = exp;
    }

    public int hashCode() {
        return exp.hashCode();
    }

    public String toString() {
        return "return " + exp.toString();
    }

    public boolean equals(final Object other) {
        return (other instanceof ReturnStmt &&
                ((ReturnStmt)other).exp.equals(exp));
    }
}
