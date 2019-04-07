package vtables_example.codegen;

public enum MIPSRegister {
    ZERO(0, "zero"),
    AT(1, "at"),
    V0(2, "v0"),
    V1(3, "v1"),
    A0(4, "a0"),
    A1(5, "a1"),
    A2(6, "a2"),
    A3(7, "a3"),
    T0(8, "t0"),
    T1(9, "t1"),
    T2(10, "t2"),
    T3(11, "t3"),
    T4(12, "t4"),
    T5(13, "t5"),
    T6(14, "t6"),
    T7(15, "t7"),
    S0(16, "s0"),
    S1(17, "s1"),
    S2(18, "s2"),
    S3(19, "s3"),
    S4(20, "s4"),
    S5(21, "s5"),
    S6(22, "s6"),
    S7(23, "s7"),
    T8(24, "t8"),
    T9(25, "t9"),
    K0(26, "k0"),
    K1(27, "k1"),
    GP(28, "gp"),
    SP(29, "sp"),
    FP(30, "fp"),
    RA(31, "ra");

    public final int registerNumber;
    public final String baseRegisterName;

    MIPSRegister(final int registerNumber,
                 final String baseRegisterName) {
        this.registerNumber = registerNumber;
        this.baseRegisterName = baseRegisterName;
    }

    public String toString() {
        return "$" + baseRegisterName;
    }
} // MIPSRegister
