package sem;

import ast.*;

import java.util.*;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

    public Scope scope;
    public Map<String, Symbol> funDeclHelper;

    public NameAnalysisVisitor() {
        this.scope = new Scope();
        this.funDeclHelper = new HashMap<>();

        addPredefinedFunctions();
    }

    private void addPredefinedFunctions() {
        List<VarDecl> params;

        params = new ArrayList<>();
        params.add(new VarDecl(new PointerType(BaseType.CHAR), "s"));
        scope.put(new FuncSymbol(new FunDecl(BaseType.VOID, "print_s", params, new Block(new ArrayList<>(), new ArrayList<>()))));

        params = new ArrayList<>();
        params.add(new VarDecl(BaseType.INT, "i"));
        scope.put(new FuncSymbol(new FunDecl(BaseType.VOID, "print_i", params, new Block(new ArrayList<>(), new ArrayList<>()))));

        params = new ArrayList<>();
        params.add(new VarDecl(BaseType.CHAR, "c"));
        scope.put(new FuncSymbol(new FunDecl(BaseType.VOID, "print_c", params, new Block(new ArrayList<>(), new ArrayList<>()))));

        params = new ArrayList<>();
        scope.put(new FuncSymbol(new FunDecl(BaseType.VOID, "read_c", params, new Block(new ArrayList<>(), new ArrayList<>()))));

        params = new ArrayList<>();
        scope.put(new FuncSymbol(new FunDecl(BaseType.VOID, "read_i", params, new Block(new ArrayList<>(), new ArrayList<>()))));

        params = new ArrayList<>();
        params.add(new VarDecl(BaseType.INT, "size"));
        scope.put(new FuncSymbol(new FunDecl(new PointerType(BaseType.VOID), "mcmalloc", params, new Block(new ArrayList<>(), new ArrayList<>()))));

    }

    @Override
    public Void visitBaseType(BaseType bt) {
        // To be completed...
        return null;
    }

    @Override
    public Void visitStructTypeDecl(StructTypeDecl sts) {
        Symbol s = scope.lookupCurrent(sts.structType.name);
        if (s != null && s.isStruct())
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
        else if (checkIfTypeDeclared(fd.type))
            scope.put(new FuncSymbol(fd));
        else
            error("Function " + fd.name + " has incorrect return type " + fd.type);

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
        else if (checkIfTypeDeclared(vd.type) && !vd.type.equals(BaseType.VOID))
            scope.put(new VarSymbol(vd));
        else
            error("Variable " + vd.varName + " has incorrect type " + vd.type);
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
        } else {
            fc.fd = ((FuncSymbol) s).fd;
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
        // To be completed...
        return null;
    }

    @Override
    public Void visitArrayType(ArrayType at) {
        // To be completed...
        return null;
    }

    public void visitExpression(Expr expression) {
        switch (expression.getClass().getSimpleName()) {
            case "IntLiteral": {
                visitIntLiteral((IntLiteral) expression);
                break;
            }
            case "StrLiteral": {
                visitStrLiteral((StrLiteral) expression);
                break;
            }
            case "ChrLiteral": {
                visitChrLiteral((ChrLiteral) expression);
                break;
            }
            case "VarExpr": {
                visitVarExpr((VarExpr) expression);
                break;
            }
            case "FunCallExpr": {
                visitFunCallExpr((FunCallExpr) expression);
                break;
            }
            case "BinOp": {
                visitBinOp((BinOp) expression);
                break;
            }
            case "ArrayAccessExpr": {
                visitArrayAccessExpr((ArrayAccessExpr) expression);
                break;
            }
            case "FieldAccessExpr": {
                visitFieldAccessExpr((FieldAccessExpr) expression);
                break;
            }
            case "ValueAtExpr": {
                visitValueAtExpr((ValueAtExpr) expression);
                break;
            }
            case "SizeOfExpr": {
                visitSizeOfExpr((SizeOfExpr) expression);
                break;
            }
            case "TypecastExpr": {
                visitTypecastExpr((TypecastExpr) expression);
                break;
            }
        }
    }

    public boolean checkIfTypeDeclared(Type type) {
        if (type.equals(BaseType.INT) || type.equals(BaseType.CHAR) || type.equals(BaseType.VOID))
            return true;
        if (type.getClass().equals(ArrayType.class)) {
            ArrayType arrayType = (ArrayType) type;
            return checkIfTypeDeclared(arrayType.type);
        }
        if (type.getClass().equals(PointerType.class)) {
            PointerType pointerType = (PointerType) type;
            return checkIfTypeDeclared(pointerType.type);
        }
        if (type.getClass().equals(StructType.class)) {
            StructType structType = (StructType) type;
            return scope.lookup(structType.name) != null;
        }
        return true;
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
        // To be completed...
        return null;
    }

    @Override
    public Void visitOp(Op o) {
        // To be completed...
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(ArrayAccessExpr aa) {
        visitExpression(aa.name);
        return null;
    }

    @Override
    public Void visitFieldAccessExpr(FieldAccessExpr fa) {
        if (fa.name.getClass().equals(VarExpr.class)) {
            visitVarExpr((VarExpr) (fa.name));
            VarExpr ve = (VarExpr) (fa.name);
            if (ve.vd != null && ve.vd.type.getClass().equals(StructType.class)) {
                VarSymbol var = (VarSymbol) scope.lookup(ve.name);
                String structName = ((StructType) (var.vd.type)).name;
                if (scope.lookup(structName) != null && scope.lookup(structName).isStruct()) {
                    StructTypeSymbol structSymbol = (StructTypeSymbol) (scope.lookup(structName));
                    for (VarDecl vd : structSymbol.std.variables) {
                        if (vd.varName.equals(fa.field))
                            return null;
                    }
                }
            }
            error("Field access " + fa.field + " not allowed for variable " + ve.name);
        }
        return null;
    }

    @Override
    public Void visitValueAtExpr(ValueAtExpr va) {
        visitExpression(va.expression);
        return null;
    }

    @Override
    public Void visitSizeOfExpr(SizeOfExpr so) {
        if (!checkIfTypeDeclared(so.type))
            error("SizeOfExpr of type that has not been declared");
        return null;
    }

    @Override
    public Void visitTypecastExpr(TypecastExpr tc) {
        visitExpression(tc.expression);
        if (!checkIfTypeDeclared(tc.type))
            error("TypecastExpr of type that has not been declared");
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt es) {
        visitExpression(es.expression);
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
        visitExpression(a.lhs);
        visitExpression(a.rhs);
        return null;
    }

    @Override
    public Void visitReturn(Return r) {
        if (r.expression != null)
            visitExpression(r.expression);
        return null;
    }

}
