package sem;

import ast.*;

import java.util.*;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

    public boolean isReturnable(Stmt stmt) {
        return (stmt instanceof Block || stmt instanceof If || stmt instanceof While || stmt instanceof Return);
    }

	@Override
	public Type visitBaseType(BaseType bt) {
		return bt;
	}

	@Override
	public Type visitStructTypeDecl(StructTypeDecl sts) {
	    return sts.structType.accept(this);
	}

	@Override
	public Type visitBlock(Block b) {
        for (VarDecl vd : b.variables)
            vd.accept(this);

        List<Type> returnTypes = new ArrayList<>();
        for (Stmt stmt : b.statements) {
            Type returnType = stmt.accept(this);
            if (!isReturnable(stmt) || returnType == null)
                continue;
            returnTypes.add(returnType);
        }

        if (returnTypes.isEmpty())
            return null;

        Type lastChecked = null;
        boolean isSameType = true;
        for (Type type : returnTypes) {
            if (lastChecked == null) {
                lastChecked = type;
            }
            else if (!isSameType(lastChecked, type)) {
                isSameType = false;
                break;
            }
        }

        if (!isSameType)
            error("Block returnTypes are not unified. ReturnTypes: %s", Arrays.toString(returnTypes.toArray()));

		return returnTypes.get(0);
	}

	@Override
	public Type visitFunDecl(FunDecl fd) {
        if (fd.isPreDefined)
            return fd.type;

	    for (VarDecl vd : fd.params)
	        vd.accept(this);

	    Type returnType = fd.block.accept(this);
	    if (returnType == null)
            returnType = BaseType.VOID;

        if (!isSameType(returnType, fd.type))
            error("Function (%s) return type (%s) does not match, the return type of the Block (%s)", fd.name, fd.type, returnType);

        return fd.type;
	}


	@Override
	public Type visitProgram(Program p) {
        Type returnType = BaseType.VOID;

		for (StructTypeDecl std : p.structTypeDecls)
			std.accept(this);
		for (VarDecl vd : p.varDecls)
			vd.accept(this);
		for (FunDecl fd : p.funDecls)
		    if (fd.name.equals("main"))
		        returnType = fd.accept(this);
		    else
			    fd.accept(this);

        return returnType;
	}

	@Override
	public Type visitVarDecl(VarDecl vd) {
        if (vd.type == BaseType.VOID)
            error("Cannot declare VOID variable %s", vd.varName);

        return vd.type;
	}

	@Override
	public Type visitVarExpr(VarExpr v) {
        v.type = v.vd.type;
        return v.type;
	}

	@Override
	public Type visitFunCallExpr(FunCallExpr fc) {
        if (fc.fd == null) {
            error("Could not perform funcall %s%s, %s %s because it has not been declared!", fc.name, Arrays.toString(fc.params.toArray()), fc.name);
            return BaseType.VOID;
        }

        fc.type = fc.fd.type;

        if (fc.params.size() != fc.fd.params.size()) {
            error("Could not call %s, expected %d arguments, but received %d", fc.fd.name, fc.fd.params.size(), fc.params.size());
            return fc.type;
        }

        for (int i = 0; i < fc.params.size(); i++) {
            Expr arg = fc.params.get(i);
            VarDecl param = fc.fd.params.get(i);

            Type argType = arg.accept(this);
            if (!isSameType(argType, param.type)) {
                error("Could not call %s, param `%s` was incorrectly given type %s", fc.fd, param, argType);
            }
        }

        return fc.type;
	}

	@Override
	public Type visitStructType(StructType st) {
		return st;
	}

	@Override
	public Type visitPointerType(PointerType pt) {
        pt.type.accept(this);
		return pt;
	}

	@Override
	public Type visitArrayType(ArrayType at) {
		return at;
	}

	@Override
	public Type visitIntLiteral(IntLiteral il) {
	    il.type = BaseType.INT;
		return il.type;
	}

	@Override
	public Type visitStrLiteral(StrLiteral sl) {
	    sl.type = new ArrayType(BaseType.CHAR, sl.value.length() + 1);
		return sl.type;
	}

	@Override
	public Type visitChrLiteral(ChrLiteral cl) {
	    cl.type = BaseType.CHAR;
		return cl.type;
	}

	@Override
	public Type visitBinOp(BinOp bo) {
        Type lhsType = bo.lhs.accept(this);
        Type rhsType = bo.rhs.accept(this);
        bo.type = BaseType.INT;
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
                if (lhsType == BaseType.INT && rhsType == BaseType.INT)
                    return bo.type.accept(this);
                error("Operation %s expects INT and INT, but received %s and %s", bo.op, lhsType, rhsType);
                return bo.type;
            }
            case EQ:
            case NE: {
                if (isSameType(lhsType, rhsType) && !(lhsType instanceof StructType) && !(lhsType instanceof ArrayType) && lhsType != BaseType.VOID)
                    return bo.type.accept(this);
                error("Operation %s expects matching BaseTypes, but received %s and %s", bo.op, lhsType, rhsType);
                return bo.type;
            }
            default:
                error("Invalid Operation" + bo.op);
                return null;
        }
	}

	@Override
	public Type visitOp(Op o) {
		return null;
	}

	@Override
	public Type visitArrayAccessExpr(ArrayAccessExpr aa) {
        Type exprType = aa.name.accept(this);
        Type indexType = aa.index.accept(this);

        if (indexType != BaseType.INT)
            error("Expected INT, but received %s", indexType);

        if (!(exprType instanceof ArrayType) && !(exprType instanceof PointerType)) {
            error("Expected ArrayType or PointerType, but received %s", exprType);
            return exprType;
        }

        Type returnType;
        if (exprType instanceof ArrayType)
            returnType = ((ArrayType) exprType).type;
        else
            returnType = ((PointerType) exprType).type;

        aa.type = returnType;
        return aa.type.accept(this);
	}

	@Override
	public Type visitFieldAccessExpr(FieldAccessExpr fa) {
        Type exprType = fa.name.accept(this);

        if (!(exprType instanceof StructType)) {
            error("Expression is not a struct");
            return BaseType.VOID;
        }

        StructTypeDecl std = ((StructType) exprType).std;
        VarDecl varDecl = null;
        for (VarDecl vd : std.variables) {
            if (vd.varName.equals(fa.field)) {
                varDecl = vd;
                break;
            }
        }

        if (varDecl == null) {
            error("Field %s does not exist in struct", fa.field);
            return BaseType.VOID;
        }

        fa.type = varDecl.type.accept(this);
        return fa.type;
	}

	@Override
	public Type visitValueAtExpr(ValueAtExpr va) {
        Type pointerType = va.expression.accept(this);
        if (!(pointerType instanceof PointerType)) {
            error("Expression %s should be PointerType, but received %s", va, pointerType);
            va.type = BaseType.VOID;
            return va.type;
        }

        va.type = ((PointerType) pointerType).type;
        return va.type;
	}

	@Override
	public Type visitSizeOfExpr(SizeOfExpr so) {
	    so.type = BaseType.INT;
		return so.type;
	}

	@Override
	public Type visitTypecastExpr(TypecastExpr tc) {
        Type castToType = tc.castType;
        Type castFromType = tc.expression.accept(this);

        boolean isCastValid = false;

        if (castFromType == BaseType.CHAR && castToType == BaseType.INT) {
            isCastValid = true;
        } else if (castFromType instanceof ArrayType && castToType instanceof PointerType) {
            ArrayType castFrom = (ArrayType) castFromType;
            PointerType castTo = (PointerType) castToType;
            isCastValid = isSameType(castFrom.type, castTo.type);
        } else if (castFromType instanceof PointerType && castToType instanceof PointerType) {
            isCastValid = true;
        }

        if (!isCastValid)
            error("Invalid cast from %s to %s", castFromType, castToType);

        tc.type = castToType;
        return tc.type;
	}

	@Override
	public Type visitExprStmt(ExprStmt es) {
        return es.expression.accept(this);
	}

	@Override
	public Type visitWhile(While w) {
        Type expr = w.expression.accept(this);
        if (expr != BaseType.INT)
            error("Expression should be of type INT but received %s", expr);

        Type returnType = w.statement.accept(this);

        if (isReturnable(w.statement))
            return returnType;
        else
            return null;
	}

	@Override
	public Type visitIf(If i) {
        Type exprType = i.expression.accept(this);
        if (exprType != BaseType.INT)
            error("Expression should be of type INT, but received %s", exprType);

        Type ifStatementType = i.ifStatement.accept(this);

        Type returnType;
        if (isReturnable(i.ifStatement))
            returnType = ifStatementType;
        else
            returnType =  null;

        if (i.elseStatement != null) {
            Type elseStatementType = i.elseStatement.accept(this);

            if (elseStatementType != null) {
                if (returnType == null && isReturnable(i.elseStatement))
                    returnType = elseStatementType;
                else if (isReturnable(i.elseStatement) && !isSameType(ifStatementType, elseStatementType))
                    error("ifStatement (%s) and elseStatement (%s) return different types", ifStatementType, elseStatementType);
            }
        }

        return returnType;
	}

	@Override
	public Type visitAssign(Assign a) {
        Expr lhsExpr = a.lhs;
        if (!(lhsExpr instanceof VarExpr || lhsExpr instanceof FieldAccessExpr || lhsExpr instanceof ArrayAccessExpr || lhsExpr instanceof ValueAtExpr)) {
            error("lhs cannot be %s", a.lhs);
        }

        Type lhsType = a.lhs.accept(this);
        Type rhsType = a.rhs.accept(this);

        if (lhsType == BaseType.VOID || lhsType instanceof ArrayType) {
            error("lhsType cannot be %s", lhsType);
        }

        if (!isSameType(lhsType, rhsType)) {
            error("lhsType (%s) and rhsType (%s) do not match", lhsType, rhsType);
        }

        return null;
	}

	@Override
	public Type visitReturn(Return r) {
        if (r.expression == null) {
            return BaseType.VOID;
        }
        return r.expression.accept(this);
	}

}
