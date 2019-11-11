package ast;

public class PointerType implements Type {
    public final Type type;

    public PointerType(Type type) {
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> v) { return v.visitPointerType(this); }

    @Override
    public String toString() { return "*" + type.toString(); }

    @Override
    public int sizeOf() { return BaseType.INT.sizeOf(); }
}
