// Generated from de/ims/icarus2/query/api/iql/antlr/IQL.g4 by ANTLR 4.13.2

package de.ims.icarus2.query.api.iql.antlr;
import org.antlr.v4.runtime.misc.Interval;	

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link IQLParser}.
 */
public interface IQLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link IQLParser#standaloneNodeStatement}.
	 * @param ctx the parse tree
	 */
	void enterStandaloneNodeStatement(IQLParser.StandaloneNodeStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#standaloneNodeStatement}.
	 * @param ctx the parse tree
	 */
	void exitStandaloneNodeStatement(IQLParser.StandaloneNodeStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#standaloneStructuralConstraint}.
	 * @param ctx the parse tree
	 */
	void enterStandaloneStructuralConstraint(IQLParser.StandaloneStructuralConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#standaloneStructuralConstraint}.
	 * @param ctx the parse tree
	 */
	void exitStandaloneStructuralConstraint(IQLParser.StandaloneStructuralConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#standaloneSelectiveStatement}.
	 * @param ctx the parse tree
	 */
	void enterStandaloneSelectiveStatement(IQLParser.StandaloneSelectiveStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#standaloneSelectiveStatement}.
	 * @param ctx the parse tree
	 */
	void exitStandaloneSelectiveStatement(IQLParser.StandaloneSelectiveStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#standaloneExpression}.
	 * @param ctx the parse tree
	 */
	void enterStandaloneExpression(IQLParser.StandaloneExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#standaloneExpression}.
	 * @param ctx the parse tree
	 */
	void exitStandaloneExpression(IQLParser.StandaloneExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#payloadStatement}.
	 * @param ctx the parse tree
	 */
	void enterPayloadStatement(IQLParser.PayloadStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#payloadStatement}.
	 * @param ctx the parse tree
	 */
	void exitPayloadStatement(IQLParser.PayloadStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#bindingsList}.
	 * @param ctx the parse tree
	 */
	void enterBindingsList(IQLParser.BindingsListContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#bindingsList}.
	 * @param ctx the parse tree
	 */
	void exitBindingsList(IQLParser.BindingsListContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#binding}.
	 * @param ctx the parse tree
	 */
	void enterBinding(IQLParser.BindingContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#binding}.
	 * @param ctx the parse tree
	 */
	void exitBinding(IQLParser.BindingContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#selectionStatement}.
	 * @param ctx the parse tree
	 */
	void enterSelectionStatement(IQLParser.SelectionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#selectionStatement}.
	 * @param ctx the parse tree
	 */
	void exitSelectionStatement(IQLParser.SelectionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#laneStatementsList}.
	 * @param ctx the parse tree
	 */
	void enterLaneStatementsList(IQLParser.LaneStatementsListContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#laneStatementsList}.
	 * @param ctx the parse tree
	 */
	void exitLaneStatementsList(IQLParser.LaneStatementsListContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#laneStatement}.
	 * @param ctx the parse tree
	 */
	void enterLaneStatement(IQLParser.LaneStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#laneStatement}.
	 * @param ctx the parse tree
	 */
	void exitLaneStatement(IQLParser.LaneStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#structuralConstraint}.
	 * @param ctx the parse tree
	 */
	void enterStructuralConstraint(IQLParser.StructuralConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#structuralConstraint}.
	 * @param ctx the parse tree
	 */
	void exitStructuralConstraint(IQLParser.StructuralConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#hitsLimit}.
	 * @param ctx the parse tree
	 */
	void enterHitsLimit(IQLParser.HitsLimitContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#hitsLimit}.
	 * @param ctx the parse tree
	 */
	void exitHitsLimit(IQLParser.HitsLimitContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#matchFlag}.
	 * @param ctx the parse tree
	 */
	void enterMatchFlag(IQLParser.MatchFlagContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#matchFlag}.
	 * @param ctx the parse tree
	 */
	void exitMatchFlag(IQLParser.MatchFlagContext ctx);
	/**
	 * Enter a parse tree produced by the {@code elementSequence}
	 * labeled alternative in {@link IQLParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void enterElementSequence(IQLParser.ElementSequenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code elementSequence}
	 * labeled alternative in {@link IQLParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void exitElementSequence(IQLParser.ElementSequenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code elementGrouping}
	 * labeled alternative in {@link IQLParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void enterElementGrouping(IQLParser.ElementGroupingContext ctx);
	/**
	 * Exit a parse tree produced by the {@code elementGrouping}
	 * labeled alternative in {@link IQLParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void exitElementGrouping(IQLParser.ElementGroupingContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleNode}
	 * labeled alternative in {@link IQLParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void enterSingleNode(IQLParser.SingleNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleNode}
	 * labeled alternative in {@link IQLParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void exitSingleNode(IQLParser.SingleNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code graphFragment}
	 * labeled alternative in {@link IQLParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void enterGraphFragment(IQLParser.GraphFragmentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code graphFragment}
	 * labeled alternative in {@link IQLParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void exitGraphFragment(IQLParser.GraphFragmentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code elementDisjunction}
	 * labeled alternative in {@link IQLParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void enterElementDisjunction(IQLParser.ElementDisjunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code elementDisjunction}
	 * labeled alternative in {@link IQLParser#nodeStatement}.
	 * @param ctx the parse tree
	 */
	void exitElementDisjunction(IQLParser.ElementDisjunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#nodeArrangement}.
	 * @param ctx the parse tree
	 */
	void enterNodeArrangement(IQLParser.NodeArrangementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#nodeArrangement}.
	 * @param ctx the parse tree
	 */
	void exitNodeArrangement(IQLParser.NodeArrangementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dummyNode}
	 * labeled alternative in {@link IQLParser#node}.
	 * @param ctx the parse tree
	 */
	void enterDummyNode(IQLParser.DummyNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dummyNode}
	 * labeled alternative in {@link IQLParser#node}.
	 * @param ctx the parse tree
	 */
	void exitDummyNode(IQLParser.DummyNodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code properNode}
	 * labeled alternative in {@link IQLParser#node}.
	 * @param ctx the parse tree
	 */
	void enterProperNode(IQLParser.ProperNodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code properNode}
	 * labeled alternative in {@link IQLParser#node}.
	 * @param ctx the parse tree
	 */
	void exitProperNode(IQLParser.ProperNodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#memberLabel}.
	 * @param ctx the parse tree
	 */
	void enterMemberLabel(IQLParser.MemberLabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#memberLabel}.
	 * @param ctx the parse tree
	 */
	void exitMemberLabel(IQLParser.MemberLabelContext ctx);
	/**
	 * Enter a parse tree produced by the {@code markerConjunction}
	 * labeled alternative in {@link IQLParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void enterMarkerConjunction(IQLParser.MarkerConjunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code markerConjunction}
	 * labeled alternative in {@link IQLParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void exitMarkerConjunction(IQLParser.MarkerConjunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code markerDisjunction}
	 * labeled alternative in {@link IQLParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void enterMarkerDisjunction(IQLParser.MarkerDisjunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code markerDisjunction}
	 * labeled alternative in {@link IQLParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void exitMarkerDisjunction(IQLParser.MarkerDisjunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code markerCall}
	 * labeled alternative in {@link IQLParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void enterMarkerCall(IQLParser.MarkerCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code markerCall}
	 * labeled alternative in {@link IQLParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void exitMarkerCall(IQLParser.MarkerCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code markerWrapping}
	 * labeled alternative in {@link IQLParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void enterMarkerWrapping(IQLParser.MarkerWrappingContext ctx);
	/**
	 * Exit a parse tree produced by the {@code markerWrapping}
	 * labeled alternative in {@link IQLParser#positionMarker}.
	 * @param ctx the parse tree
	 */
	void exitMarkerWrapping(IQLParser.MarkerWrappingContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#positionArgument}.
	 * @param ctx the parse tree
	 */
	void enterPositionArgument(IQLParser.PositionArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#positionArgument}.
	 * @param ctx the parse tree
	 */
	void exitPositionArgument(IQLParser.PositionArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#element}.
	 * @param ctx the parse tree
	 */
	void enterElement(IQLParser.ElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#element}.
	 * @param ctx the parse tree
	 */
	void exitElement(IQLParser.ElementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code emptyEdge}
	 * labeled alternative in {@link IQLParser#edge}.
	 * @param ctx the parse tree
	 */
	void enterEmptyEdge(IQLParser.EmptyEdgeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code emptyEdge}
	 * labeled alternative in {@link IQLParser#edge}.
	 * @param ctx the parse tree
	 */
	void exitEmptyEdge(IQLParser.EmptyEdgeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code filledEdge}
	 * labeled alternative in {@link IQLParser#edge}.
	 * @param ctx the parse tree
	 */
	void enterFilledEdge(IQLParser.FilledEdgeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code filledEdge}
	 * labeled alternative in {@link IQLParser#edge}.
	 * @param ctx the parse tree
	 */
	void exitFilledEdge(IQLParser.FilledEdgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#leftEdgePart}.
	 * @param ctx the parse tree
	 */
	void enterLeftEdgePart(IQLParser.LeftEdgePartContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#leftEdgePart}.
	 * @param ctx the parse tree
	 */
	void exitLeftEdgePart(IQLParser.LeftEdgePartContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#rightEdgePart}.
	 * @param ctx the parse tree
	 */
	void enterRightEdgePart(IQLParser.RightEdgePartContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#rightEdgePart}.
	 * @param ctx the parse tree
	 */
	void exitRightEdgePart(IQLParser.RightEdgePartContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#directedEdgeLeft}.
	 * @param ctx the parse tree
	 */
	void enterDirectedEdgeLeft(IQLParser.DirectedEdgeLeftContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#directedEdgeLeft}.
	 * @param ctx the parse tree
	 */
	void exitDirectedEdgeLeft(IQLParser.DirectedEdgeLeftContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#directedEdgeRight}.
	 * @param ctx the parse tree
	 */
	void enterDirectedEdgeRight(IQLParser.DirectedEdgeRightContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#directedEdgeRight}.
	 * @param ctx the parse tree
	 */
	void exitDirectedEdgeRight(IQLParser.DirectedEdgeRightContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#undirectedEdge}.
	 * @param ctx the parse tree
	 */
	void enterUndirectedEdge(IQLParser.UndirectedEdgeContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#undirectedEdge}.
	 * @param ctx the parse tree
	 */
	void exitUndirectedEdge(IQLParser.UndirectedEdgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#groupStatement}.
	 * @param ctx the parse tree
	 */
	void enterGroupStatement(IQLParser.GroupStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#groupStatement}.
	 * @param ctx the parse tree
	 */
	void exitGroupStatement(IQLParser.GroupStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#groupExpression}.
	 * @param ctx the parse tree
	 */
	void enterGroupExpression(IQLParser.GroupExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#groupExpression}.
	 * @param ctx the parse tree
	 */
	void exitGroupExpression(IQLParser.GroupExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#resultStatement}.
	 * @param ctx the parse tree
	 */
	void enterResultStatement(IQLParser.ResultStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#resultStatement}.
	 * @param ctx the parse tree
	 */
	void exitResultStatement(IQLParser.ResultStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#orderExpressionList}.
	 * @param ctx the parse tree
	 */
	void enterOrderExpressionList(IQLParser.OrderExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#orderExpressionList}.
	 * @param ctx the parse tree
	 */
	void exitOrderExpressionList(IQLParser.OrderExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#orderExpression}.
	 * @param ctx the parse tree
	 */
	void enterOrderExpression(IQLParser.OrderExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#orderExpression}.
	 * @param ctx the parse tree
	 */
	void exitOrderExpression(IQLParser.OrderExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#constraint}.
	 * @param ctx the parse tree
	 */
	void enterConstraint(IQLParser.ConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#constraint}.
	 * @param ctx the parse tree
	 */
	void exitConstraint(IQLParser.ConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void enterExpressionList(IQLParser.ExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void exitExpressionList(IQLParser.ExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryExpression}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpression(IQLParser.PrimaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryExpression}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpression(IQLParser.PrimaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code disjunction}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterDisjunction(IQLParser.DisjunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code disjunction}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitDisjunction(IQLParser.DisjunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code pathAccess}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPathAccess(IQLParser.PathAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code pathAccess}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPathAccess(IQLParser.PathAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code forEach}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterForEach(IQLParser.ForEachContext ctx);
	/**
	 * Exit a parse tree produced by the {@code forEach}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitForEach(IQLParser.ForEachContext ctx);
	/**
	 * Enter a parse tree produced by the {@code methodInvocation}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMethodInvocation(IQLParser.MethodInvocationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code methodInvocation}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMethodInvocation(IQLParser.MethodInvocationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code additiveOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveOp(IQLParser.AdditiveOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code additiveOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveOp(IQLParser.AdditiveOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryOp(IQLParser.UnaryOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryOp(IQLParser.UnaryOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparisonOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOp(IQLParser.ComparisonOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparisonOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOp(IQLParser.ComparisonOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignmentOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentOp(IQLParser.AssignmentOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignmentOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentOp(IQLParser.AssignmentOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterStringOp(IQLParser.StringOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitStringOp(IQLParser.StringOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code wrappingExpression}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterWrappingExpression(IQLParser.WrappingExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code wrappingExpression}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitWrappingExpression(IQLParser.WrappingExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code castExpression}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterCastExpression(IQLParser.CastExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code castExpression}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitCastExpression(IQLParser.CastExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code listAccess}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterListAccess(IQLParser.ListAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code listAccess}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitListAccess(IQLParser.ListAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setPredicate}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSetPredicate(IQLParser.SetPredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setPredicate}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSetPredicate(IQLParser.SetPredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code equalityCheck}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterEqualityCheck(IQLParser.EqualityCheckContext ctx);
	/**
	 * Exit a parse tree produced by the {@code equalityCheck}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitEqualityCheck(IQLParser.EqualityCheckContext ctx);
	/**
	 * Enter a parse tree produced by the {@code conjunction}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterConjunction(IQLParser.ConjunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code conjunction}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitConjunction(IQLParser.ConjunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code annotationAccess}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAnnotationAccess(IQLParser.AnnotationAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code annotationAccess}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAnnotationAccess(IQLParser.AnnotationAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multiplicativeOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeOp(IQLParser.MultiplicativeOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multiplicativeOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeOp(IQLParser.MultiplicativeOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code bitwiseOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBitwiseOp(IQLParser.BitwiseOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code bitwiseOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBitwiseOp(IQLParser.BitwiseOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ternaryOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTernaryOp(IQLParser.TernaryOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ternaryOp}
	 * labeled alternative in {@link IQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTernaryOp(IQLParser.TernaryOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(IQLParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(IQLParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#listStatement}.
	 * @param ctx the parse tree
	 */
	void enterListStatement(IQLParser.ListStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#listStatement}.
	 * @param ctx the parse tree
	 */
	void exitListStatement(IQLParser.ListStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#reference}.
	 * @param ctx the parse tree
	 */
	void enterReference(IQLParser.ReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#reference}.
	 * @param ctx the parse tree
	 */
	void exitReference(IQLParser.ReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#qualifiedIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedIdentifier(IQLParser.QualifiedIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#qualifiedIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedIdentifier(IQLParser.QualifiedIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#loopExpresseion}.
	 * @param ctx the parse tree
	 */
	void enterLoopExpresseion(IQLParser.LoopExpresseionContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#loopExpresseion}.
	 * @param ctx the parse tree
	 */
	void exitLoopExpresseion(IQLParser.LoopExpresseionContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#loopControl}.
	 * @param ctx the parse tree
	 */
	void enterLoopControl(IQLParser.LoopControlContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#loopControl}.
	 * @param ctx the parse tree
	 */
	void exitLoopControl(IQLParser.LoopControlContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#boundedRange}.
	 * @param ctx the parse tree
	 */
	void enterBoundedRange(IQLParser.BoundedRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#boundedRange}.
	 * @param ctx the parse tree
	 */
	void exitBoundedRange(IQLParser.BoundedRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#counterList}.
	 * @param ctx the parse tree
	 */
	void enterCounterList(IQLParser.CounterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#counterList}.
	 * @param ctx the parse tree
	 */
	void exitCounterList(IQLParser.CounterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#counter}.
	 * @param ctx the parse tree
	 */
	void enterCounter(IQLParser.CounterContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#counter}.
	 * @param ctx the parse tree
	 */
	void exitCounter(IQLParser.CounterContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(IQLParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(IQLParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#quantifier}.
	 * @param ctx the parse tree
	 */
	void enterQuantifier(IQLParser.QuantifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#quantifier}.
	 * @param ctx the parse tree
	 */
	void exitQuantifier(IQLParser.QuantifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#simpleQuantifier}.
	 * @param ctx the parse tree
	 */
	void enterSimpleQuantifier(IQLParser.SimpleQuantifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#simpleQuantifier}.
	 * @param ctx the parse tree
	 */
	void exitSimpleQuantifier(IQLParser.SimpleQuantifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#not}.
	 * @param ctx the parse tree
	 */
	void enterNot(IQLParser.NotContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#not}.
	 * @param ctx the parse tree
	 */
	void exitNot(IQLParser.NotContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#all}.
	 * @param ctx the parse tree
	 */
	void enterAll(IQLParser.AllContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#all}.
	 * @param ctx the parse tree
	 */
	void exitAll(IQLParser.AllContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#and}.
	 * @param ctx the parse tree
	 */
	void enterAnd(IQLParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#and}.
	 * @param ctx the parse tree
	 */
	void exitAnd(IQLParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#or}.
	 * @param ctx the parse tree
	 */
	void enterOr(IQLParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#or}.
	 * @param ctx the parse tree
	 */
	void exitOr(IQLParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#variable}.
	 * @param ctx the parse tree
	 */
	void enterVariable(IQLParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#variable}.
	 * @param ctx the parse tree
	 */
	void exitVariable(IQLParser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#member}.
	 * @param ctx the parse tree
	 */
	void enterMember(IQLParser.MemberContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#member}.
	 * @param ctx the parse tree
	 */
	void exitMember(IQLParser.MemberContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#versionDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterVersionDeclaration(IQLParser.VersionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#versionDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitVersionDeclaration(IQLParser.VersionDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#nullLiteral}.
	 * @param ctx the parse tree
	 */
	void enterNullLiteral(IQLParser.NullLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#nullLiteral}.
	 * @param ctx the parse tree
	 */
	void exitNullLiteral(IQLParser.NullLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#floatingPointLiteral}.
	 * @param ctx the parse tree
	 */
	void enterFloatingPointLiteral(IQLParser.FloatingPointLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#floatingPointLiteral}.
	 * @param ctx the parse tree
	 */
	void exitFloatingPointLiteral(IQLParser.FloatingPointLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#signedFloatingPointLiteral}.
	 * @param ctx the parse tree
	 */
	void enterSignedFloatingPointLiteral(IQLParser.SignedFloatingPointLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#signedFloatingPointLiteral}.
	 * @param ctx the parse tree
	 */
	void exitSignedFloatingPointLiteral(IQLParser.SignedFloatingPointLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#unsignedFloatingPointLiteral}.
	 * @param ctx the parse tree
	 */
	void enterUnsignedFloatingPointLiteral(IQLParser.UnsignedFloatingPointLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#unsignedFloatingPointLiteral}.
	 * @param ctx the parse tree
	 */
	void exitUnsignedFloatingPointLiteral(IQLParser.UnsignedFloatingPointLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiteral(IQLParser.BooleanLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiteral(IQLParser.BooleanLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteral(IQLParser.IntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteral(IQLParser.IntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#signedIntegerLiteral}.
	 * @param ctx the parse tree
	 */
	void enterSignedIntegerLiteral(IQLParser.SignedIntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#signedIntegerLiteral}.
	 * @param ctx the parse tree
	 */
	void exitSignedIntegerLiteral(IQLParser.SignedIntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#unsignedIntegerLiteral}.
	 * @param ctx the parse tree
	 */
	void enterUnsignedIntegerLiteral(IQLParser.UnsignedIntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#unsignedIntegerLiteral}.
	 * @param ctx the parse tree
	 */
	void exitUnsignedIntegerLiteral(IQLParser.UnsignedIntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IQLParser#sign}.
	 * @param ctx the parse tree
	 */
	void enterSign(IQLParser.SignContext ctx);
	/**
	 * Exit a parse tree produced by {@link IQLParser#sign}.
	 * @param ctx the parse tree
	 */
	void exitSign(IQLParser.SignContext ctx);
}