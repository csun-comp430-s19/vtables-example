package vtables_example.syntax;

public class VoidType implements Type {
    public int hashCode() { return 0; }
    public boolean equals(final Object other) {
        return other instanceof VoidType;
    }
    public String toString() { return "void"; }
}
