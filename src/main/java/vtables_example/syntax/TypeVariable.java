package vtables_example.syntax;

public class TypeVariable extends Name implements Type{
    public TypeVariable(final String name) {
        super(name);
    }

    public boolean sameClass(final Name other) {
        return other instanceof TypeVariable;
    }
}
