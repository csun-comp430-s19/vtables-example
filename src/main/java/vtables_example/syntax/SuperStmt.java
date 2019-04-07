package vtables_example.syntax;

import java.util.Arrays;

// for initializing a superclass
public class SuperStmt implements Stmt {
    public final Exp[] params;
    
    public SuperStmt(final Exp[] params) {
        this.params = params;
    }

    public int hashCode() {
        return Arrays.deepHashCode(params);
    }

    public boolean equals(final Object other) {
        return (other instanceof SuperStmt &&
                Arrays.deepEquals(((SuperStmt)other).params, params));
    }

    public String toString() {
        return "super(" + Join.join(", ", params) + ")";
    }
}
