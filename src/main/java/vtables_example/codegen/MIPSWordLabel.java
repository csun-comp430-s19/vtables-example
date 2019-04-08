package vtables_example.codegen;

// .word label
public class MIPSWordLabel implements MIPSEntry {
    public final MIPSLabel label;

    public MIPSWordLabel(final MIPSLabel label) {
        this.label = label;
    }

    public int hashCode() {
        return label.hashCode();
    }

    public String toString() {
        return MIPSInstruction.INDENT + ".word " + label.name;
    }

    public boolean equals(final Object other) {
        return (other instanceof MIPSWordLabel &&
                ((MIPSWordLabel)other).label.equals(label));
    }
}

