package vtables_example.syntax;

public class FieldAccessLhs implements Lhs {
    public final Lhs lhs;
    public final Variable field;
    private ClassName lhsClass; // intended to be filled in by the typechecker
    
    public FieldAccessLhs(final Lhs lhs,
                          final Variable field) {
        this.lhs = lhs;
        this.field = field;
        lhsClass = null;
    }

    public int hashCode() {
        return lhs.hashCode() + field.hashCode();
    }

    public boolean equals(final Object other) {
        if (other instanceof FieldAccessLhs) {
            final FieldAccessLhs asField = (FieldAccessLhs)other;
            return (lhs.equals(asField.lhs) &&
                    field.equals(asField.field));
        } else {
            return false;
        }
    }

    public String toString() {
        return lhs.toString() + "." + field.toString();
    }

    public void setLhsClass(final ClassName lhsClass) {
        assert(this.lhsClass == null);
        this.lhsClass = lhsClass;
    }

    public ClassName getLhsClass() {
        assert(lhsClass != null);
        return lhsClass;
    }
}

                    
