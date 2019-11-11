package ast;

public enum BaseType implements Type {
    INT, CHAR, VOID;

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitBaseType(this);
    }

    @Override
    public int sizeOf() {
        switch (this) {
            case INT:
                return 4;
            case CHAR:
                return 1;
            case VOID:
                return 0;
            default:
                throw new RuntimeException("Sizeof called on " + this.toString());
        }
    }

}
