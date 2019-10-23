package sem;

public abstract class Symbol {
	public String name;
	
	public Symbol(String name) {
		this.name = name;
	}

	public boolean isVar() {
		return this.getClass().equals(VarSymbol.class);
	}

	public boolean isFunc() {
		return this.getClass().equals(FuncSymbol.class);
	}

	public boolean isStruct() {
		return this.getClass().equals(StructTypeSymbol.class);
	}
}
