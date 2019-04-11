package vtables_example.codegen;

import vtables_example.syntax.Variable;
import vtables_example.syntax.Type;

public class VariableEntry {
    public final Variable variable;
    public final int size;
    
    public VariableEntry(final Variable variable,
                         final int size) {
        this.variable = variable;
        this.size = size;
    }

    public String toString() {
        return ("VariableEntry(" +
                variable + ", " +
                size + ")");
    }

    public boolean equals(final Object other) {
        if (other instanceof VariableEntry) {
            final VariableEntry otherEntry = (VariableEntry)other;
            return (variable.equals(otherEntry.variable) &&
                    size == otherEntry.size);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return (variable.hashCode() +
                size);
    }
} // VariableEntry
