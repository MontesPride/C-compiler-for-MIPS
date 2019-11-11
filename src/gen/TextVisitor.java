package gen;

import ast.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static gen.CodeGenerator.allignTo4Bytes;

public class TextVisitor extends CodeGeneratorVisitor<Register> {
    private OutputWriter writer;

    private Labeller funcLabel = new Labeller("func");
    private Labeller binopLabel = new Labeller("binop");
    private Labeller ifLabel = new Labeller("if");
    private Labeller whileLabel = new Labeller("while");

    private int frameOffset = 0;
    private final static int prologueSize = 4 * Register.savedRegisters.size();

    private static Map<Op, BiFunction<Register,Register,Register>> comparisonFunctions = null;
    private static Map<Op, String> comparators = new HashMap<Op, String>() {{
        put(Op.LT, "slt");
        put(Op.GT, "sgt");
        put(Op.LE, "sle");
        put(Op.GE, "sge");
        put(Op.ADD, "add");
        put(Op.SUB, "sub");
        put(Op.EQ, "seq");
        put(Op.NE, "sne");
    }};

    public TextVisitor() {
        this.writer = Helper.writer;

        if (comparisonFunctions == null) {

            comparisonFunctions = new HashMap<>();
            comparisonFunctions.put(Op.MUL, TextVisitor::mul);
            comparisonFunctions.put(Op.MOD, TextVisitor::mod);
            comparisonFunctions.put(Op.DIV, TextVisitor::div);
        }
    }

    // START OF HELPER FUNCTIONS

    private void allocateStackSpace(List<VarDecl> varDecls, boolean updateStackPointer) {
        writer.comment("Allocate space on stack for varDecls %s (updateSP=%s) (frameOffset=%d)", Arrays.toString(varDecls.toArray()), updateStackPointer, frameOffset);
        int totalSize = 0;
        for (VarDecl vd : varDecls) {
            int size = allignTo4Bytes(vd.type.sizeOf());
            frameOffset -= size;
            totalSize += size;

            vd.setGenStackOffset(frameOffset);
        }

        if (updateStackPointer) {
            if (totalSize == 0) {
                writer.nop();
                return;
            }

            Register.sp.sub(totalSize);
        }
    }

    private void freeStackSpace(List<VarDecl> varDecls) {
        writer.comment("Free space on stack from varDecls");
        int totalSize = 0;
        for (VarDecl vd : varDecls) {
            int size = allignTo4Bytes(vd.type.sizeOf());
            frameOffset += size;
            totalSize += size;
        }

        if (totalSize == 0) {
            writer.nop();
            return;
        }

        Register.sp.add(totalSize);
    }

    // save registers
    private void saveRegisters() {
        writer.comment("save registers");
        try (OutputWriter scope = writer.scope()) {
            writer.comment("Adjust $sp for prologue");
            Register.sp.sub(prologueSize);

            int i = 0;
            for (Register r: Register.savedRegisters) {
                writer.sw(r, Register.sp, i);
                i += 4;
            }
        }
    }

    // restore registers
    private void restoreRegisters() {
        writer.comment("restore registers");
        try (OutputWriter scope = writer.scope()) {
            int i = 0;
            for (Register r : Register.savedRegisters) {
                writer.lw(r, Register.sp, i);
                i += 4;
            }
            Register.sp.add(prologueSize);
        }
    }

    // Establish which expression to addressOf
    public Register addressOf(Expr expr) {
        if (expr instanceof VarExpr) {
            return addressOf((VarExpr) expr);
        } else if (expr instanceof ArrayAccessExpr) {
            return addressOf((ArrayAccessExpr) expr);
        } else if (expr instanceof FieldAccessExpr) {
            return addressOf((FieldAccessExpr) expr);
        } else if (expr instanceof ValueAtExpr) {
            return addressOf((ValueAtExpr) expr);
        }
        throw new RuntimeException("Received expression that that cannot be addressed - addressOf(" + expr.toString() + ")");
    }

    // get addressOf(ArrayAccessExpr)
    private Register addressOf(ArrayAccessExpr aa) {
        Register pointer = aa.name.accept(this);

        writer.comment("%s = addressOf(%s)", pointer, aa);
        int size = aa.type.sizeOf();
        try (Register index = aa.index.accept(this)) {
            index.mul(size);
            pointer.add(index);
        }

        return pointer;
    }

