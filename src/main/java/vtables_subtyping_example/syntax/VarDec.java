package vtables_subtyping_example.syntax;

public class VarDec {
    public final Type type;
    public final Variable variable;

    public VarDec(final Type type,
                  final Variable variable) {
        this.type = type;
        this.variable = variable;
    }

    public int hashCode() {
        return type.hashCode() + variable.hashCode();
    }

    public boolean equals(final Object other) {
        if (other instanceof VarDec) {
            final VarDec asDec = (VarDec)other;
            return (type.equals(asDec.type) &&
                    variable.equals(asDec.variable));
        } else {
            return false;
        }
    }

    public String toString() {
        return type.toString() + " " + variable.toString();
    }
}
