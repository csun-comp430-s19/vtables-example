package vtables_example.codegen;

public class SingleLabelInstruction implements MIPSInstruction {
    public String instructionName;
    public final MIPSLabel label;

    public SingleLabelInstruction(final String instructionName,
                                  final MIPSLabel label) {
        this.instructionName = instructionName;
        this.label = label;
    }

    public String toString() {
        return (MIPSInstruction.INDENT +
                instructionName + " " +
                label.name);
    }

    public boolean equals(final Object other) {
        if (other instanceof SingleLabelInstruction) {
            final SingleLabelInstruction asSingle = (SingleLabelInstruction)other;
            return (asSingle.instructionName.equals(instructionName) &&
                    asSingle.label.equals(label));
        } else {
            return false;
        }
    }

    public int hashCode() {
        return instructionName.hashCode() + label.hashCode();
    }
} // SingleLabelInstruction
