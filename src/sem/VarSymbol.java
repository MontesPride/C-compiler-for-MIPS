package sem;

import ast.VarDecl;

public class VarSymbol extends Symbol {
    public VarDecl vd;

    public VarSymbol(VarDecl vd) {
        super(vd.varName);
        this.vd = vd;
    }
}
