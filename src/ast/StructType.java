package ast;

import static gen.CodeGenerator.allignTo4Bytes;

public class StructType implements Type {
    public final String name;
    public StructTypeDecl std; // to be filled in by the type analyser

    public StructType(String name) {
        this.name = name;
    }

    public <T> T accept(ASTVisitor<T> v) { return v.visitStructType(this); }

    @Override
    public String toString() { return "struct " + name; }

    @Override
    public int sizeOf() {
        int size = 0;
        for (VarDecl vd : std.variables) {
            size += allignTo4Bytes(vd.type.sizeOf());
        }
        return size;
    }
}
