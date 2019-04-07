package vtables_example.codegen;

public class Addi extends TwoRegisterImmediateInstruction {
    public Addi(final MIPSRegister rt,
                final MIPSRegister rs,
                final int immediate) {
        super("addi", rt, rs, immediate);
    }
} // Addi
