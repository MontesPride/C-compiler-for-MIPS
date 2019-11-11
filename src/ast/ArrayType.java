package ast;

import static gen.CodeGenerator.allignTo4Bytes;

public class ArrayType implements Type {
    public final Type type;
    public final int numOfElems;

    public ArrayType(Type type, int numOfElems) {
        this.type = type;
        this.numOfElems = numOfElems;
    }

    public <T> T accept(ASTVisitor<T> v) { return v.visitArrayType(this); }

    @Override
    public String toString() { return type.toString() + "[" + numOfElems + "]"; }

    @Override
    public int sizeOf() { return allignTo4Bytes(numOfElems * type.sizeOf()); }
}
