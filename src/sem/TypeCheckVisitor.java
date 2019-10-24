package sem;

import ast.*;

import java.util.Arrays;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

	@Override
	public Type visitBaseType(BaseType bt) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitStructTypeDecl(StructTypeDecl sts) {
        return null;
	}

	@Override
	public Type visitBlock(Block b) {
        for (VarDecl vd : b.variables)
            visitVarDecl(vd);
        for (Stmt stmt : b.statements)
            visitStatement(stmt);
		return null;
	}

    public void visitStatement(Stmt stmt) {
        switch (stmt.getClass().getSimpleName()) {
            case "Block": {
                visitBlock((Block) stmt);
                return;
            }
            case "While": {
                visitWhile((While) stmt);
                return;
            }
            case "If": {
                visitIf((If) stmt);
                return;
            }
            case "Assign": {
                visitAssign((Assign) stmt);
                return;
            }
            case "Return": {
                visitReturn((Return) stmt);
                return;
            }
            case "ExprStmt": {
                visitExprStmt((ExprStmt) stmt);
            }
        }
    }

	@Override
	public Type visitFunDecl(FunDecl fd) {
	    for (VarDecl vd : fd.params)
	        visitVarDecl(vd);
	    visitBlock(fd.block);
        return null;
	}


	@Override
	public Type visitProgram(Program p) {
		for (StructTypeDecl std : p.structTypeDecls)
			visitStructTypeDecl(std);
		for (VarDecl vd : p.varDecls)
			visitVarDecl(vd);
		for (FunDecl fd : p.funDecls)
			visitFunDecl(fd);
		return null;
	}

	@Override
	public Type visitVarDecl(VarDecl vd) {
        return null;
	}

	@Override
	public Type visitVarExpr(VarExpr v) {
	    try {
            v.type = v.vd.type;
            return v.type;
        } catch (Exception e) { return null; }
	}

	@Override
	public Type visitFunCallExpr(FunCallExpr fc) {
	    /*try {
            if (fc.fd.params.size() != fc.params.size()) {
                error("Invalid number of parameters");
                return null;
            }
            for (int i = 0; i < fc.params.size(); i++) {
                Type paramType = fc.params.get(i).accept(this);
                if (fc.fd.params.get(i).type != paramType) {
                    error("Invalid Type of parameter " + fc.fd.params.get(i).varName);
                    return null;
                }
            }
            fc.type = fc.fd.type;
            return fc.fd.type;
        } catch (Exception e) { return null; }*/
	    try {
	        return fc.fd.type;
        } catch (Exception e) { return null; }
	}

	@Override
	public Type visitStructType(StructType st) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitPointerType(PointerType pt) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitArrayType(ArrayType at) {
		// To be completed...
		return null;
	}

    public Type visitExpression(Expr expression) {
        switch (expression.getClass().getSimpleName()) {
            case "IntLiteral": {
                return visitIntLiteral((IntLiteral) expression);
            }
            case "StrLiteral": {
                return visitStrLiteral((StrLiteral) expression);
            }
            case "ChrLiteral": {
                return visitChrLiteral((ChrLiteral) expression);
            }
            case "VarExpr": {
                return visitVarExpr((VarExpr) expression);
            }
            case "FunCallExpr": {
                return visitFunCallExpr((FunCallExpr) expression);
            }
            case "BinOp": {
                return visitBinOp((BinOp) expression);
            }
            case "ArrayAccessExpr": {
                return visitArrayAccessExpr((ArrayAccessExpr) expression);
            }
            case "FieldAccessExpr": {
                return visitFieldAccessExpr((FieldAccessExpr) expression);
            }
            case "ValueAtExpr": {
                return visitValueAtExpr((ValueAtExpr) expression);
            }
            case "SizeOfExpr": {
                return visitSizeOfExpr((SizeOfExpr) expression);
            }
            case "TypecastExpr": {
                return visitTypecastExpr((TypecastExpr) expression);
            }
        }
        return null;
    }

	@Override
	public Type visitIntLiteral(IntLiteral il) {
	    il.type = BaseType.INT;
		return BaseType.INT;
	}

	@Override
	public Type visitStrLiteral(StrLiteral sl) {
	    sl.type = BaseType.CHAR;
		return new PointerType(BaseType.CHAR);
	}

	@Override
	public Type visitChrLiteral(ChrLiteral cl) {
	    cl.type = BaseType.CHAR;
		return BaseType.CHAR;
	}

	@Override
	public Type visitBinOp(BinOp bo) {
	    try {
            Type lhsT = bo.lhs.accept(this);
            Type rhsT = bo.rhs.accept(this);
            switch (bo.op) {
                case ADD:
                case SUB:
                case MUL:
                case DIV:
                case MOD:
                case OR:
                case AND:
                case GT:
                case LT:
                case GE:
                case LE: {
                    if (lhsT.equals(BaseType.INT) && rhsT.equals(BaseType.INT)) {
                        bo.type = BaseType.INT;
                        return BaseType.INT;
                    } else {
                        error("INVALID TYPES FOR THIS OPERATION(" + bo.op.toString() + "): lhsT = " + lhsT.toString() + ", rhsT = " + rhsT.toString());
                        return null;
                    }
                }
                case EQ:
                case NE: {
                    if ((lhsT.getClass().equals(StructType.class) || lhsT.getClass().equals(ArrayType.class) || lhsT.equals(BaseType.VOID)) || (rhsT.getClass().equals(StructType.class) || rhsT.getClass().equals(ArrayType.class) || rhsT.equals(BaseType.VOID))) {
                        error("INVALID TYPES FOR THIS OPERATION(" + bo.op.toString() + "): lhsT = " + lhsT.toString() + ", rhsT = " + rhsT.toString());
                        return null;
                    } else if (!lhsT.getClass().getSimpleName().equals(rhsT.getClass().getSimpleName())) {
                        error("TYPES DO NOT MATCH (" + bo.op.toString() + "): lhsT = " + lhsT.toString() + ", rhsT = " + rhsT.toString());
                        return null;
                    } else {
                        bo.type = lhsT;
                        return lhsT;
                    }
                }
                default:
                    error("INVALID OPERATION" + bo.op);
                    return null;
            }
        } catch (Exception e) { return null; }
	}

	@Override
	public Type visitOp(Op o) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitArrayAccessExpr(ArrayAccessExpr aa) {
		if (!aa.index.accept(this).equals(BaseType.INT)) {
		    error("Invalid array index");
		    return null;
        }
		if (!aa.name.accept(this).getClass().equals(ArrayType.class) && !aa.name.accept(this).getClass().equals(PointerType.class)) {
            error("Invalid type of array");
            return null;
        }
		return aa.name.accept(this);
	}

	@Override
	public Type visitFieldAccessExpr(FieldAccessExpr fa) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitValueAtExpr(ValueAtExpr va) {
        if (!va.expression.accept(this).getClass().equals(PointerType.class)) {
            error("Invalid ValueAt Type");
            return null;
        }
        va.type = va.expression.accept(this);
		return va.type;
	}

	@Override
	public Type visitSizeOfExpr(SizeOfExpr so) {
		return BaseType.INT;
	}

	@Override
	public Type visitTypecastExpr(TypecastExpr tc) {
        visitExpression(tc.expression);
        tc.expression.type = tc.expression.accept(this);
		return tc.type;
	}

	@Override
	public Type visitExprStmt(ExprStmt es) {
        visitExpression(es.expression);
        es.expression.type = es.expression.accept(this);
		return es.expression.type;
	}

	@Override
	public Type visitWhile(While w) {
	    try {
            if (!w.expression.accept(this).equals(BaseType.INT)){
                error("Invalid while expression");
            }
            visitStatement(w.statement);
        } catch (Exception ignored) {}
		return null;
	}

	@Override
	public Type visitIf(If i) {
	    try {
            if (!i.expression.accept(this).equals(BaseType.INT)){
                error("Invalid If expression");
            }
            visitStatement(i.ifStatement);
            if (i.elseStatement != null)
                visitStatement(i.elseStatement);
        } catch (Exception ignored) {}

		return null;
	}

	@Override
	public Type visitAssign(Assign a) {
	    return null;
	    /*
	    try {
            Type lhsT = a.lhs.accept(this);
            Type rhsT = a.rhs.accept(this);
            if (lhsT != rhsT) {
                error("Incorrect assign type");
                return null;
            } else {
                return lhsT;
            }
        } catch (Exception e) { return null; }*/
	}

	@Override
	public Type visitReturn(Return r) {
		// To be completed...
		return null;
	}

	// To be completed...


}
