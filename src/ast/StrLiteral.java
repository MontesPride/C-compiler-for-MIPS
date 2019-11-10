package ast;

public class StrLiteral extends Expr {
    public final String value;

    public StrLiteral(String value) {
        this.value = value;
    }

    public <T> T accept(ASTVisitor<T> v) { return v.visitStrLiteral(this); }

    @Override
    public String toString() { return "\"" + escapedString(value) + "\""; }

    public static String escapedString(String string) {
        return string
                .replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\f", "\\f")
                .replace("\"", "\\\"")
                .replace("\0", "\\0");
    }
}
