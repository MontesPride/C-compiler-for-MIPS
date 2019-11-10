package ast;

public class FieldAccessExpr extends Expr {
    public final Expr name;
    public final String field;

    public FieldAccessExpr(Expr name, String field) {
        this.name = name;
        this.field = field;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitFieldAccessExpr(this);
    }

    @Override
    public String toString() { return name.toString() + "." + field; }
}
