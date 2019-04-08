package vtables_example.codegen;

public class FieldOffsetResult {
    final boolean found;
    final int offset; // meaningless if !found

    public FieldOffsetResult(final boolean found, final int offset) {
        this.found = found;
        this.offset = offset;
    }
}
