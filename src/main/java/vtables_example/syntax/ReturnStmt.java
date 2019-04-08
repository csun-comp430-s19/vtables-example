package vtables_example.syntax;

public class ReturnStmt implements Stmt {
    // null if returning void
    public final Exp exp;

    public ReturnStmt(final Exp exp) {
        this.exp = exp;
    }

    public int hashCode() {
        return (exp == null) ? 0 : exp.hashCode();
    }

    public String toString() {
        if (exp == null) {
            return "return";
        } else {
            return "return " + exp.toString();
        }
    }

    public boolean equals(final Object other) {
        if (other instanceof ReturnStmt) {
            final Exp otherExp = ((ReturnStmt)other).exp;
            if (exp == null) {
                return otherExp == null;
            } else if (otherExp == null) {
                return false;
            } else {
                return exp.equals(otherExp);
            }
        } else {
            return false;
        }
    }
}
