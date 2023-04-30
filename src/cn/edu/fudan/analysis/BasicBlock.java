package cn.edu.fudan.analysis;

import java.util.ArrayList;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;

public class BasicBlock {

	public final Method method;

	private int startAddress;

	private ArrayList<Instruction> instructions = new ArrayList<Instruction>();

	private ArrayList<BasicBlock> successors = new ArrayList <BasicBlock>();
	
	public BasicBlock(Method method, int startAddress){
		this.method = method;
		this.startAddress = startAddress;
	}

	public ArrayList<BasicBlock> getSuccessors() {
		return (ArrayList<BasicBlock>) successors.clone();
	}
	
	public int getStartAddress() {
		return startAddress;
	}

	public ArrayList<Instruction> getInstructions() {
		return instructions;
	}

	public void addInstruction(Instruction i){
		this.instructions.add(i);
	}

	public void addSuccessor(BasicBlock block) {
		this.successors.add(block);
	}
}
