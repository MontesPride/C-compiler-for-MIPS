// Example of how to write an LLVM pass
// For more information see: http://llvm.org/docs/WritingAnLLVMPass.html

#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Transforms/IPO/PassManagerBuilder.h"
#include "llvm/Transforms/Utils/Local.h"
#include <vector>

using namespace llvm;
using namespace std;

namespace {
struct MyPass : public FunctionPass {
  static char ID;
  MyPass() : FunctionPass(ID) {}

  //-----------------------------------------------
  /*
      bool runOnFunction(Function &F) override {
      errs() << "I saw a function called " << F.getName() << "!\n";
      return true;
      }
      */
  //------------------------------------------------

  // findAndRemoveDeadFunctions returns true if at least one instruction was found trivially dead
  bool findAndRemoveDeadFunctions(Function& F) {
    SmallVector<Instruction*, 64> workList;

    // Find trivially dead instructions
    for (Function::iterator bb = F.begin(); bb != F.end(); bb++) {
      for (BasicBlock::iterator i = bb->begin(); i != bb->end(); i++) {
        Instruction* instruction = &*i;

        if (isInstructionTriviallyDead(instruction)) {
          errs() << "instruction dead: ";
          instruction->printAsOperand(errs());
          errs() << "\n";
          workList.push_back(instruction);
        } else {
          errs() << "instruction alive: ";
          instruction->printAsOperand(errs());
          errs() << "\n";
        }
      }
    }

    // Remove dead instruction
    for (Instruction* instruction : workList) {
      errs() << "instruction removed\n";
      instruction->eraseFromParent();
    }

    return !workList.empty();
  }

  bool runOnFunction(Function& F) override {
    int pass = 1;
    bool result = false;
    bool changed = false;

    do {
      errs() << "\nI saw a function called: " << F.getName() << " (pass " << pass << ")\n";
      pass++;
      changed = findAndRemoveDeadFunctions(F);
      result |= changed;
    } while (changed);

    errs() << "\n--------\n";

    return result;
  }
};
}

char MyPass::ID = 0;
static RegisterPass<MyPass> X("mypass", "My simple dead code elimination pass");
