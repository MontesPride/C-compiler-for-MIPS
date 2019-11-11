package gen;

public class TextVisitor extends CodeGeneratorVisitor<Register> {
    OutputWriter writer;

    public TextVisitor(OutputWriter writer) { this.writer = writer; }
}
