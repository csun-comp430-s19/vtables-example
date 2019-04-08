package vtables_example.codegen;

public class MIPSLabel implements MIPSEntry {
    public final String name;

    public MIPSLabel(final String name) {
        this.name = name;
    }

    public String toString() {
        return name + ":";
    }

    public boolean equals(final Object other) {
        return (other instanceof MIPSLabel &&
                ((MIPSLabel)other).name.equals(name));
    }

    public int hashCode() {
        return name.hashCode();
    }
}
