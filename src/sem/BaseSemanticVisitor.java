package sem;

import ast.*;

/**
 * 
 * @author sfilipiak
 * A base class providing basic error accumulation.
 */
public abstract class BaseSemanticVisitor<T> implements SemanticVisitor<T> {
	private int errors;
	
	
	public BaseSemanticVisitor() {
		errors = 0;
	}
	
	public int getErrorCount() {
		return errors;
	}
	
	protected void error(String message) {
		System.err.println("semantic error: " + message);
		errors++;
	}

	protected void error(String formatString, Object... params) {
		System.err.printf(formatString + "\n", params);
		errors++;
	}

	public boolean isSameType(Type lhs, Type rhs) {
		if (lhs instanceof BaseType && rhs instanceof BaseType)
			return lhs == rhs;

		if (lhs instanceof StructType && rhs instanceof StructType)
			return ((StructType) lhs).name.equals(((StructType) rhs).name);

		if (lhs instanceof PointerType && rhs instanceof PointerType)
			return isSameType(((PointerType) lhs).type, ((PointerType) rhs).type);

		if (lhs instanceof ArrayType && rhs instanceof ArrayType)
			return (((ArrayType) lhs).numOfElems == ((ArrayType) rhs).numOfElems) && isSameType(((ArrayType) lhs).type, ((ArrayType) rhs).type);

		return lhs == rhs;
	}
}
