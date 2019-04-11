package vtables_example.syntax;

import java.util.Arrays;

public class ClassType implements Type {
    public final ClassName name;
    public final Type[] types;

    public ClassType(final ClassName name,
                     final Type[] types) {
        this.name = name;
        this.types = types;
    }

    public int hashCode() {
        return name.hashCode() + Arrays.deepHashCode(types);
    }

    public boolean equals(final Object other) {
        if (other instanceof ClassType) {
            final ClassType otherClass = (ClassType)other;
            return (otherClass.name.equals(name) &&
                    Arrays.deepEquals(otherClass.types, types));
        } else {
            return false;
        }
    }
    
    public String toString() {
        return name.toString() + "<" + Join.join(", ", types) + ">";
    }
}
