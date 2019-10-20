package ast;

import java.util.List;

public class StructTypeDecl implements ASTNode {
    public final StructType structType;
    public final List<VarDecl> variables;

    public StructTypeDecl(StructType structType, List<VarDecl> variables) {
        this.structType = structType;
        this.variables = variables;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructTypeDecl(this);
    }

}
