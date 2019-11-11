package ast;

import java.util.HashMap;

public class VarDecl implements ASTNode {
    public final Type type;
    public final String varName;
    private String globalName = null;
    private HashMap<String, String> globalStructName;
    private Integer genStackOffset = null;

    public VarDecl(Type type, String varName) {
	    this.type = type;
	    this.varName = varName;
    }

     public <T> T accept(ASTVisitor<T> v) {
	return v.visitVarDecl(this);
    }

    @Override
    public String toString() { return type.toString() + " " + varName; }

    public void setGlobalName(String name) {
        if (isGlobalName()) {
            throw new RuntimeException("Can't set name if it is already set to " + globalName);
        }
        globalName = name;
    }

    public String getGlobalName() {
        if (!isGlobalName()) {
            throw new NullPointerException("Can't get global label if not a global label (genLabel is null)");
        }
        return globalName;
    }

    public boolean isGlobalName() {
        return globalName != null;
    }

    public void setStructFieldLabel(String field, String globalName) {
        // ensure is global
        if (!isGlobalName()) {
            throw new RuntimeException("entire struct must have a label first");
        }

        // ensure vardecl is actually for a struct
        if (!(type instanceof StructType)) {
            throw new RuntimeException("this variable declaration must be declaring a struct type");
        }

        // ensure label is non-empty
        if (globalName.isEmpty()) {
            throw new RuntimeException("label can't be empty");
        }

        // initialise hashmap if this is our first time labelling the struct field
        if (globalStructName == null) {
            globalStructName = new HashMap<>();
            for (VarDecl vd : ((StructType) type).std.variables) {
                // initialise each field label to empty str
                globalStructName.put(vd.varName, "");
            }
        }

        // check if the field exists by seeing if the label map contains the key
        if (!globalStructName.containsKey(field)) {
            throw new RuntimeException("this struct does not have that field");
        }

        // we do not allow relabelling
        if (!globalStructName.get(field).isEmpty()) {
            throw new RuntimeException("this struct has that field already labelled");
        }

        globalStructName.put(field, globalName);
    }

    // returns label for that field, if the vardecl is a struct
    public String getStructFieldLabel(String field) {
        if (globalStructName == null) {
            throw new RuntimeException("this vardecl does not declare a struct variable");
        }

        if (!globalStructName.containsKey(field)) {
            throw new RuntimeException("this struct does not have that field " + field);
        }

        String globalName = globalStructName.get(field);

        if (globalName.isEmpty()) {
            throw new RuntimeException("this struct has the field, but it is not labelled. field is " + field);
        }

        return globalName;
    }

    public void setGenStackOffset(int genStackOffset) {
        if (isGlobalName()) {
            throw new RuntimeException("Can't set offset of global");
        }
        if (this.genStackOffset != null) {
            throw new RuntimeException("Can't set offset for a single variable declaration multiple times");
        }
        this.genStackOffset = genStackOffset;
    }

    public int getGenStackOffset() {
        if (genStackOffset == null) {
            throw new NullPointerException("can't get genStackOffset when not set");
        }
        return genStackOffset;
    }


}
