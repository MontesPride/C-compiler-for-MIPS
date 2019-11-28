package gen;

import ast.*;

import java.util.List;

import static gen.CodeGenerator.allignTo4Bytes;

public class DataVisitor extends CodeGeneratorVisitor<Void> {
    private OutputWriter writer;

    private Labeller strLabel = new Labeller("str");
    private Labeller globalLabel = new Labeller("g");

    public DataVisitor() {
        this.writer = Helper.writer;
    }

    @Override
    public Void visitProgram(Program p) {
        writer.newSection("data");

        try (OutputWriter scope = writer.scope()) {
            for (StructTypeDecl std : p.structTypeDecls)
                std.accept(this);
            for (VarDecl vd : p.varDecls) {
                visitGlobalVarDecl(vd);
            }
            for (FunDecl fd : p.funDecls)
                fd.accept(this);
        }

        assert writer.getIndentLevel() == 0;
        return null;
    }

    @Override
    public Void visitStrLiteral(StrLiteral sl) {
        super.visitStrLiteral(sl);

        sl.globalName = strLabel.enumLabel();
        writer.withLabel(sl.globalName).dataAsciiNullTerminated(StrLiteral.escapedString(sl.value));
        return null;
    }

    public void visitGlobalStructDecl(VarDecl varDecl, StructType structType) {
        // Comment the list of declarations
        writer.comment("'%s' (struct %s) [size %d]", varDecl.varName, structType.name, structType.sizeOf());

        // Label the entire struct
        String name = globalLabel.addLabel(varDecl.varName);
        varDecl.setGlobalName(name);
        writer.withLabel(name).printf("");

        // Prep varName to be a suffix
        Labeller labeller = new Labeller("s_" + varDecl.varName);

        try (OutputWriter scope = writer.scope()) {
            for (VarDecl vd : structType.std.variables) {
                name = labeller.addLabel(vd.varName);
                // vd.setGlobalLabel(label); // we can't use this. each `vd` is global to all declarations of this struct
                varDecl.setStructFieldLabel(vd.varName, name);

                writer.withLabel(name).dataNeedSize(allignTo4Bytes(vd.type.sizeOf()));
            }
        }

        writer.newline();
    }

    public void visitGlobalVarDecl(VarDecl varDecl) {
        //super.visitVarDecl(varDecl);

        if (varDecl.type instanceof StructType) {
            visitGlobalStructDecl(varDecl, (StructType)varDecl.type);
            return;
        }

        String globalLabel = this.globalLabel.addLabel(varDecl.varName);
        varDecl.setGlobalName(globalLabel);

        int size = varDecl.type.sizeOf();
        writer.withLabel(globalLabel).dataNeedSize(allignTo4Bytes(size));
    }

}
