package vtables_example.syntax;

public class AssignStmt implements Stmt {
    public final Lhs lhs;
    public final Exp exp;

    public AssignStmt(final Lhs lhs,
                      final Exp exp) {
        this.lhs = lhs;
        this.exp = exp;
    }

    public int hashCode() {
        return lhs.hashCode() + exp.hashCode();
    }

    public String toString() {
        return lhs.toString() + " = " + exp.toString();
    }

    public boolean equals(final Object other) {
        if (other instanceof AssignStmt) {
            final AssignStmt otherAssign = (AssignStmt)other;
            return (otherAssign.lhs.equals(lhs) &&
                    otherAssign.exp.equals(exp));
        } else {
            return false;
        }
    }
}
