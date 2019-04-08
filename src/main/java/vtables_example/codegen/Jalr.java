package vtables_example.codegen;

public class Jalr extends SingleRegisterInstruction {
    public Jalr(final MIPSRegister rd) {
        super("jalr", rd);
    }
}
