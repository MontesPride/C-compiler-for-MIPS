package ast;

public class SizeOfExpr extends Expr {
    public final Type sizeOfType;

    public SizeOfExpr(Type sizeOfType) {
        this.sizeOfType = sizeOfType;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitSizeOfExpr(this);
    }

    @Override
    public String toString() {
        return "sizeof(" + sizeOfType.toString() + ")";
    }
}
