package gen;

public class DataVisitor extends CodeGeneratorVisitor<Void> {
    private OutputWriter writer;
    private PrefixAdder strPrefix = new PrefixAdder("str");
    private PrefixAdder chrPrefix = new PrefixAdder("chr");
    private PrefixAdder globalPrefix = new PrefixAdder("g");

    public DataVisitor(OutputWriter writer) {
        this.writer = writer;
    }





}
