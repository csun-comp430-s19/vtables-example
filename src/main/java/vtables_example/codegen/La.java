package vtables_example.codegen;

public class La implements MIPSInstruction {
    public final MIPSRegister rd;
    public final MIPSLabel label;

    public La(final MIPSRegister rd,
              final MIPSLabel label) {
        this.rd = rd;
        this.label = label;
    }

    public String toString() {
        return (MIPSInstruction.INDENT + "la " +
                rd.toString() + ", " +
                label.name);
    } // toString

    public int hashCode() {
        return rd.hashCode() + label.hashCode();
    } // hashCode

    public boolean equals(final Object other) {
        if (other instanceof La) {
            final La otherI = (La)other;
            return (rd.equals(otherI.rd) &&
                    label.equals(otherI.label));
        } else {
            return false;
        }
    } // equals
} // La
