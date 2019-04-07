package vtables_example.codegen;

public class TwoRegisterImmediateInstruction implements MIPSInstruction {
    public final String instructionName;
    public final MIPSRegister rt;
    public final MIPSRegister rs;
    public final int immediate;

    public TwoRegisterImmediateInstruction(final String instructionName,
                                           final MIPSRegister rt,
                                           final MIPSRegister rs,
                                           final int immediate) {
        this.instructionName = instructionName;
        this.rt = rt;
        this.rs = rs;
        this.immediate = immediate;
    }

    public String toString() {
        return (MIPSInstruction.INDENT +
                instructionName + " " +
                rt.toString() + ", " +
                rs.toString() + ", " +
                Integer.toString(immediate));
    } // toString

    public boolean equals(final Object other) {
        if (other instanceof TwoRegisterImmediateInstruction) {
            final TwoRegisterImmediateInstruction otherI =
                (TwoRegisterImmediateInstruction)other;
            return (instructionName.equals(otherI.instructionName) &&
                    rt.equals(otherI.rt) &&
                    rs.equals(otherI.rs) &&
                    immediate == otherI.immediate);
        } else {
            return false;
        }
    } // equals

    public int hashCode() {
        return (instructionName.hashCode() +
                rt.hashCode() +
                rs.hashCode() +
                immediate);
    } // hashCode
} // TwoRegisterImmediateInstruction
