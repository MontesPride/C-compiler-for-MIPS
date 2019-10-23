package sem;

import ast.*;

import java.util.HashMap;
import java.util.Map;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

    public Scope scope;
    public Map<String, Symbol> funDeclHelper;

    public NameAnalysisVisitor() {
        this.scope = new Scope();
        this.funDeclHelper = new HashMap<>();
    }

    public NameAnalysisVisitor(Scope scope) {
        this.scope = scope;
        this.funDeclHelper = new HashMap<>();
    }

    @Override
    public Void visitBaseType(BaseType bt) {
        // To be completed...
        return null;
    }

    @Override
    public Void visitStructTypeDecl(StructTypeDecl sts) {
        Symbol s = scope.lookupCurrent(sts.structType.name);
        if (s != null)
            error("StructType" + sts.structType.name + "already declared");
        else
            scope.put(new StructTypeSymbol(sts));
        return null;
    }

    @Override
    public Void visitBlock(Block b) {
        Scope oldScope = scope;
        scope = new Scope(oldScope);
        for (VarDecl vd : b.variables)
            visitVarDecl(vd);
        for (Stmt stmt : b.statements)
            visitStatement(stmt);
        scope = oldScope;
        return null;
    }

    public void visitStatement(Stmt stmt) {
        switch (stmt.getClass().getSimpleName()) {
            case "Block": {
                visitBlock((Block) stmt);
                break;
            }
            case "While": {
                visitWhile((While) stmt);
                break;
            }
            case "If": {
                visitIf((If) stmt);
                break;
            }
            case "Assign": {
                visitAssign((Assign) stmt);
                break;
            }
            case "Return": {
                visitReturn((Return) stmt);
                break;
            }
            case "ExprStmt": {
                visitExprStmt((ExprStmt) stmt);
                break;
            }
        }
    }

    @Override
    public Void visitFunDecl(FunDecl fd) {
        Symbol s = scope.lookupCurrent(fd.name);
        if (s != null)
            error("Function " + fd.name + " already declared");
        else
            scope.put(new FuncSymbol(fd));

        funDeclHelper = new HashMap<>();
        for (VarDecl vd : fd.params)
            funDeclHelper.put(vd.varName, new VarSymbol(vd));
        visitBlock(fd.block);
        funDeclHelper.clear();

        return null;
    }


    @Override
    public Void visitProgram(Program p) {
        for (StructTypeDecl std : p.structTypeDecls)
            visitStructTypeDecl(std);
        for (VarDecl vd : p.varDecls)
            visitVarDecl(vd);
        for (FunDecl fd : p.funDecls)
            visitFunDecl(fd);
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl vd) {
        Symbol s = funDeclHelper.get(vd.varName);
        if (s == null)
            s = scope.lookupCurrent(vd.varName);
        if (s != null)
            error("Variable " + vd.varName + " already declared!");
        else
            scope.put(new VarSymbol(vd));
        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        Symbol s = funDeclHelper.get(v.name);
        if (s == null)
            s = scope.lookup(v.name);
        if (s == null) {
            error("Variable " + v.name + " not declared!");
        } else if (!s.isVar()) {
            error(v.name + "is not declared as a variable");
        } else {
            v.vd = ((VarSymbol) s).vd;
        }
        return null;
    }

    @Override
    public Void visitFunCallExpr(FunCallExpr fc) {
        Symbol s = scope.lookup(fc.name);
        if (s == null) {
            error("Function " + fc.name + " not declared!");
        } else if (!s.isFunc()) {
            error(fc.name + "is not declared as a function");
        }
        return null;
    }

    @Override
    public Void visitStructType(StructType st) {
        Symbol s = scope.lookup(st.name);
        if (s == null) {
            error("StructType " + st.name + " not declared!");
        } else if (!s.isStruct()) {
            error(st.name + "is not declared as a StructType");
        }
        return null;
    }

    @Override
    public Void visitPointerType(PointerType pt) {
        return null;
    }

    @Override
    public Void visitArrayType(ArrayType at) {

        return null;
    }

    public void visitExpression(Expr expression) {

    }

    @Override
    public Void visitIntLiteral(IntLiteral il) {
        // To be completed...
        return null;
    }

    @Override
    public Void visitStrLiteral(StrLiteral sl) {
        // To be completed...
        return null;
    }

    @Override
    public Void visitChrLiteral(ChrLiteral cl) {
        // To be completed...
        return null;
    }

    @Override
    public Void visitBinOp(BinOp bo) {
        return null;
    }

    @Override
    public Void visitOp(Op o) {
        // To be completed...
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(ArrayAccessExpr aa) {
        return null;
    }

    @Override
    public Void visitFieldAccessExpr(FieldAccessExpr fa) {
        if (fa.name.getClass().equals(VarExpr.class)) {
            visitVarExpr((VarExpr)(fa.name));
            VarExpr ve = (VarExpr)(fa.name);
            if (ve.vd.type != null && ve.vd.type.getClass().equals(StructType.class)) {
                StructTypeSymbol s = (StructTypeSymbol)scope.lookup(ve.name);
                for (VarDecl vd : s.std.variables) {
                    if (vd.varName.equals(fa.field))
                        return null;

                }
            }
            error("Field access not allowed for " + ve.name);
        }
        return null;
    }

    @Override
    public Void visitValueAtExpr(ValueAtExpr va) {
        // To be completed...
        return null;
    }

    @Override
    public Void visitSizeOfExpr(SizeOfExpr so) {
        // To be completed...
        return null;
    }

    @Override
    public Void visitTypecastExpr(TypecastExpr tc) {
        // To be completed...
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt es) {
        // To be completed...
        return null;
    }

    @Override
    public Void visitWhile(While w) {
        visitExpression(w.expression);
        visitStatement(w.statement);
        return null;
    }

    @Override
    public Void visitIf(If i) {
        visitExpression(i.expression);
        visitStatement(i.ifStatement);
        if (i.elseStatement != null)
            visitStatement(i.elseStatement);
        return null;
    }

    @Override
    public Void visitAssign(Assign a) {
        // To be completed...
        return null;
    }

    @Override
    public Void visitReturn(Return r) {
        // To be completed...
        return null;
    }


    // To be completed...


}
