// Example of how to write an LLVM pass
// For more information see: http://llvm.org/docs/WritingAnLLVMPass.html

#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/Instructions.h"
#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Transforms/IPO/PassManagerBuilder.h"
#include "llvm/Transforms/Utils/Local.h"
#include <set>
#include <vector>

using namespace llvm;
using namespace std;

bool wasLivenessPrinted = false;

namespace {

bool isPHINode(Value* v) { return isa<PHINode>(v); }

struct MyPass : public FunctionPass {
  static char ID;
  MyPass() : FunctionPass(ID) {}

  //-----------------------------------------------
  /*
      bool runOnFunction(Function &F) override {
        printLiveness(F);
        return true;
      }

      void printLiveness(Function &F) {
        for (Function::iterator bb = F.begin(), end = F.end(); bb != end; bb++)
     {
          for (BasicBlock::iterator i = bb->begin(), e = bb->end(); i != e; i++)
     {
            // skip phis
            if (dyn_cast<PHINode>(i))
              continue;

            errs() << "{";

            // UNCOMMENT AND ADAPT FOR YOUR "IN" SET
            auto operatorSet = inSet[&*i];
            for (auto oper = operatorSet.begin(); oper != operatorSet.end();
     oper++) {
              auto op = *oper;
              if (oper != operatorSet.begin())
                errs() << ", ";
              (*oper)->printAsOperand(errs(), false);
            }
            //

            errs() << "}\n";
          }
        }
        errs() << "{}\n";
      }
  */
  //------------------------------------------------

  bool canBeRemoved(Instruction* instruction) {
    return !isa<CallInst>(instruction) && !instruction->mayHaveSideEffects() &&
           !instruction->isTerminator();
  }

  void printLiveness(Function& F, map<Value*, set<Value*>> inSet) {
    if (wasLivenessPrinted) return;
    wasLivenessPrinted = true;

    for (BasicBlock& bb : F) {
      for (auto i = bb.begin(); i != bb.end(); ++i) {
        Instruction* instruction = &*i;

        if (isa<PHINode>(instruction)) continue;

        errs() << "{";
        int counter = 0;
        int count = inSet[instruction].size();
        for (auto value : inSet[instruction]) {
          value->printAsOperand(errs(), false);
          counter++;
          if (counter < count) errs() << ",";
        }
        errs() << "}\n";
      }
    }
    errs() << "{}\n\n";
  }

