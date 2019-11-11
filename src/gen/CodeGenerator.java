package gen;

import ast.*;

import javax.rmi.PortableRemoteObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.EmptyStackException;
import java.util.Stack;

public class CodeGenerator {

    public CodeGenerator() {}

    public static int allignTo4Bytes(int size) { return (size + 3) / 4 * 4; }

    public void emitProgram(Program program, File outputFile) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(outputFile);

        Helper.writer = new OutputWriter(writer);
        Helper.registers = new Registers();

        Helper.dataVisitor = new DataVisitor();
        Helper.textVisitor = new TextVisitor();
        Helper.preDefinedVisitor = new PreDefinedVisitor();

        program.accept(Helper.dataVisitor);
        program.accept(Helper.textVisitor);

        writer.close();
    }
}
