package gen;

import ast.*;

public abstract class CodeGeneratorVisitor<T> implements ASTVisitor<T> {

    @Override
    public T visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public T visitStructTypeDecl(StructTypeDecl std) {
        std.structType.accept(this);
        for (VarDecl vd : std.variables)
            vd.accept(this);

        return null;
    }

    @Override
    public T visitProgram(Program p) {
        for (StructTypeDecl std : p.structTypeDecls)
            std.accept(this);
        for (VarDecl vd : p.varDecls)
            vd.accept(this);
        for (FunDecl fd : p.funDecls)
            fd.accept(this);

        return null;
    }

    @Override
    public T visitBlock(Block b) {
        for (VarDecl vd : b.variables)
            vd.accept(this);
        for (Stmt stmt : b.statements)
            stmt.accept(this);

        return null;
    }

    @Override
    public T visitFunDecl(FunDecl fd) {
        fd.type.accept(this);
        for (VarDecl vd : fd.params)
            vd.accept(this);
        fd.block.accept(this);

        return null;
    }


    @Override
    public T visitVarDecl(VarDecl vd) {
        vd.type.accept(this);

        return null;
    }

    @Override
    public T visitVarExpr(VarExpr v) { return null; }

    @Override
    public T visitFunCallExpr(FunCallExpr fc) {
        for (Expr expr : fc.params)
            expr.accept(this);

        return null;
    }

    @Override
    public T visitStructType(StructType st) { return null; }

    @Override
    public T visitPointerType(PointerType pt) {
        pt.type.accept(this);

        return null;
    }

    @Override
    public T visitArrayType(ArrayType at) {
        at.type.accept(this);

        return null;
    }

    @Override
    public T visitIntLiteral(IntLiteral il) {
        return null;
    }

    @Override
    public T visitChrLiteral(ChrLiteral cl) {
        return null;
    }

    @Override
    public T visitStrLiteral(StrLiteral sl) { return null; }

    @Override
    public T visitBinOp(BinOp bo) {
        bo.lhs.accept(this);
        bo.op.accept(this);
        bo.rhs.accept(this);

        return null;
    }

    @Override
    public T visitOp(Op o) {
        return null;
    }

    @Override
    public T visitArrayAccessExpr(ArrayAccessExpr aa) {
        aa.name.accept(this);
        aa.index.accept(this);

        return null;
    }

    @Override
    public T visitFieldAccessExpr(FieldAccessExpr fa) {
        fa.name.accept(this);

        return null;
    }

    @Override
    public T visitValueAtExpr(ValueAtExpr va) {
        va.expression.accept(this);

        return null;
    }

    @Override
    public T visitSizeOfExpr(SizeOfExpr so) {
        so.sizeOfType.accept(this);

        return null;
    }

    @Override
    public T visitTypecastExpr(TypecastExpr tc) {
        tc.castType.accept(this);
        tc.expression.accept(this);

        return null;
    }

    @Override
    public T visitExprStmt(ExprStmt es) {
        es.expression.accept(this);

        return null;
    }

    @Override
    public T visitWhile(While w) {
        w.expression.accept(this);
        w.statement.accept(this);

        return null;
    }

    @Override
    public T visitIf(If i) {
        i.expression.accept(this);
        i.ifStatement.accept(this);
        if (i.elseStatement != null)
            i.elseStatement.accept(this);

        return null;
    }

    @Override
    public T visitAssign(Assign a) {
        a.lhs.accept(this);
        a.rhs.accept(this);

        return null;
    }

    @Override
    public T visitReturn(Return r) {
        if (r.expression != null)
            r.expression.accept(this);

        return null;
    }


}
