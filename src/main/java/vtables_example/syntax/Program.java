package vtables_example.syntax;

import java.util.Arrays;

public class Program {
    public final ClassDefinition[] classes;
    public final Stmt entryPoint;

    public Program(final ClassDefinition[] classes,
                   final Stmt entryPoint) {
        this.classes = classes;
        this.entryPoint = entryPoint;
    }

    public int hashCode() {
        return Arrays.deepHashCode(classes) + entryPoint.hashCode();
    }

    public String toString() {
        return Join.join("\n", classes) + "\n" + entryPoint.toString();
    }

    public boolean equals(final Object other) {
        if (other instanceof Program) {
            final Program otherProg = (Program)other;
            return (Arrays.deepEquals(classes, otherProg.classes) &&
                    entryPoint.equals(otherProg.entryPoint));
        } else {
            return false;
        }
    }
}
