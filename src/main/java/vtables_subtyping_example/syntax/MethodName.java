package vtables_subtyping_example.syntax;

public class MethodName extends Name {
    public MethodName(final String name) {
        super(name);
    }

    public boolean sameClass(final Name other) {
        return other instanceof MethodName;
    }
}
