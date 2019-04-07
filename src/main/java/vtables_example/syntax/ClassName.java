package vtables_example.syntax;

public class ClassName extends Name {
    public ClassName(final String name) {
        super(name);
    }

    public boolean sameClass(final Name other) {
        return other instanceof ClassName;
    }
}
