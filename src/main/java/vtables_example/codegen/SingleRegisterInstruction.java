package vtables_example.codegen;

public class SingleRegisterInstruction implements MIPSInstruction {
    public final String instructionName;
    public final MIPSRegister rd;

    public SingleRegisterInstruction(final String instructionName,
                                     final MIPSRegister rd) {
        this.instructionName = instructionName;
        this.rd = rd;
    }

    public String toString() {
        return (MIPSInstruction.INDENT +
                instructionName + " " +
                rd.toString());
    } // toString

    public boolean equals(final Object other) {
        if (other instanceof SingleRegisterInstruction) {
            final SingleRegisterInstruction otherI =
                (SingleRegisterInstruction)other;
            return (instructionName.equals(otherI.instructionName) &&
                    rd.equals(otherI.rd));
        } else {
            return false;
        }
    } // equals

    public int hashCode() {
        return instructionName.hashCode() + rd.hashCode();
    } // hashCode
} // SingleRegisterInstruction
