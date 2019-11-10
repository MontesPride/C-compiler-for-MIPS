package ast;

public class ArrayAccessExpr extends Expr {
    public final Expr name;
    public final Expr index;

    public ArrayAccessExpr(Expr name, Expr index) {
        this.name = name;
        this.index = index;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayAccessExpr(this);
    }

    @Override
    public String toString() {
        return name.toString() + "[" + index.toString() + "]";
    }
}
