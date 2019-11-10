package ast;

import java.util.*;

public class FunDecl implements ASTNode {
    public final Type type;
    public final String name;
    public final List<VarDecl> params;
    public final Block block;
    public final boolean isPreDefined;

    public FunDecl(Type type, String name, List<VarDecl> params, Block block) {
        this.type = type;
        this.name = name;
        this.params = params;
        this.block = block;
        this.isPreDefined = false;
    }

    public FunDecl(Type type, String name, List<VarDecl> params, Block block, boolean isInBuilt) {
        this.type = type;
        this.name = name;
        this.params = params;
        this.block = block;
        this.isPreDefined = isInBuilt;
    }

    public <T> T accept(ASTVisitor<T> v) {
	return v.visitFunDecl(this);
    }

    @Override
    public String toString() {
        String parameters = Arrays.toString(params.toArray());
        return String.format("%s(%s)", name, parameters.substring(1, parameters.length() - 1));
    }
}