    // get addressOf(VarExpr)
    private Register addressOf(VarExpr v) {
        VarDecl varDecl = v.vd;

        Register value = Helper.registers.get();
        writer.comment("%s = addressOf(%s)", value, v);

        if (varDecl.isGlobalName()) {
            // Load address into "value"
            String label = varDecl.getGlobalName();
            value.loadAddress(label);

            return value;
        }

        // Local variables have an offset defined from the current frame pointer.
        // Set a new register to the address of this item on stack. $fp + item.offset
        writer.add(value, Register.fp, varDecl.getGenStackOffset());

        return value;
    }

    // get addressOf(FieldAccessExpr)
    private Register addressOf(FieldAccessExpr fa) {
        assert fa.name.type instanceof StructType;

        // Only varExpr.fieldName can take advantage of directly addressing by label
        if (fa.name instanceof VarExpr) {
            if (((VarExpr) fa.name).vd.isGlobalName()) {
                String label = ((VarExpr) fa.name).vd.getStructFieldLabel(fa.field);

                Register address = Helper.registers.get();
                address.loadAddress(label);
                return address;
            }
        }

        // Get the address of the struct
        Register address = fa.name.accept(this);
        writer.comment("%s = addressOf(%s)", address, fa);

        // Offset the address by whichever amount
        StructType structType = (StructType) fa.name.type;
        int offset = 0;
        for (VarDecl vd : structType.std.variables) {
            if (vd.varName.equals(fa.field)) {
                address.add(offset);
                return address;
            }

            offset += allignTo4Bytes(vd.type.sizeOf());
        }

        throw new RuntimeException("could not find field in: " + fa.toString());
    }

    // get addressOf(ValueAtExpr)
    private Register addressOf(ValueAtExpr va) {
        assert va.expression.type instanceof PointerType;
        writer.comment(va);
        try (OutputWriter scope = writer.scope()) {
            // Store address of the pointer in a register
            Register locationOfPointer = addressOf(va.expression);

            // Read the pointer into our register
            writer.lw(locationOfPointer, locationOfPointer, 0);

            // This is what the above load word has done
            return locationOfPointer;
        }
    }

    // Get value of a certain type stored at this address
    public Register getValue(Register address, Type type) {
        Register value = Helper.registers.get();

        writer.comment("%s = valueAt(%s, %s)", value, address, type);

        if (type == BaseType.CHAR) {
            value.loadByte(address, 0);
        } else if (type == BaseType.INT || type instanceof PointerType) {
            value.loadWord(address, 0);
        } else if (type instanceof ArrayType || type instanceof StructType) {
            value.set(address);
        } else if (type == BaseType.VOID) {
            writer.nop();
        } else {
            throw new RuntimeException("getValue can't be used with type " + type.toString());
        }

        return value;
    }

    public void storeValue(Register sourceValue, Type type, Register targetAddress, int offset) {
        writer.comment("(%s + %d) = valueOf(%s, %s)", targetAddress, offset, sourceValue, type);
        if (type == BaseType.CHAR) {
            sourceValue.storeByteAt(targetAddress, offset);
        } else if (type == BaseType.INT || type instanceof PointerType) {
            sourceValue.storeWordAt(targetAddress, offset);
        } else if (type instanceof StructType) {

            // Note, sourceValue is actually referring to the struct's address
            // Don't forget this!

            try (OutputWriter scope = writer.scope()) {
                StructTypeDecl std = ((StructType) type).std;

                int totalSize = 0;
                for (VarDecl vd : std.variables) {
                    // Read the value at the struct address (which may or may not have been incremented)
                    try (Register innerSourceValue = getValue(sourceValue, vd.type)) {
                        storeValue(innerSourceValue, vd.type, targetAddress, offset);
                    }

                    // Increment our read offset and struct address by the size we've just read
                    int size = allignTo4Bytes(vd.type.sizeOf());
                    sourceValue.add(size);
                    offset += size;
                    totalSize += size;
                }

                // Restore sourceValue to original address
                sourceValue.sub(totalSize);
            }
        } else {
            throw new RuntimeException("storeValue hasn't been implemented for type: " + type.toString());
        }
    }

    private Register visitAddressableExpr(Expr expr) {
        try (Register address = addressOf(expr)) {
            return getValue(address, expr.type);
        }
    }

    private static Register mul(Register x, Register y) {
        Register result = Helper.registers.get();
        Helper.writer.mul(result, x, y);
        return result;
    }

    private static Register mod(Register num, Register dividedBy) {
        Register result = Helper.registers.get();
        Helper.writer.div(num, dividedBy);
        Helper.writer.mfhi(result);
        return result;
    }

