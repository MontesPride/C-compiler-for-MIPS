package ast;

public class While extends Stmt {
    public final Expr expression;
    public final Stmt statement;

    public While(Expr expression, Stmt statement) {
        this.expression = expression;
        this.statement = statement;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitWhile(this);
    }
}
