package ast;

public class TypecastExpr extends Expr {
    public final Type castType;
    public final Expr expression;

    public TypecastExpr(Type castType, Expr expression) {
        this.castType = castType;
        this.expression = expression;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitTypecastExpr(this);
    }

    @Override
    public String toString() { return "(" + castType.toString() + ") " + expression.toString(); }
}
