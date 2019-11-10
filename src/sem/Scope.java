package sem;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private Scope outer;
    private Map<String, Symbol> symbolTable;
    private Map<String, Symbol> structSymbolTable;

    public Scope(Scope outer) {
        this.outer = outer;
        this.symbolTable = new HashMap<>();
        this.structSymbolTable = new HashMap<>();
    }

    public Scope() {
        this.outer = null;
        this.symbolTable = new HashMap<>();
        this.structSymbolTable = new HashMap<>();
    }

    public Symbol lookup(String name, boolean isStructSymbol) {
        if (isStructSymbol) {
            if (structSymbolTable.get(name) != null)
                return structSymbolTable.get(name);
            if (outer != null)
                return outer.lookup(name, true);
        } else {
            if (symbolTable.get(name) != null)
                return symbolTable.get(name);
            if (outer != null)
                return outer.lookup(name, false);
        }
        return null;
    }

    public Symbol lookupCurrent(String name, boolean isStructSymbol) {
        if (isStructSymbol)
            return structSymbolTable.get(name);
        else
            return symbolTable.get(name);
    }

    public void put(Symbol sym, boolean isStructSymbol) {
        if (isStructSymbol)
            structSymbolTable.put(sym.name, sym);
        else
            symbolTable.put(sym.name, sym);
    }
}
