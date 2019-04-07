package vtables_example.codegen;

public class Syscall implements MIPSInstruction {
    public int hashCode() { return 0; }
    public boolean equals(final Object other) {
        return other instanceof Syscall;
    }
    public String toString() {
        return MIPSInstruction.INDENT + "syscall";
    }
} // Syscall
