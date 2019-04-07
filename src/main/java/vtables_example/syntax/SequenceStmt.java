package vtables_example.syntax;

public class SequenceStmt implements Stmt {
    public final Stmt first;
    public final Stmt second;

    public SequenceStmt(final Stmt first,
                        final Stmt second) {
        this.first = first;
        this.second = second;
    }

    public int hashCode() {
        return first.hashCode() + second.hashCode();
    }

    public String toString() {
        return first.toString() + "; " + second.toString();
    }

    public boolean equals(final Object other) {
        if (other instanceof SequenceStmt) {
            final SequenceStmt asSeq = (SequenceStmt)other;
            return (asSeq.first.equals(first) &&
                    asSeq.second.equals(second));
        } else {
            return false;
        }
    }
}
