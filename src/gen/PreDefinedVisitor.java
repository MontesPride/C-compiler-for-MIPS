package gen;

import ast.*;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

public class PreDefinedVisitor extends CodeGeneratorVisitor<Register> {
    private static OutputWriter writer;

    private static HashMap<String, BiFunction<FunDecl, List<Expr>, Register>> predefinedFunctions = null;

    public PreDefinedVisitor() {
        this.writer = Helper.writer;

        if (predefinedFunctions == null) {
            // Initialise predefined functions
            PreDefinedVisitor.predefinedFunctions = new HashMap<>();
            PreDefinedVisitor.predefinedFunctions.put("print_s", PreDefinedVisitor::print_s);
            PreDefinedVisitor.predefinedFunctions.put("print_i", PreDefinedVisitor::print_i);
            PreDefinedVisitor.predefinedFunctions.put("print_c", PreDefinedVisitor::print_c);
            PreDefinedVisitor.predefinedFunctions.put("read_c", PreDefinedVisitor::read_c);
            PreDefinedVisitor.predefinedFunctions.put("read_i", PreDefinedVisitor::read_i);
            PreDefinedVisitor.predefinedFunctions.put("mcmalloc", PreDefinedVisitor::mcmalloc);
        }
    }

    private static Register get_register(FunDecl fd, List<Expr> args) {
        int reg = ((IntLiteral) args.get(0)).value;
        Register val = Helper.registers.get();
        writer.comment("get_register %d", reg);
        writer.printf("move %s, $%d", val, reg);
        return val;
    }

    private static Register print_i(FunDecl fd, List<Expr> args) {
        Expr arg = args.get(0);

        writer.comment("$a0 = %s", arg);
        try (OutputWriter scope = writer.scope()) {
            if (arg instanceof IntLiteral) {
                // this is left here for efficiency. removing this will just make an extra intermediary register.
                Helper.writer.li(Register.paramRegs[0], ((IntLiteral) arg).value);
            } else {
                try (Register val = arg.accept(Helper.textVisitor)) {
                    writer.comment("$a0 = %s", val);
                    Register.paramRegs[0].set(val);
                }
            }
        }

        writer.comment("print_i($a0)");
        Register.v0.loadImmediate(1);
        Helper.writer.syscall();

        return null;
    }

    private static Register print_s(FunDecl fd, List<Expr> args) {
        Expr arg = args.get(0);

        writer.comment("$a0 = %s", arg);
        try (OutputWriter scope = writer.scope(); Register val = arg.accept(Helper.textVisitor)) {
            writer.comment("$a0 = %s", val);
            Register.paramRegs[0].set(val);
        }

        writer.comment("print_s($a0)");
        Register.v0.loadImmediate(4);
        writer.syscall();
        return null;
    }

    private static Register read_i(FunDecl fd, List<Expr> args) {
        writer.comment("$v0 = read_i()");
        // Call syscall 5 - this sets the read integer to v0
        Register.v0.loadImmediate(5);
        writer.syscall();

        Register value = Helper.registers.get();
        Register.v0.moveTo(value);
        return value;
    }

    private static Register mcmalloc(FunDecl fd, List<Expr> args) {
        // Get the bytes required to allocate
        Expr arg = args.get(0);
        writer.comment("$a0 = %s", arg);
        try (OutputWriter scope = writer.scope(); Register byteCount = arg.accept(Helper.textVisitor)) {
            // Set the argument of the syscall to these bytes
            Register.paramRegs[0].set(byteCount);
        }

        // Call syscall 9 - this puts the address in v0
        writer.comment("mcmalloc($a0)");
        Register.v0.loadImmediate(9);
        writer.syscall();

        Register value = Helper.registers.get();
        Register.v0.moveTo(value);
        return value;
    }

    private static Register print_c(FunDecl fd, List<Expr> args) {
        Expr arg = args.get(0);

        writer.comment("$a0 = %s", arg);
        try (OutputWriter scope = writer.scope()) {
            if (arg instanceof ChrLiteral) {
                // this is left here for efficiency. removing this will just make an extra intermediary register.
                Helper.writer.li(Register.paramRegs[0], ((ChrLiteral) arg).value);
            } else {
                try (Register val = arg.accept(Helper.textVisitor)) {
                    writer.comment("$a0 = %s", val);
                    Register.paramRegs[0].set(val);
                }
            }
        }

        writer.comment("print_c($a0)");
        Register.v0.loadImmediate(11);
        Helper.writer.syscall();

        return null;
    }

    private static Register read_c(FunDecl fd, List<Expr> args) {
        // Call syscall 12 - this sets the read character to v0
        writer.comment("$v0 = read_c()");
        Register.v0.loadImmediate(12);
        writer.syscall();

        Register value = Helper.registers.get();
        writer.comment("%s = $v0", value);
        Register.v0.moveTo(value);
        return value;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr fc) {
        if (!fc.fd.isPreDefined) {
            return null;
        }

        writer.comment(fc);

        try (OutputWriter scope = writer.scope()) {
            if (!predefinedFunctions.containsKey(fc.fd.name)) {
                throw new RuntimeException("attempt to call undefined predefined function " + fc.fd.name);
            }

            return predefinedFunctions.get(fc.fd.name).apply(fc.fd, fc.params);
        }
    }

}
