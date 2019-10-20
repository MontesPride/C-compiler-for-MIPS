package ast;

public class ArrayType implements Type {
    public final Type type;
    public final int numOfElems;

    public ArrayType(Type type, int numOfElems) {
        this.type = type;
        this.numOfElems = numOfElems;
    }

    public <T> T accept(ASTVisitor<T> v) { return v.visitArrayType(this); }
}
