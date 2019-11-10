package ast;

public class ValueAtExpr extends Expr {
    public final Expr expression;

    public ValueAtExpr(Expr expression) {
        this.expression = expression;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitValueAtExpr(this);
    }

    @Override
    public String toString() { return "*" + expression.toString(); }
}
