package vtables_example.syntax;

public class LhsExp implements Exp {
    public final Lhs lhs;

    public LhsExp(final Lhs lhs) {
        this.lhs = lhs;
    }

    public int hashCode() {
        return lhs.hashCode();
    }

    public String toString() {
        return lhs.toString();
    }

    public boolean equals(final Object other) {
        return (other instanceof LhsExp &&
                ((LhsExp)other).lhs.equals(lhs));
    }
}