    private static Register div(Register num, Register dividedBy) {
        Register result = Helper.registers.get();
        Helper.writer.div(num, dividedBy);
        Helper.writer.mflo(result);
        return result;
    }

    private Register and(Register x, Expr yExpr) {
        // Generate a result register
        Register result = Helper.registers.get();

        // Generate a "false", "true", "end" label ahead of time
        String falsePrefix = binopLabel.enumLabel("and_false");
        String truePrefix = binopLabel.enumLabel("and_true");
        String finishPrefix = binopLabel.enumLabel("and_finish");

        // Plan:
        // - jump to FALSE if X fails, otherwise continue (jump to CHECK_Y)
        // - CHECK_Y: jump to TRUE if Y success, otherwise continue (jump to FALSE)
        // - FALSE  : set result to 0, then finish (jump to FINISH)
        // - TRUE   : set result to 1
        // - FINISH : return the result

        // Jump to FALSE if X is zero
        Helper.writer.beqz(x, falsePrefix);

        // Jump to TRUE if Y success
        try (Register y = yExpr.accept(this)) {
            // If y is greater than zero, we want to skip to the true label
            Helper.writer.bgtz(y, truePrefix);
        }

        // FALSE: Set result to 0, jump to finish
        writer.withLabel(falsePrefix).li(result, 0);
        writer.b(finishPrefix);

        // TRUE : Set result to 1
        writer.withLabel(truePrefix).li(result, 1);

        // Emit finish label
        writer.withLabel(finishPrefix).nop();

        return result;
    }

    private Register or(Register x, Expr yExpr) {
        // Generate a result register
        Register result = Helper.registers.get();

        // Generate a "false", "true", "end" label ahead of time
        String falsePrefix = binopLabel.enumLabel("or_false");
        String truePrefix = binopLabel.enumLabel("or_true");
        String finishPrefix = binopLabel.enumLabel("or_finish");

        // Plan:
        // - jump to TRUE if X success, otherwise continue (jump to CHECK_Y)
        // - CHECK_Y: continue if Y success, otherwise jump to FALSE
        // - TRUE   : set result to 1 (jump to FINISH)
        // - FALSE  : set result to 0
        // - FINISH : return the result

        // Jump to TRUE if X success
        writer.bnez(x, truePrefix);

        // Jump to FALSE if Y fail
        try (Register y = yExpr.accept(this)) {
            // If y is greater than zero, we want to skip to the true label
            writer.beqz(y, falsePrefix);
        }

        // TRUE : Set result to 1, jump to finish
        writer.withLabel(truePrefix).li(result, 1);
        writer.b(finishPrefix);

        // FALSE: Set result to 0
        writer.withLabel(falsePrefix).li(result, 0);

        // Emit finish label
        writer.withLabel(finishPrefix).nop();

        return result;
    }

    private Register compare(Register lhs, Register rhs, String operator) {
        Register result = Helper.registers.get();
        writer.printf("%s %s, %s, %s", operator, result, lhs, rhs);
        return result;

    }

    // END OF HELPER FUNCTIONS

    // START OF VISIT FUNCTIONS

    @Override
    public Register visitProgram(Program p) {
        writer.newSection("text");

        try (OutputWriter scope = writer.scope()) {

            writer.withLabel("main").newSection("globl %s", "main");
            writer.jal("func_main_start");
            Register.paramRegs[0].set(Register.v0);
            Register.v0.loadImmediate(17);
            writer.syscall();

            super.visitProgram(p);

        }

        assert writer.getIndentLevel() == 0;
        return null;
    }

    @Override
    public Register visitBlock(Block b) {
        int oldOffset = frameOffset;
        allocateStackSpace(b.variables, true);

        for (Stmt stmt : b.statements)
            stmt.accept(this);

        freeStackSpace(b.variables);
        assert frameOffset == oldOffset;
        return null;
    }

