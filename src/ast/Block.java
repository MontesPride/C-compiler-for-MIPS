package ast;

import java.util.List;

public class Block extends Stmt {
    public final List<VarDecl> variables;
    public final List<Stmt> statements;

    public Block(List<VarDecl> variables, List<Stmt> statements) {
        this.variables = variables;
        this.statements = statements;
    }

    public <T> T accept(ASTVisitor<T> v) {
	    return v.visitBlock(this);
    }
}
