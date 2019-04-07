package vtables_subtyping_example.syntax;

public class ThisLhs implements Lhs {
    public int hashCode() { return 0; }
    public boolean equals(final Object other) {
        return other instanceof ThisLhs;
    }
    public String toString() { return "this"; }
}
