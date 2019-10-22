package ast;

import java.io.PrintWriter;

public class ASTPrinter implements ASTVisitor<Void> {

    private PrintWriter writer;

    public ASTPrinter(PrintWriter writer) {
            this.writer = writer;
    }

    @Override
    public Void visitBlock(Block b) {
        writer.print("Block(");
        String delimiter = "";
        for (VarDecl vd : b.variables) {
            writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }
        for (Stmt stmt : b.statements) {
            writer.print(delimiter);
            delimiter = ",";
            stmt.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitFunDecl(FunDecl fd) {
        writer.print("FunDecl(");
        fd.type.accept(this);
        writer.print(","+fd.name+",");
        for (VarDecl vd : fd.params) {
            vd.accept(this);
            writer.print(",");
        }
        fd.block.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitProgram(Program p) {
        writer.print("Program(");
        String delimiter = "";
        for (StructTypeDecl std : p.structTypeDecls) {
            writer.print(delimiter);
            delimiter = ",";
            std.accept(this);
        }
        for (VarDecl vd : p.varDecls) {
            writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }
        for (FunDecl fd : p.funDecls) {
            writer.print(delimiter);
            delimiter = ",";
            fd.accept(this);
        }
        writer.print(")");
	    writer.flush();
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl vd){
        writer.print("VarDecl(");
        vd.type.accept(this);
        writer.print(","+vd.varName);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        writer.print("VarExpr(");
        writer.print(v.name);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitBaseType(BaseType bt) {
        writer.print(bt);
        return null;
    }

    @Override
    public Void visitStructTypeDecl(StructTypeDecl st) {
        writer.print("StructTypeDecl(");
        st.structType.accept(this);
        String delimiter = ",";
        for(VarDecl vd : st.variables) {
            writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }
        writer.print(")");
        return null;
    }

    //------------------------------------

    @Override
    public Void visitFunCallExpr(FunCallExpr fc) {
        writer.print("FunCallExpr(");
        writer.print(fc.name);
        String delimiter = ",";
        for(Expr expression : fc.params) {
            writer.print(delimiter);
            delimiter = ",";
            expression.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitStructType(StructType st) {
        writer.print("StructType(");
        writer.print(st.name);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitPointerType(PointerType pt) {
        writer.print("PointerType(");
        pt.type.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitArrayType(ArrayType at) {
        writer.print("ArrayType(");
        at.type.accept(this);
        writer.print(","+at.numOfElems);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitIntLiteral(IntLiteral il) {
        writer.print("IntLiteral(");
        writer.print(il.value);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitStrLiteral(StrLiteral sl) {
        writer.print("StrLiteral(");
        writer.print(sl.value);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitChrLiteral(ChrLiteral cl) {
        writer.print("ChrLiteral(");
        writer.print(cl.value);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitBinOp(BinOp bo) {
        writer.print("BinOp(");
        bo.lhs.accept(this);
        bo.op.accept(this);
        bo.rhs.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitOp(Op o) {
        writer.print("Op(");
        writer.print(o);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(ArrayAccessExpr aa) {
        writer.print("ArrayAccessExpr(");
        aa.name.accept(this);
        writer.print(",");
        aa.index.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitFieldAccessExpr(FieldAccessExpr fa) {
        writer.print("FieldAccessExpr(");
        fa.name.accept(this);
        writer.print(","+fa.field);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitValueAtExpr(ValueAtExpr va) {
        writer.print("ValueAtExpr(");
        va.expression.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitSizeOfExpr(SizeOfExpr so) {
        writer.print("SizeOfExpr(");
        so.type.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitTypecastExpr(TypecastExpr tc) {
        writer.print("TypecastExpr(");
        tc.type.accept(this);
        writer.print(",");
        tc.expression.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt es) {
        writer.print("ExprStmt(");
        es.expression.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitWhile(While w) {
        writer.print("While(");
        w.expression.accept(this);
        writer.print(",");
        w.statement.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitIf(If i) {
        writer.print("If(");
        i.expression.accept(this);
        writer.print(",");
        i.ifStatement.accept(this);
        if (i.elseStatement != null) {
            writer.print(",");
            i.elseStatement.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitAssign(Assign a) {
        writer.print("Assign(");
        a.lhs.accept(this);
        writer.print(",");
        a.rhs.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitReturn(Return r) {
        writer.print("Return(");
        if (r.expression != null)
            r.expression.accept(this);
        writer.print(")");
        return null;
    }
    
}