    @Override
    public Register visitFunDecl(FunDecl f) {
        // Ignore inbuilt declarations
        if (f.isPreDefined) {
            return null;
        }

        f.globalName = funcLabel.addLabel(f.name + "_start");
        writer.withLabel(f.globalName).comment("%s", f);

        String epilogueLabel = funcLabel.addLabel(f.name + "_epilogue");

        frameOffset = 0; // reset frame offset to 0 because we only care about it per function
        // Allocate space for arguments on stack
        allocateStackSpace(f.params, false);

        try (OutputWriter scope = writer.scope()) {
            /*
            PROLOGUE:
            - initialise the frame pointer
            - save all the temporary registers onto the stack
             */
            int paramsSize = 0;

            writer.comment("prologue");
            try (OutputWriter innerScope = writer.scope()) {
                // Snapshot our caller's registers
                saveRegisters();

                // Our stack pointer is just to tell us where to allocate space next.
                // We already have arguments allocated.
                // Set frame pointer to the stack pointer so we know where the callee can look for our passed data
                writer.comment("reset frame pointer");
                Register.fp.set(Register.sp);

                // Jump over our parameters
                writer.comment("Skip over parameters: %s", Arrays.toString(f.params.toArray()));
                for (VarDecl vd : f.params) {
                    paramsSize += allignTo4Bytes(vd.type.sizeOf());
                }
                Register.sp.sub(paramsSize);

                // Set $ra to epilogue
                writer.comment("Set $ra to epilogue");
                Register.ra.loadAddress(epilogueLabel);
            }

            // do some stuff
            writer.comment("function contents");
            try (OutputWriter innerScope = writer.scope()) {
                visitBlock(f.block);

                writer.comment("Store default return value at $v0");
                Register.v0.loadImmediate(0);
            }

            writer.withLabel(epilogueLabel).comment("epilogue");
            try (OutputWriter innerScope = writer.scope()) {
                // Set stack pointer to our function's frame pointer
                Register.sp.set(Register.fp);

                // Restore registers to caller's state
                restoreRegisters();

                // Jump to $ra
                Register.ra.jump();
            }
        }
        return null; // no register returned for function declarations
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        assert v.type == v.vd.type;
        return visitAddressableExpr(v);
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr fc) {
        if (fc.fd.isPreDefined) {
            return fc.accept(Helper.preDefinedVisitor);
        }

        /*
        Plan for calling int foo(char bar):
        - WIND:
            - skip prologue
            - allocate and place arguments on our current function's stack frame (first argument is result address)
        - JUMP: jump to foo
        - UNWIND:
            -
        -
         */

        Register result = Helper.registers.get();

        writer.comment("precall");
        try (OutputWriter scope = writer.scope()) {
            // Store current return address
            writer.comment("Store current return address on stack");
            Register.sp.sub(4);
            Register.ra.storeWordAt(Register.sp, 0);

            // Skip the prologue size
            writer.comment("Skip the prologue size (we will be writing into our callee stack frame)");
            Register.sp.sub(prologueSize);

            // Iterate through args
            int totalArgSize = 0;
            for (int i = 0; i < fc.fd.params.size(); i++) {
                VarDecl decl = fc.fd.params.get(i);
                Expr expr = fc.params.get(i);
                Type type = expr.type;

                int argSize = allignTo4Bytes(type.sizeOf());
                totalArgSize += argSize;

                Register.sp.sub(argSize);

                int offset = decl.getGenStackOffset();
                writer.comment("Storing arg $d (%s) at $sp", i, expr, offset);
                try (OutputWriter argScope = writer.scope(); Register sourceValue = expr.accept(this)) {
                    Register targetAddress = Register.sp;
                    storeValue(sourceValue, type, targetAddress, 0);
                }
            }

            // Roll back the sp by PrologueSize + argSize
            writer.comment("Roll back the sp by PrologueSize + argSize");
            Register.sp.add(prologueSize + totalArgSize);
        }

        writer.comment("perform jump to declaration");
        writer.jal(fc.fd.globalName);

        writer.comment("postreturn");
        try (OutputWriter scope = writer.scope()) {
            writer.comment("Restore return address");
            Register.ra.loadWord(Register.sp, 0);
            Register.sp.add(4);

            writer.comment("Set return value");
            result.set(Register.v0);
            return result;
        }
    }

    @Override
    public Register visitIntLiteral(IntLiteral il) {
        Register val = Helper.registers.get();
        val.loadImmediate(il.value);
        return val;
    }


    @Override
    public Register visitChrLiteral(ChrLiteral cl) {
        Register val = Helper.registers.get();
        writer.comment("%s = %s", val, cl.toString());
        val.loadImmediate(cl.value);
        return val;
    }

    @Override
    public Register visitStrLiteral(StrLiteral sl) {
        Register address = Helper.registers.get();
        address.loadAddress(sl.globalName);
        return address;
    }

