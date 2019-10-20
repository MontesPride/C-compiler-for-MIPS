package ast;

public class TypecastExpr extends Expr {
    public final Type type;
    public final Expr expression;

    public TypecastExpr(Type type, Expr expression) {
        this.type = type;
        this.expression = expression;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitTypecastExpr(this);
    }
}
