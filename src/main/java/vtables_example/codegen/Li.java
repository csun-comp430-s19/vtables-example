package vtables_example.codegen;

public class Li implements MIPSInstruction {
    public final MIPSRegister rd;
    public final int immediate;

    public Li(final MIPSRegister rd,
              final int immediate) {
        this.rd = rd;
        this.immediate = immediate;
    }

    public String toString() {
        return (MIPSInstruction.INDENT + "li " +
                rd.toString() + ", " +
                Integer.toString(immediate));
    } // toString

    public boolean equals(final Object other) {
        if (other instanceof Li) {
            final Li otherLi = (Li)other;
            return (rd.equals(otherLi.rd) &&
                    immediate == otherLi.immediate);
        } else {
            return false;
        }
    } // equals
    
    public int hashCode() {
        return immediate;
    } // hashCode
} // Li

