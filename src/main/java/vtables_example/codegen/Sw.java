package vtables_example.codegen;

public class Sw extends MemoryInstruction {
    public Sw(final MIPSRegister left,
              final int offset,
              final MIPSRegister right) {
        super("sw", left, offset, right);
    }
} // Sw
