package vtables_example.syntax;

import java.util.Arrays;

public class Constructor {
    public final VarDec[] params;
    public final Stmt body;

    public Constructor(final VarDec[] params,
                       final Stmt body) {
        this.params = params;
        this.body = body;
    }

    public int hashCode() {
        return Arrays.deepHashCode(params) + body.hashCode();
    }

    public boolean equals(final Object other) {
        if (other instanceof Constructor) {
            final Constructor asCons = (Constructor)other;
            return (Arrays.deepEquals(asCons.params, params) &&
                    asCons.body.equals(body));
        } else {
            return false;
        }
    }

    public String toString() {
        return ("init(" +
                Join.join(", ", params) +
                "{ " + body.toString() + " }");
    }
}
