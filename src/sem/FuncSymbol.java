package sem;

import ast.FunDecl;

public class FuncSymbol extends Symbol {
    public FunDecl fd;

    public FuncSymbol(FunDecl fd) {
        super(fd.name);
        this.fd = fd;
    }
}
