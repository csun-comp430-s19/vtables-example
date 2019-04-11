package vtables_example.syntax;

import java.util.Arrays;

public class Extends {
    public final ClassName extendsName;
    public final Type[] types;

    public Extends(final ClassName extendsName,
                   final Type[] types) {
        this.extendsName = extendsName;
        this.types = types;
    }

    public int hashCode() {
        return extendsName.hashCode() + Arrays.deepHashCode(types);
    }

    public String toString() {
        return ("extends " + extendsName.name + "<" +
                Join.join(", ", types) + ">");
    }

    public boolean equals(final Object other) {
        if (other instanceof Extends) {
            final Extends asExtends = (Extends)other;
            return (asExtends.extendsName.equals(extendsName) &&
                    Arrays.deepEquals(asExtends.types, types));
        } else {
            return false;
        }
    }
}
