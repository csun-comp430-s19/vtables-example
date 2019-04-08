package vtables_example.syntax;

public class EmptyStmt implements Stmt {
    public int hashCode() { return 0; }
    public boolean equals(final Object other) {
        return other instanceof EmptyStmt;
    }
    public String toString() { return ""; }
}