    @Override
    public Register visitBinOp(BinOp binOp) {
        writer.comment("%s", binOp);

        if (binOp.op == Op.AND) {
            writer.comment("%s", binOp);
            try (Register lhs = binOp.lhs.accept(this); OutputWriter scope = writer.scope()) {
                return and(lhs, binOp.rhs);
            }
        } else if (binOp.op == Op.OR) {
            writer.comment("%s", binOp);
            try (Register lhs = binOp.lhs.accept(this); OutputWriter scope = writer.scope()) {
                return or(lhs, binOp.rhs);
            }
        } else if (comparisonFunctions.containsKey(binOp.op)) {
            try (Register lhs = binOp.lhs.accept(this); Register rhs = binOp.rhs.accept(this)) {
                return comparisonFunctions.get(binOp.op).apply(lhs, rhs);
            }
        } else if (comparators.containsKey(binOp.op)) {
            try (Register lhs = binOp.lhs.accept(this); Register rhs = binOp.rhs.accept(this)) {
                return compare(lhs, rhs, comparators.get(binOp.op));
            }
        }

        throw new RuntimeException("unsupported operation");
    }

    @Override
    public Register visitArrayAccessExpr(ArrayAccessExpr aa) { return visitAddressableExpr(aa); }

    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr fa) {
        assert fa.type != null;
        return visitAddressableExpr(fa);
    }

    @Override
    public Register visitValueAtExpr(ValueAtExpr va) {
        assert va.expression.type instanceof PointerType;
        return visitAddressableExpr(va);
    }

    @Override
    public Register visitSizeOfExpr(SizeOfExpr so) {
        // visitSizeOfExpr is a value, so we don't need to get addr

        writer.comment(so);

        try (OutputWriter scope = writer.scope()) {
            Register val = Helper.registers.get();
            val.loadImmediate(so.sizeOfType.sizeOf());
            return val;
        }
    }

    @Override
    public Register visitTypecastExpr(TypecastExpr tc) {
        // visitSizeOfExpr is a value, so we don't need to get addr
        Register value;
        writer.comment(tc);
        try (OutputWriter scope = writer.scope()) {
            value = tc.expression.accept(this);
        }
        return value;
    }

    @Override
    public Register visitExprStmt(ExprStmt es) {
        writer.comment(es);
        try (OutputWriter scope = writer.scope()) {
            try (Register register = es.expression.accept(this)) {}
        }
        return null;
    }

    @Override
    public Register visitIf(If i) {
        String endLabel = ifLabel.enumLabel("end");
        String elseLabel = (i.elseStatement == null) ? endLabel : ifLabel.enumLabel("else");

        writer.comment("if (%s)", i.expression);
        try (OutputWriter scope = writer.scope(); Register shouldSkip = i.expression.accept(this)) {
            writer.beqz(shouldSkip, elseLabel);

            i.ifStatement.accept(this);

            writer.b(endLabel);
        }

        if (i.elseStatement == null) {
            writer.withLabel(endLabel).nop();
            return null;
        }

        writer.withLabel(elseLabel).comment("else");
        try (OutputWriter scope = writer.scope()) {
            i.elseStatement.accept(this);
        }

        writer.withLabel(endLabel).nop();

        return null;
    }

    @Override
    public Register visitWhile(While w) {
        String startLabel = whileLabel.enumLabel("begin");
        String endLabel = whileLabel.enumLabel("end");

        writer.withLabel(startLabel).comment("while (%s)", w.expression);
        try (OutputWriter scope = writer.scope(); Register shouldContinue = w.expression.accept(this)) {
            writer.beqz(shouldContinue, endLabel);

            w.statement.accept(this);

            writer.b(startLabel);
        }

        writer.withLabel(endLabel).nop();
        return null;
    }

    @Override
    public Register visitAssign(Assign a) {
        writer.comment(a);
        try (OutputWriter scope = writer.scope(); Register pointer = this.addressOf(a.lhs); Register value = a.rhs.accept(this)) {
            storeValue(value, a.rhs.type, pointer, 0);
        }
        return null;
    }

    @Override
    public Register visitReturn(Return r) {
        writer.comment(r);
        try (OutputWriter scope = writer.scope()) {
            if (r.expression != null) {
                try (Register value = r.expression.accept(this)) {
                    writer.comment("Store return value at $v0");
                    Register.v0.set(value);
                }
            } else {
                writer.comment("Store default return value at $v0");
                Register.v0.loadImmediate(0);
            }

            writer.comment("Jump to epilogue (defined at $ra)");
            writer.jr(Register.ra);
        }
        return null;
    }

}