  bool findAndRemoveDeadFunctions(Function& F) {
    SmallVector<Instruction*, 64> workList;

    map<Value *, set<Value *>> inSet, outSet, previousInSet, previousOutSet,
        difference;
        map<PHINode*, map<BasicBlock*, set<Value*>>> phiMap;

    for (BasicBlock& bb : F) {
      for (BasicBlock::iterator i = bb.begin(); i != bb.end(); ++i) {
        Instruction* instruction = &*i;
        inSet[instruction] = set<Value*>();
        outSet[instruction] = set<Value*>();
      }
    }

    do {
      for (Function::iterator bb = F.begin(); bb != F.end(); ++bb) {
        for (BasicBlock::iterator i = bb->begin(); i != bb->end(); ++i) {
          Instruction* instruction = &*i;
          previousInSet[instruction] = inSet[instruction];
          previousOutSet[instruction] = outSet[instruction];

          set<Value*> instructionUsers;
          if (isa<PHINode>(instruction)) {
            auto phiSuccessor = dyn_cast<PHINode>(instruction);
            for (auto j = 0; j < phiSuccessor->getNumIncomingValues(); j++) {
              auto incomingValue = phiSuccessor->getIncomingValue(j);
              if (isa<Instruction>(incomingValue) ||
                  isa<Argument>(incomingValue)) {
                auto phiIncomingBlock = phiSuccessor->getIncomingBlock(j);
                phiMap[phiSuccessor][phiIncomingBlock].insert(incomingValue);
              }
            }
          } else {
            for (auto instructionIterator = instruction->op_begin();
                 instructionIterator != instruction->op_end();
                 instructionIterator++) {
              if (isa<Instruction>(&*instructionIterator) ||
                  isa<Argument>(&*instructionIterator)) {
                auto instructionValue = dyn_cast<Value>(&*instructionIterator);
                instructionUsers.insert(instructionValue);
              }
            }
          }

          copy(outSet[instruction].begin(), outSet[instruction].end(),
               inserter(difference[instruction], difference[instruction].begin()));
          difference[instruction].erase(instruction);

          set<Value*> inDest;
          set_union(instructionUsers.begin(), instructionUsers.end(), difference[instruction].begin(),
                    difference[instruction].end(), inserter(inDest, inDest.begin()));

          inSet[instruction] = inDest;

          set<Instruction*> successors;
          set<PHINode*> phiSuccessors;
          if (instruction->isTerminator()) {
            for (size_t j = 0; j < instruction->getNumSuccessors(); j++) {
              BasicBlock* bbSuccessor = instruction->getSuccessor(j);
              auto it = bbSuccessor->begin();
              auto instructionSuccessor = &*it;
              successors.insert(instructionSuccessor);

              if (isa<PHINode>(instructionSuccessor)) {
                for (it++; it != bbSuccessor->end(); it++) {
                  if (!isa<PHINode>(&*it)) break;
                  phiSuccessors.insert(dyn_cast<PHINode>(&*it));
                }
              }
            }
          } else {
            auto peekNextInstruction = i;
            ++peekNextInstruction;
            successors.insert(&*peekNextInstruction);
          }

          set<Value*> outDest;
          for (Instruction* successor : successors) {
            set<Value*> newOutDest;
            set<Value*> swapped;
            if (isPHINode(successor)) {
              auto phiSuccessor = dyn_cast<PHINode>(successor);
              if (instruction->isTerminator()) {
                auto phiSuccessorSet = phiMap[phiSuccessor][&*bb];

                set_union(phiSuccessorSet.begin(), phiSuccessorSet.end(), difference[phiSuccessor].begin(),
                          difference[phiSuccessor].end(),
                          inserter(swapped, swapped.begin()));
              } else {
                swapped = difference[phiSuccessor];
              }
            } else {
              swapped = inSet[successor];
            }

            set_union(swapped.begin(), swapped.end(), outDest.begin(),
                      outDest.end(), inserter(newOutDest, newOutDest.begin()));
            outDest = newOutDest;
          }

          if (!phiSuccessors.empty()) {
            set<Value*> phiUnion;
            for (auto value : phiSuccessors) {
              auto phiSuccessorSet = phiMap[value][&*bb];
              set<Value*> newPhiUnion;
              set_union(phiSuccessorSet.begin(), phiSuccessorSet.end(), phiUnion.begin(), phiUnion.end(),
                        inserter(newPhiUnion, newPhiUnion.begin()));
              phiUnion = newPhiUnion;
            }
            set<Value*> newNewOutDest;
            set_union(phiUnion.begin(), phiUnion.end(), outDest.begin(),
                      outDest.end(),
                      inserter(newNewOutDest, newNewOutDest.begin()));
            outDest = newNewOutDest;
          }

          outSet[instruction] = outDest;
        }
      }
    } while (!(inSet == previousInSet && outSet == previousOutSet));

    printLiveness(F, inSet);

    for (Function::iterator bb = F.begin(); bb != F.end(); ++bb) {
      for (BasicBlock::iterator i = bb->begin(); i != bb->end(); ++i) {
        Instruction* instruction = &*i;
        bool isInstructionDead = (outSet[instruction].find(instruction) ==
                       outSet[instruction].end()) &&
                      canBeRemoved(instruction);
        if (isInstructionDead) workList.push_back(instruction);
      }
    }

    // Remove dead instruction
    for (Instruction* instruction : workList) {
      instruction->eraseFromParent();
    }

    return !workList.empty();
  }

  bool runOnFunction(Function& F) {
    int pass = 1;

    wasLivenessPrinted = false;
    bool changed = false;
    bool result = false;

    do {
      changed = findAndRemoveDeadFunctions(F);
      result |= changed;
      pass++;
    } while (changed);

    return result;
  }
};
}

char MyPass::ID = 0;
static RegisterPass<MyPass> X(
    "mypass", "My liveness analysis and dead code elimination pass");