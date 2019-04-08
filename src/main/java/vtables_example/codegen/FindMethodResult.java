package vtables_example.codegen;

import vtables_example.syntax.ClassName;

public class FindMethodResult {
    public final boolean isVirtual;
    public final ClassName providesImplementation;

    public FindMethodResult(final boolean isVirtual,
                            final ClassName providesImplementation) {
        this.isVirtual = isVirtual;
        this.providesImplementation = providesImplementation;
    }
}
