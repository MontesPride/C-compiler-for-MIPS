package sem;

import ast.*;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

	@Override
	public Type visitBaseType(BaseType bt) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitStructTypeDecl(StructTypeDecl st) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitBlock(Block b) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitFunDecl(FunDecl p) {
		// To be completed...
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
		// To be completed...
		return null;
	}

	@Override
	public Type visitVarExpr(VarExpr v) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitFunCallExpr(FunCallExpr fc) {
		// To be completed...
		return null;
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

	@Override
	public Type visitIntLiteral(IntLiteral il) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitStrLiteral(StrLiteral sl) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitChrLiteral(ChrLiteral cl) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitBinOp(BinOp bo) {
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
				if (lhsT.getClass().equals(StructType.class) || lhsT.getClass().equals(ArrayType.class) || lhsT.equals(BaseType.VOID) || rhsT.getClass().equals(StructType.class) || rhsT.getClass().equals(ArrayType.class) || rhsT.equals(BaseType.VOID)) {
					error("INVALID TYPES FOR THIS OPERATION(" + bo.op.toString() + "): lhsT = " + lhsT.toString() + ", rhsT = " + rhsT.toString());
					return null;
				} else if (!lhsT.equals(rhsT)) {
					error("TYPES DO NOT MATCH (" + bo.op.toString() + "): lhsT = " + lhsT.toString() + ", rhsT = " + rhsT.toString());
				} else {
					bo.type = lhsT;
				}
			}
			default:
				return null;
		}
	}

	@Override
	public Type visitOp(Op o) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitArrayAccessExpr(ArrayAccessExpr aa) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitFieldAccessExpr(FieldAccessExpr fa) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitValueAtExpr(ValueAtExpr va) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitSizeOfExpr(SizeOfExpr so) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitTypecastExpr(TypecastExpr tc) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitExprStmt(ExprStmt es) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitWhile(While w) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitIf(If i) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitAssign(Assign a) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitReturn(Return r) {
		// To be completed...
		return null;
	}

	// To be completed...


}
