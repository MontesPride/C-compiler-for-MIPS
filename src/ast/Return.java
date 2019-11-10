package ast;

public class Return extends Stmt {
    public final Expr expression;

    public Return(Expr expression) {
        this.expression = expression;
    }

    public Return() {
        this.expression = null;
    }

    public <T> T accept(ASTVisitor<T> v) { return v.visitReturn(this); }

    @Override
    public String toString() {
        if (expression == null) {
            return "return;";
        }
        return "return " + expression.toString() + ";";
    }
}
