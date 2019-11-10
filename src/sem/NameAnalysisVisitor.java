package sem;

import ast.*;

import java.util.*;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

    public Scope scope;

    public NameAnalysisVisitor() {
        this.scope = new Scope();

        addPredefinedFunctions();
    }

    private void addPredefinedFunctions() {
        List<VarDecl> params;

        params = new ArrayList<>();
        params.add(new VarDecl(new PointerType(BaseType.CHAR), "s"));
        scope.put(new FuncSymbol(new FunDecl(BaseType.VOID, "print_s", params, new Block(new ArrayList<>(), new ArrayList<>()), true)), false);

        params = new ArrayList<>();
        params.add(new VarDecl(BaseType.INT, "i"));
        scope.put(new FuncSymbol(new FunDecl(BaseType.VOID, "print_i", params, new Block(new ArrayList<>(), new ArrayList<>()), true)), false);

        params = new ArrayList<>();
        params.add(new VarDecl(BaseType.CHAR, "c"));
        scope.put(new FuncSymbol(new FunDecl(BaseType.VOID, "print_c", params, new Block(new ArrayList<>(), new ArrayList<>()), true)), false);

        params = new ArrayList<>();
        scope.put(new FuncSymbol(new FunDecl(BaseType.CHAR, "read_c", params, new Block(new ArrayList<>(), new ArrayList<>()), true)), false);

        params = new ArrayList<>();
        scope.put(new FuncSymbol(new FunDecl(BaseType.INT, "read_i", params, new Block(new ArrayList<>(), new ArrayList<>()), true)), false);

        params = new ArrayList<>();
        params.add(new VarDecl(BaseType.INT, "size"));
        scope.put(new FuncSymbol(new FunDecl(new PointerType(BaseType.VOID), "mcmalloc", params, new Block(new ArrayList<>(), new ArrayList<>()), true)), false);

    }

    public boolean putSymbol(String name, boolean isStructSymbol) {
        Symbol s = scope.lookupCurrent(name, isStructSymbol);
        if (s != null) {
            error("Symbol %s has been declared!", name);
            return true;
        }
        return false;
    }

    public <S extends Symbol> S getSymbol(String name, Class<S> symbolClass, boolean isStructSymbol) {
        Symbol s = scope.lookup(name, isStructSymbol);
        if (s == null) {
            error("Symbol %s has not been declared!", name);
            return null;
        }
        if (!symbolClass.isInstance(s)) {
            error("%s is not of Type: %s", name, symbolClass);
            return null;
        }
        return symbolClass.cast(s);
    }


    @Override
    public Void visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Void visitStructTypeDecl(StructTypeDecl sts) {
        if (putSymbol(sts.structType.name, true))
            return null;

        scope.put(new StructTypeSymbol(sts), true);
        sts.structType.accept(this);

        Scope oldScope = scope;
        scope = new Scope(oldScope);
        for (VarDecl vd : sts.variables)
            vd.accept(this);
        scope = oldScope;

        return null;
    }

    @Override
    public Void visitBlock(Block b) {
        return visitBlock(b, true);

    }

    // isNewScope is used to help with functions where
    // parameters need to be a part of an inner block
    public Void visitBlock(Block b, boolean isNewScope) {
        Scope oldScope = null;
        if (isNewScope) {
            oldScope = scope;
            scope = new Scope(oldScope);
        }

        for (VarDecl vd : b.variables)
            vd.accept(this);
        for (Stmt stmt : b.statements)
            stmt.accept(this);

        if (isNewScope) {
            scope = oldScope;
        }
        return null;
    }

    @Override
    public Void visitFunDecl(FunDecl fd) {
        if (putSymbol(fd.name, false)) {
            return null;
        }

        scope.put(new FuncSymbol(fd), false);

        Scope oldScope = scope;
        scope = new Scope(oldScope);
        fd.type.accept(this);

        for (VarDecl vd : fd.params)
            vd.accept(this);
        visitBlock(fd.block, false);
        scope = oldScope;

        return null;
    }


    @Override
    public Void visitProgram(Program p) {
        for (StructTypeDecl std : p.structTypeDecls)
            std.accept(this);
        for (VarDecl vd : p.varDecls)
            vd.accept(this);
        for (FunDecl fd : p.funDecls)
            fd.accept(this);
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl vd) {
        if (putSymbol(vd.varName, false)) {
            return null;
        }

        vd.type.accept(this);
        scope.put(new VarSymbol(vd), false);

        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        VarSymbol s = getSymbol(v.name, VarSymbol.class, false);

        if (s != null)
            v.vd = s.vd;

        // Avoid possible NullPointerExceptions
        if (v.vd == null)
            v.vd = new VarDecl(BaseType.VOID, v.name);

        return null;
    }

    @Override
    public Void visitFunCallExpr(FunCallExpr fc) {
        FuncSymbol s = getSymbol(fc.name, FuncSymbol.class, false);

        if (s != null)
            fc.fd = s.fd;

        // Avoid possible NullPointerExceptions
        if (fc.fd == null)
            fc.fd = new FunDecl(BaseType.VOID, fc.name, new ArrayList<>(), new Block(new ArrayList<>(), new ArrayList<>()));

        for (Expr e : fc.params)
            e.accept(this);

        return null;
    }

    @Override
    public Void visitStructType(StructType st) {
        StructTypeSymbol s = getSymbol(st.name, StructTypeSymbol.class, true);

        if (s != null)
            st.std = s.std;

        // Avoid possible NullPointerExceptions
        if (st.std == null)
            st.std = new StructTypeDecl(new StructType(st.name), new ArrayList<>());

        return null;
    }

    @Override
    public Void visitPointerType(PointerType pt) {
        pt.type.accept(this);
        return null;
    }

    @Override
    public Void visitArrayType(ArrayType at) {
        at.type.accept(this);
        return null;
    }

    @Override
    public Void visitIntLiteral(IntLiteral il) {
        return null;
    }

    @Override
    public Void visitStrLiteral(StrLiteral sl) {
        return null;
    }

    @Override
    public Void visitChrLiteral(ChrLiteral cl) {
        return null;
    }

    @Override
    public Void visitBinOp(BinOp bo) {
        bo.lhs.accept(this);
        bo.op.accept(this);
        bo.rhs.accept(this);
        return null;
    }

    @Override
    public Void visitOp(Op o) {
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(ArrayAccessExpr aa) {
        aa.name.accept(this);
        aa.index.accept(this);
        return null;
    }

    @Override
    public Void visitFieldAccessExpr(FieldAccessExpr fa) {
        fa.name.accept(this);
        return null;
    }

    @Override
    public Void visitValueAtExpr(ValueAtExpr va) {
        va.expression.accept(this);
        return null;
    }

    @Override
    public Void visitSizeOfExpr(SizeOfExpr so) {
        so.sizeOfType.accept(this);
        return null;
    }

    @Override
    public Void visitTypecastExpr(TypecastExpr tc) {
        tc.expression.accept(this);
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt es) {
        es.expression.accept(this);
        return null;
    }

    @Override
    public Void visitWhile(While w) {
        w.expression.accept(this);
        w.statement.accept(this);
        return null;
    }

    @Override
    public Void visitIf(If i) {
        i.expression.accept(this);
        i.ifStatement.accept(this);
        if (i.elseStatement != null)
            i.elseStatement.accept(this);
        return null;
    }

    @Override
    public Void visitAssign(Assign a) {
        a.lhs.accept(this);
        a.rhs.accept(this);
        return null;
    }

    @Override
    public Void visitReturn(Return r) {
        if (r.expression != null)
            r.expression.accept(this);
        return null;
    }

}
