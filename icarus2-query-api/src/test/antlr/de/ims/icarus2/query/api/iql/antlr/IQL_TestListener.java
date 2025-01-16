// Generated from de/ims/icarus2/query/api/iql/antlr/IQL_Test.g4 by ANTLR 4.13.2

package de.ims.icarus2.query.api.iql.antlr;
import org.antlr.v4.runtime.misc.Interval;	

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link IQL_TestParser}.
 */
public interface IQL_TestListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#versionDeclarationTest}.
	 * @param ctx the parse tree
	 */
	void enterVersionDeclarationTest(IQL_TestParser.VersionDeclarationTestContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#versionDeclarationTest}.
	 * @param ctx the parse tree
	 */
	void exitVersionDeclarationTest(IQL_TestParser.VersionDeclarationTestContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#quantifierTest}.
	 * @param ctx the parse tree
	 */
	void enterQuantifierTest(IQL_TestParser.QuantifierTestContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#quantifierTest}.
	 * @param ctx the parse tree
	 */
	void exitQuantifierTest(IQL_TestParser.QuantifierTestContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#unsignedSimpleQuantifierTest}.
	 * @param ctx the parse tree
	 */
	void enterUnsignedSimpleQuantifierTest(IQL_TestParser.UnsignedSimpleQuantifierTestContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#unsignedSimpleQuantifierTest}.
	 * @param ctx the parse tree
	 */
	void exitUnsignedSimpleQuantifierTest(IQL_TestParser.UnsignedSimpleQuantifierTestContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#integerLiteralTest}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteralTest(IQL_TestParser.IntegerLiteralTestContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#integerLiteralTest}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteralTest(IQL_TestParser.IntegerLiteralTestContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#unsignedIntegerLiteralTest}.
	 * @param ctx the parse tree
	 */
	void enterUnsignedIntegerLiteralTest(IQL_TestParser.UnsignedIntegerLiteralTestContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#unsignedIntegerLiteralTest}.
	 * @param ctx the parse tree
	 */
	void exitUnsignedIntegerLiteralTest(IQL_TestParser.UnsignedIntegerLiteralTestContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#floatingPointLiteralTest}.
	 * @param ctx the parse tree
	 */
	void enterFloatingPointLiteralTest(IQL_TestParser.FloatingPointLiteralTestContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#floatingPointLiteralTest}.
	 * @param ctx the parse tree
	 */
	void exitFloatingPointLiteralTest(IQL_TestParser.FloatingPointLiteralTestContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#unsignedFloatingPointLiteralTest}.
	 * @param ctx the parse tree
	 */
	void enterUnsignedFloatingPointLiteralTest(IQL_TestParser.UnsignedFloatingPointLiteralTestContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#unsignedFloatingPointLiteralTest}.
	 * @param ctx the parse tree
	 */
	void exitUnsignedFloatingPointLiteralTest(IQL_TestParser.UnsignedFloatingPointLiteralTestContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#standaloneNodeStatement}.
	 * @param ctx the parse tree
	 */
	void enterStandaloneNodeStatement(IQL_TestParser.StandaloneNodeStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#standaloneNodeStatement}.
	 * @param ctx the parse tree
	 */
	void exitStandaloneNodeStatement(IQL_TestParser.StandaloneNodeStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#standaloneStructuralConstraint}.
	 * @param ctx the parse tree
	 */
	void enterStandaloneStructuralConstraint(IQL_TestParser.StandaloneStructuralConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#standaloneStructuralConstraint}.
	 * @param ctx the parse tree
	 */
	void exitStandaloneStructuralConstraint(IQL_TestParser.StandaloneStructuralConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#standaloneSelectiveStatement}.
	 * @param ctx the parse tree
	 */
	void enterStandaloneSelectiveStatement(IQL_TestParser.StandaloneSelectiveStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#standaloneSelectiveStatement}.
	 * @param ctx the parse tree
	 */
	void exitStandaloneSelectiveStatement(IQL_TestParser.StandaloneSelectiveStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#standaloneExpression}.
	 * @param ctx the parse tree
	 */
	void enterStandaloneExpression(IQL_TestParser.StandaloneExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#standaloneExpression}.
	 * @param ctx the parse tree
	 */
	void exitStandaloneExpression(IQL_TestParser.StandaloneExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#payloadStatement}.
	 * @param ctx the parse tree
	 */
	void enterPayloadStatement(IQL_TestParser.PayloadStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#payloadStatement}.
	 * @param ctx the parse tree
	 */
	void exitPayloadStatement(IQL_TestParser.PayloadStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#bindingsList}.
	 * @param ctx the parse tree
	 */
	void enterBindingsList(IQL_TestParser.BindingsListContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#bindingsList}.
	 * @param ctx the parse tree
	 */
	void exitBindingsList(IQL_TestParser.BindingsListContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#binding}.
	 * @param ctx the parse tree
	 */
	void enterBinding(IQL_TestParser.BindingContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#binding}.
	 * @param ctx the parse tree
	 */
	void exitBinding(IQL_TestParser.BindingContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#selectionStatement}.
	 * @param ctx the parse tree
	 */
	void enterSelectionStatement(IQL_TestParser.SelectionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#selectionStatement}.
	 * @param ctx the parse tree
	 */
	void exitSelectionStatement(IQL_TestParser.SelectionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#laneStatementsList}.
	 * @param ctx the parse tree
	 */
	void enterLaneStatementsList(IQL_TestParser.LaneStatementsListContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#laneStatementsList}.
	 * @param ctx the parse tree
	 */
	void exitLaneStatementsList(IQL_TestParser.LaneStatementsListContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#laneStatement}.
	 * @param ctx the parse tree
	 */
	void enterLaneStatement(IQL_TestParser.LaneStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#laneStatement}.
	 * @param ctx the parse tree
	 */
	void exitLaneStatement(IQL_TestParser.LaneStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#structuralConstraint}.
	 * @param ctx the parse tree
	 */
	void enterStructuralConstraint(IQL_TestParser.StructuralConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#structuralConstraint}.
	 * @param ctx the parse tree
	 */
	void exitStructuralConstraint(IQL_TestParser.StructuralConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#hitsLimit}.
	 * @param ctx the parse tree
	 */
	void enterHitsLimit(IQL_TestParser.HitsLimitContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#hitsLimit}.
	 * @param ctx the parse tree
	 */
	void exitHitsLimit(IQL_TestParser.HitsLimitContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#matchFlag}.
	 * @param ctx the parse tree
	 */
	void enterMatchFlag(IQL_TestParser.MatchFlagContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#matchFlag}.
	 * @param ctx the parse tree
	 */
	void exitMatchFlag(IQL_TestParser.MatchFlagContext ctx);
	/**
	 * Enter a parse tree produced by the {@code elementSequence}
	 * labeled alternative in {@link IQL_TestParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void enterElementSequence(IQL_TestParser.ElementSequenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code elementSequence}
	 * labeled alternative in {@link IQL_TestParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void exitElementSequence(IQL_TestParser.ElementSequenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code elementGrouping}
	 * labeled alternative in {@link IQL_TestParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void enterElementGrouping(IQL_TestParser.ElementGroupingContext ctx);
	/**
	 * Exit a parse tree produced by the {@code elementGrouping}
	 * labeled alternative in {@link IQL_TestParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void exitElementGrouping(IQL_TestParser.ElementGroupingContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleNode}
	 * labeled alternative in {@link IQL_TestParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void enterSingleNode(IQL_TestParser.SingleNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleNode}
	 * labeled alternative in {@link IQL_TestParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void exitSingleNode(IQL_TestParser.SingleNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code graphFragment}
	 * labeled alternative in {@link IQL_TestParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void enterGraphFragment(IQL_TestParser.GraphFragmentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code graphFragment}
	 * labeled alternative in {@link IQL_TestParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void exitGraphFragment(IQL_TestParser.GraphFragmentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code elementDisjunction}
	 * labeled alternative in {@link IQL_TestParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void enterElementDisjunction(IQL_TestParser.ElementDisjunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code elementDisjunction}
	 * labeled alternative in {@link IQL_TestParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void exitElementDisjunction(IQL_TestParser.ElementDisjunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#nodeArrangement}.
	 * @param ctx the parse tree
	 */
	void enterNodeArrangement(IQL_TestParser.NodeArrangementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#nodeArrangement}.
	 * @param ctx the parse tree
	 */
	void exitNodeArrangement(IQL_TestParser.NodeArrangementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dummyNode}
	 * labeled alternative in {@link IQL_TestParser#node}.
	 * @param ctx the parse tree
	 */
	void enterDummyNode(IQL_TestParser.DummyNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dummyNode}
	 * labeled alternative in {@link IQL_TestParser#node}.
	 * @param ctx the parse tree
	 */
	void exitDummyNode(IQL_TestParser.DummyNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code properNode}
	 * labeled alternative in {@link IQL_TestParser#node}.
	 * @param ctx the parse tree
	 */
	void enterProperNode(IQL_TestParser.ProperNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code properNode}
	 * labeled alternative in {@link IQL_TestParser#node}.
	 * @param ctx the parse tree
	 */
	void exitProperNode(IQL_TestParser.ProperNodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#memberLabel}.
	 * @param ctx the parse tree
	 */
	void enterMemberLabel(IQL_TestParser.MemberLabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#memberLabel}.
	 * @param ctx the parse tree
	 */
	void exitMemberLabel(IQL_TestParser.MemberLabelContext ctx);
	/**
	 * Enter a parse tree produced by the {@code markerConjunction}
	 * labeled alternative in {@link IQL_TestParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void enterMarkerConjunction(IQL_TestParser.MarkerConjunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code markerConjunction}
	 * labeled alternative in {@link IQL_TestParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void exitMarkerConjunction(IQL_TestParser.MarkerConjunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code markerDisjunction}
	 * labeled alternative in {@link IQL_TestParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void enterMarkerDisjunction(IQL_TestParser.MarkerDisjunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code markerDisjunction}
	 * labeled alternative in {@link IQL_TestParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void exitMarkerDisjunction(IQL_TestParser.MarkerDisjunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code markerCall}
	 * labeled alternative in {@link IQL_TestParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void enterMarkerCall(IQL_TestParser.MarkerCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code markerCall}
	 * labeled alternative in {@link IQL_TestParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void exitMarkerCall(IQL_TestParser.MarkerCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code markerWrapping}
	 * labeled alternative in {@link IQL_TestParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void enterMarkerWrapping(IQL_TestParser.MarkerWrappingContext ctx);
	/**
	 * Exit a parse tree produced by the {@code markerWrapping}
	 * labeled alternative in {@link IQL_TestParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void exitMarkerWrapping(IQL_TestParser.MarkerWrappingContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#positionArgument}.
	 * @param ctx the parse tree
	 */
	void enterPositionArgument(IQL_TestParser.PositionArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#positionArgument}.
	 * @param ctx the parse tree
	 */
	void exitPositionArgument(IQL_TestParser.PositionArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#element}.
	 * @param ctx the parse tree
	 */
	void enterElement(IQL_TestParser.ElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#element}.
	 * @param ctx the parse tree
	 */
	void exitElement(IQL_TestParser.ElementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code emptyEdge}
	 * labeled alternative in {@link IQL_TestParser#edge}.
	 * @param ctx the parse tree
	 */
	void enterEmptyEdge(IQL_TestParser.EmptyEdgeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code emptyEdge}
	 * labeled alternative in {@link IQL_TestParser#edge}.
	 * @param ctx the parse tree
	 */
	void exitEmptyEdge(IQL_TestParser.EmptyEdgeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code filledEdge}
	 * labeled alternative in {@link IQL_TestParser#edge}.
	 * @param ctx the parse tree
	 */
	void enterFilledEdge(IQL_TestParser.FilledEdgeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code filledEdge}
	 * labeled alternative in {@link IQL_TestParser#edge}.
	 * @param ctx the parse tree
	 */
	void exitFilledEdge(IQL_TestParser.FilledEdgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#leftEdgePart}.
	 * @param ctx the parse tree
	 */
	void enterLeftEdgePart(IQL_TestParser.LeftEdgePartContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#leftEdgePart}.
	 * @param ctx the parse tree
	 */
	void exitLeftEdgePart(IQL_TestParser.LeftEdgePartContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#rightEdgePart}.
	 * @param ctx the parse tree
	 */
	void enterRightEdgePart(IQL_TestParser.RightEdgePartContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#rightEdgePart}.
	 * @param ctx the parse tree
	 */
	void exitRightEdgePart(IQL_TestParser.RightEdgePartContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#directedEdgeLeft}.
	 * @param ctx the parse tree
	 */
	void enterDirectedEdgeLeft(IQL_TestParser.DirectedEdgeLeftContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#directedEdgeLeft}.
	 * @param ctx the parse tree
	 */
	void exitDirectedEdgeLeft(IQL_TestParser.DirectedEdgeLeftContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#directedEdgeRight}.
	 * @param ctx the parse tree
	 */
	void enterDirectedEdgeRight(IQL_TestParser.DirectedEdgeRightContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#directedEdgeRight}.
	 * @param ctx the parse tree
	 */
	void exitDirectedEdgeRight(IQL_TestParser.DirectedEdgeRightContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#undirectedEdge}.
	 * @param ctx the parse tree
	 */
	void enterUndirectedEdge(IQL_TestParser.UndirectedEdgeContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#undirectedEdge}.
	 * @param ctx the parse tree
	 */
	void exitUndirectedEdge(IQL_TestParser.UndirectedEdgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#groupStatement}.
	 * @param ctx the parse tree
	 */
	void enterGroupStatement(IQL_TestParser.GroupStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#groupStatement}.
	 * @param ctx the parse tree
	 */
	void exitGroupStatement(IQL_TestParser.GroupStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#groupExpression}.
	 * @param ctx the parse tree
	 */
	void enterGroupExpression(IQL_TestParser.GroupExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#groupExpression}.
	 * @param ctx the parse tree
	 */
	void exitGroupExpression(IQL_TestParser.GroupExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#resultStatement}.
	 * @param ctx the parse tree
	 */
	void enterResultStatement(IQL_TestParser.ResultStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#resultStatement}.
	 * @param ctx the parse tree
	 */
	void exitResultStatement(IQL_TestParser.ResultStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#orderExpressionList}.
	 * @param ctx the parse tree
	 */
	void enterOrderExpressionList(IQL_TestParser.OrderExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#orderExpressionList}.
	 * @param ctx the parse tree
	 */
	void exitOrderExpressionList(IQL_TestParser.OrderExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#orderExpression}.
	 * @param ctx the parse tree
	 */
	void enterOrderExpression(IQL_TestParser.OrderExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#orderExpression}.
	 * @param ctx the parse tree
	 */
	void exitOrderExpression(IQL_TestParser.OrderExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#constraint}.
	 * @param ctx the parse tree
	 */
	void enterConstraint(IQL_TestParser.ConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#constraint}.
	 * @param ctx the parse tree
	 */
	void exitConstraint(IQL_TestParser.ConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void enterExpressionList(IQL_TestParser.ExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void exitExpressionList(IQL_TestParser.ExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryExpression}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpression(IQL_TestParser.PrimaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryExpression}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpression(IQL_TestParser.PrimaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code disjunction}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterDisjunction(IQL_TestParser.DisjunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code disjunction}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitDisjunction(IQL_TestParser.DisjunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code pathAccess}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPathAccess(IQL_TestParser.PathAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code pathAccess}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPathAccess(IQL_TestParser.PathAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code forEach}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterForEach(IQL_TestParser.ForEachContext ctx);
	/**
	 * Exit a parse tree produced by the {@code forEach}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitForEach(IQL_TestParser.ForEachContext ctx);
	/**
	 * Enter a parse tree produced by the {@code methodInvocation}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMethodInvocation(IQL_TestParser.MethodInvocationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code methodInvocation}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMethodInvocation(IQL_TestParser.MethodInvocationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code additiveOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveOp(IQL_TestParser.AdditiveOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code additiveOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveOp(IQL_TestParser.AdditiveOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryOp(IQL_TestParser.UnaryOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryOp(IQL_TestParser.UnaryOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparisonOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOp(IQL_TestParser.ComparisonOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparisonOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOp(IQL_TestParser.ComparisonOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignmentOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentOp(IQL_TestParser.AssignmentOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignmentOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentOp(IQL_TestParser.AssignmentOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterStringOp(IQL_TestParser.StringOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitStringOp(IQL_TestParser.StringOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code wrappingExpression}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterWrappingExpression(IQL_TestParser.WrappingExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code wrappingExpression}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitWrappingExpression(IQL_TestParser.WrappingExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code castExpression}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterCastExpression(IQL_TestParser.CastExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code castExpression}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitCastExpression(IQL_TestParser.CastExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code listAccess}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterListAccess(IQL_TestParser.ListAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code listAccess}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitListAccess(IQL_TestParser.ListAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setPredicate}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSetPredicate(IQL_TestParser.SetPredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setPredicate}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSetPredicate(IQL_TestParser.SetPredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code equalityCheck}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterEqualityCheck(IQL_TestParser.EqualityCheckContext ctx);
	/**
	 * Exit a parse tree produced by the {@code equalityCheck}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitEqualityCheck(IQL_TestParser.EqualityCheckContext ctx);
	/**
	 * Enter a parse tree produced by the {@code conjunction}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterConjunction(IQL_TestParser.ConjunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code conjunction}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitConjunction(IQL_TestParser.ConjunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code annotationAccess}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAnnotationAccess(IQL_TestParser.AnnotationAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code annotationAccess}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAnnotationAccess(IQL_TestParser.AnnotationAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multiplicativeOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeOp(IQL_TestParser.MultiplicativeOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multiplicativeOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeOp(IQL_TestParser.MultiplicativeOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code bitwiseOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBitwiseOp(IQL_TestParser.BitwiseOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code bitwiseOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBitwiseOp(IQL_TestParser.BitwiseOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ternaryOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTernaryOp(IQL_TestParser.TernaryOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ternaryOp}
	 * labeled alternative in {@link IQL_TestParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTernaryOp(IQL_TestParser.TernaryOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(IQL_TestParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(IQL_TestParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#listStatement}.
	 * @param ctx the parse tree
	 */
	void enterListStatement(IQL_TestParser.ListStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#listStatement}.
	 * @param ctx the parse tree
	 */
	void exitListStatement(IQL_TestParser.ListStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#reference}.
	 * @param ctx the parse tree
	 */
	void enterReference(IQL_TestParser.ReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#reference}.
	 * @param ctx the parse tree
	 */
	void exitReference(IQL_TestParser.ReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#qualifiedIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedIdentifier(IQL_TestParser.QualifiedIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#qualifiedIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedIdentifier(IQL_TestParser.QualifiedIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#loopExpresseion}.
	 * @param ctx the parse tree
	 */
	void enterLoopExpresseion(IQL_TestParser.LoopExpresseionContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#loopExpresseion}.
	 * @param ctx the parse tree
	 */
	void exitLoopExpresseion(IQL_TestParser.LoopExpresseionContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#loopControl}.
	 * @param ctx the parse tree
	 */
	void enterLoopControl(IQL_TestParser.LoopControlContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#loopControl}.
	 * @param ctx the parse tree
	 */
	void exitLoopControl(IQL_TestParser.LoopControlContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#boundedRange}.
	 * @param ctx the parse tree
	 */
	void enterBoundedRange(IQL_TestParser.BoundedRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#boundedRange}.
	 * @param ctx the parse tree
	 */
	void exitBoundedRange(IQL_TestParser.BoundedRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#counterList}.
	 * @param ctx the parse tree
	 */
	void enterCounterList(IQL_TestParser.CounterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#counterList}.
	 * @param ctx the parse tree
	 */
	void exitCounterList(IQL_TestParser.CounterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#counter}.
	 * @param ctx the parse tree
	 */
	void enterCounter(IQL_TestParser.CounterContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#counter}.
	 * @param ctx the parse tree
	 */
	void exitCounter(IQL_TestParser.CounterContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(IQL_TestParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(IQL_TestParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#quantifier}.
	 * @param ctx the parse tree
	 */
	void enterQuantifier(IQL_TestParser.QuantifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#quantifier}.
	 * @param ctx the parse tree
	 */
	void exitQuantifier(IQL_TestParser.QuantifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#simpleQuantifier}.
	 * @param ctx the parse tree
	 */
	void enterSimpleQuantifier(IQL_TestParser.SimpleQuantifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#simpleQuantifier}.
	 * @param ctx the parse tree
	 */
	void exitSimpleQuantifier(IQL_TestParser.SimpleQuantifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#not}.
	 * @param ctx the parse tree
	 */
	void enterNot(IQL_TestParser.NotContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#not}.
	 * @param ctx the parse tree
	 */
	void exitNot(IQL_TestParser.NotContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#all}.
	 * @param ctx the parse tree
	 */
	void enterAll(IQL_TestParser.AllContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#all}.
	 * @param ctx the parse tree
	 */
	void exitAll(IQL_TestParser.AllContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#and}.
	 * @param ctx the parse tree
	 */
	void enterAnd(IQL_TestParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#and}.
	 * @param ctx the parse tree
	 */
	void exitAnd(IQL_TestParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#or}.
	 * @param ctx the parse tree
	 */
	void enterOr(IQL_TestParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#or}.
	 * @param ctx the parse tree
	 */
	void exitOr(IQL_TestParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#variable}.
	 * @param ctx the parse tree
	 */
	void enterVariable(IQL_TestParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#variable}.
	 * @param ctx the parse tree
	 */
	void exitVariable(IQL_TestParser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#member}.
	 * @param ctx the parse tree
	 */
	void enterMember(IQL_TestParser.MemberContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#member}.
	 * @param ctx the parse tree
	 */
	void exitMember(IQL_TestParser.MemberContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#versionDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterVersionDeclaration(IQL_TestParser.VersionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#versionDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitVersionDeclaration(IQL_TestParser.VersionDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#nullLiteral}.
	 * @param ctx the parse tree
	 */
	void enterNullLiteral(IQL_TestParser.NullLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#nullLiteral}.
	 * @param ctx the parse tree
	 */
	void exitNullLiteral(IQL_TestParser.NullLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#floatingPointLiteral}.
	 * @param ctx the parse tree
	 */
	void enterFloatingPointLiteral(IQL_TestParser.FloatingPointLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#floatingPointLiteral}.
	 * @param ctx the parse tree
	 */
	void exitFloatingPointLiteral(IQL_TestParser.FloatingPointLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#signedFloatingPointLiteral}.
	 * @param ctx the parse tree
	 */
	void enterSignedFloatingPointLiteral(IQL_TestParser.SignedFloatingPointLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#signedFloatingPointLiteral}.
	 * @param ctx the parse tree
	 */
	void exitSignedFloatingPointLiteral(IQL_TestParser.SignedFloatingPointLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#unsignedFloatingPointLiteral}.
	 * @param ctx the parse tree
	 */
	void enterUnsignedFloatingPointLiteral(IQL_TestParser.UnsignedFloatingPointLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#unsignedFloatingPointLiteral}.
	 * @param ctx the parse tree
	 */
	void exitUnsignedFloatingPointLiteral(IQL_TestParser.UnsignedFloatingPointLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiteral(IQL_TestParser.BooleanLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiteral(IQL_TestParser.BooleanLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteral(IQL_TestParser.IntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteral(IQL_TestParser.IntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#signedIntegerLiteral}.
	 * @param ctx the parse tree
	 */
	void enterSignedIntegerLiteral(IQL_TestParser.SignedIntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#signedIntegerLiteral}.
	 * @param ctx the parse tree
	 */
	void exitSignedIntegerLiteral(IQL_TestParser.SignedIntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#unsignedIntegerLiteral}.
	 * @param ctx the parse tree
	 */
	void enterUnsignedIntegerLiteral(IQL_TestParser.UnsignedIntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#unsignedIntegerLiteral}.
	 * @param ctx the parse tree
	 */
	void exitUnsignedIntegerLiteral(IQL_TestParser.UnsignedIntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQL_TestParser#sign}.
	 * @param ctx the parse tree
	 */
	void enterSign(IQL_TestParser.SignContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQL_TestParser#sign}.
	 * @param ctx the parse tree
	 */
	void exitSign(IQL_TestParser.SignContext ctx);
}