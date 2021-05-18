package nl.cochez.bgpExtractor;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpDiff;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpFind;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLabel;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpList;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpProcedure;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpPropFunc;
import org.apache.jena.sparql.algebra.op.OpQuad;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnion;

public abstract class AllBGPOpVisitor implements OpVisitor {

	@Override
	public abstract void visit(OpBGP opBGP);

	@Override
	public final void visit(OpProject opProject) {
		opProject.getSubOp().visit(this);
	}

	@Override
	public final void visit(OpFilter opFilter) {
		opFilter.getSubOp().visit(this);
	}

	@Override
	public final void visit(OpQuadPattern quadPattern) {
		throw new Error("not implemented yet");
	}

	@Override
	public final void visit(OpQuadBlock quadBlock) {
		throw new Error("not implemented yet");

	}

	@Override
	public final void visit(OpTriple opTriple) {
		throw new Error("I think the visitor should never come to this level as triples are part of a BGP...");

	}

	@Override
	public final void visit(OpQuad opQuad) {
		throw new Error("I think the visitor should never come to this level as triples are part of a BGP...");
	}

	@Override
	public final void visit(OpPath opPath) {
		// does not contain BGP => ignored
	}

	@Override
	public final void visit(OpFind opFind) {
		// does not contain BGP => ignored
	}

	
	// For example: SELECT  ?entity WHERE {VALUES ?entity { <http://dbpedia.org/resource/Angular> } }
	@Override
	public final void visit(OpTable opTable) {
		// does not contain BGP => ignored
	}

	@Override
	public final void visit(OpNull opNull) {
		// does not contain BGP => ignored
	}

	@Override
	public final void visit(OpProcedure opProc) {
		opProc.getSubOp().visit(this);
	}

	@Override
	public final void visit(OpPropFunc opPropFunc) {
		opPropFunc.getSubOp().visit(this);
	}

	@Override
	public final void visit(OpGraph opGraph) {
		opGraph.getSubOp().visit(this);
	}

	@Override
	public final void visit(OpService opService) {
		opService.getSubOp().visit(this);
	}

	@Override
	public final void visit(OpDatasetNames dsNames) {
		// does not contain BGP => ignored
	}

	@Override
	public final void visit(OpLabel opLabel) {
		opLabel.getSubOp().visit(this);
	}

	@Override
	public final void visit(OpAssign opAssign) {
		opAssign.getSubOp().visit(this);
	}

	@Override
	public final void visit(OpExtend opExtend) {
		opExtend.getSubOp().visit(this);
	}

	@Override
	public final void visit(OpJoin opJoin) {
		opJoin.getLeft().visit(this);
		opJoin.getRight().visit(this);
	}

	@Override
	public final void visit(OpLeftJoin opLeftJoin) {
		opLeftJoin.getLeft().visit(this);
		opLeftJoin.getRight().visit(this);
	}

	@Override
	public final void visit(OpUnion opUnion) {
		opUnion.getLeft().visit(this);
		opUnion.getRight().visit(this);
	}

	@Override
	public final void visit(OpDiff opDiff) {
		opDiff.getLeft().visit(this);
		opDiff.getRight().visit(this);
	}

	@Override
	public final void visit(OpMinus opMinus) {
		opMinus.getLeft().visit(this);
		opMinus.getRight().visit(this);
	}

	@Override
	public final void visit(OpConditional opCondition) {
		opCondition.getLeft().visit(this);
		opCondition.getRight().visit(this);
	}

	@Override
	public final void visit(OpSequence opSequence) {
		for (Op el : opSequence.getElements()) {
			el.visit(this);
		}

	}

	@Override
	public final void visit(OpDisjunction opDisjunction) {
		for (Op el : opDisjunction.getElements()) {
			el.visit(this);
		}
	}

	@Override
	public final void visit(OpList opList) {
		opList.getSubOp().visit(this);
	}

	@Override
	public final void visit(OpOrder opOrder) {
		opOrder.getSubOp().visit(this);
	}

	@Override
	public final void visit(OpReduced opReduced) {
		opReduced.getSubOp().visit(this);
	}

	@Override
	public final void visit(OpDistinct opDistinct) {
		opDistinct.getSubOp().visit(this);
	}

	@Override
	public final void visit(OpSlice opSlice) {
		opSlice.getSubOp().visit(this);
	}

	@Override
	public final void visit(OpGroup opGroup) {
		opGroup.getSubOp().visit(this);
	}

	@Override
	public final void visit(OpTopN opTop) {
		opTop.getSubOp().visit(this);
	}

}