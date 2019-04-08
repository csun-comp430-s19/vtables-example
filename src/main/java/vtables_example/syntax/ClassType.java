package vtables_example.syntax;

public class ClassType implements Type {
    final ClassName name;

    public ClassType(final ClassName name) {
        this.name = name;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(final Object other) {
        return (other instanceof ClassType &&
                ((ClassType)other).name.equals(name));
    }
    
    public String toString() {
        return name.toString();
    }
}
