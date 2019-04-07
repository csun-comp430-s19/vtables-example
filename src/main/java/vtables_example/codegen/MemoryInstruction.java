package vtables_example.codegen;

public class MemoryInstruction implements MIPSInstruction {
    public final String instructionName;
    public final MIPSRegister left;
    public final int offset;
    public final MIPSRegister right;

    public MemoryInstruction(final String instructionName,
                             final MIPSRegister left,
                             final int offset,
                             final MIPSRegister right) {
        this.instructionName = instructionName;
        this.left = left;
        this.offset = offset;
        this.right = right;
    }

    public String toString() {
        return (MIPSInstruction.INDENT +
                instructionName + " " +
                left.toString() + ", " +
                Integer.toString(offset) + "(" +
                right.toString() + ")");
    } // toString

    public boolean equals(final Object other) {
        if (other instanceof MemoryInstruction) {
            final MemoryInstruction otherI =
                (MemoryInstruction)other;
            return (instructionName.equals(otherI.instructionName) &&
                    left.equals(otherI.left) &&
                    offset == otherI.offset &&
                    right.equals(otherI.right));
        } else {
            return false;
        }
    } // equals

    public int hashCode() {
        return (instructionName.hashCode() +
                left.hashCode() +
                offset +
                right.hashCode());
    } // hashCode
} // MemoryInstruction
