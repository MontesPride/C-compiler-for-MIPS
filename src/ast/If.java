package ast;

public class If extends Stmt {
    public final Expr expression;
    public final Stmt ifStatement;
    public final Stmt elseStatement;

    public If(Expr expression, Stmt ifStatement, Stmt elseStatement) {
        this.expression = expression;
        this.ifStatement = ifStatement;
        this.elseStatement = elseStatement;
    }

    public If(Expr expression, Stmt ifStatement) {
        this.expression = expression;
        this.ifStatement = ifStatement;
        this.elseStatement = null;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitIf(this);
    }
}
