package sem;

import ast.StructTypeDecl;

public class StructTypeSymbol extends Symbol {
    public StructTypeDecl std;

    public StructTypeSymbol(StructTypeDecl std) {
        super(std.structType.name);
        this.std = std;
    }
}
