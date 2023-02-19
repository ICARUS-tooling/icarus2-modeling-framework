/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package de.ims.icarus2.query.api.engine.matcher;

import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.lang.Primitives._boolean;
import static de.ims.icarus2.util.lang.Primitives._float;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.factories.Forms;
import com.jgoodies.forms.factories.Paddings;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.standard.members.item.DefaultItem;
import de.ims.icarus2.query.api.engine.QueryProcessor;
import de.ims.icarus2.query.api.engine.QueryTestUtils;
import de.ims.icarus2.query.api.engine.ThreadVerifier;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Monitor;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Node;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.NodeInfo;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.NodeInfo.Field;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.NodeInfo.Type;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Role;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Snapshot;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.State;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.StructureMatcher;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.LaneContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.RootContext;
import de.ims.icarus2.query.api.exp.EvaluationUtils;
import de.ims.icarus2.query.api.exp.env.SharedUtilityEnvironments;
import de.ims.icarus2.query.api.iql.IqlBinding;
import de.ims.icarus2.query.api.iql.IqlConstraint.BooleanOperation;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlPredicate;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlTerm;
import de.ims.icarus2.query.api.iql.IqlElement.IqlEdge;
import de.ims.icarus2.query.api.iql.IqlElement.IqlElementDisjunction;
import de.ims.icarus2.query.api.iql.IqlElement.IqlGrouping;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlElement.IqlSequence;
import de.ims.icarus2.query.api.iql.IqlElement.IqlTreeNode;
import de.ims.icarus2.query.api.iql.IqlGroup;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerCall;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerExpression;
import de.ims.icarus2.query.api.iql.IqlMarker.MarkerExpressionType;
import de.ims.icarus2.query.api.iql.IqlPayload;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlQueryElement;
import de.ims.icarus2.query.api.iql.IqlResult;
import de.ims.icarus2.query.api.iql.IqlScope;
import de.ims.icarus2.query.api.iql.IqlSorting;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.test.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class InteractiveMatcher {

	public static void main(String[] args) {

//		String payload = StructurePatternTest.expand(
//				"{{[isAt(2) || isFirst, $X][]}[isNotAt(3) || isLast,$X]}");
//		StructurePattern pattern = StructurePatternTest.builder(payload)
//				.allowMonitor(true)
//				.build();

		SwingUtilities.invokeLater(() -> {
			InteractiveMatcher im = new InteractiveMatcher();
			im.showFrame("{{[isAt(2) || isFirst, $X][]}[isNotAt(3) || isLast,$X]}", "XXXX");
		});
	}


//	static {
//		 try {
//			mxGraphTransferable.dataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
//					+ "; class=com.mxgraph.swing.util.mxGraphTransferable", null,
//					mxGraphTransferable.class.getClassLoader());
//		} catch (ClassNotFoundException e) {
//			throw new InternalError(e);
//		}
//	}

	private static final String TITLE = "ICARUS2 - IQL Interactive Matcher";

	private volatile static InteractiveMatcher instance;

	public static InteractiveMatcher getInstance() {
		InteractiveMatcher result = instance;

		if (result == null) {
			synchronized (InteractiveMatcher.class) {
				result = instance;

				if (result == null) {
					instance = new InteractiveMatcher();
					result = instance;
				}
			}
		}

		return result;
	}

	final Lanes lanes;
	final mxStylesheet stylesheet;
	final mxGraphModel model;
	final mxGraph graph;
	final mxGraphComponent graphComponent;

	private volatile JFrame frame;

	/** Maps general node ids to info objects */
	private final Int2ObjectMap<NodeInfo> id2info = new Int2ObjectOpenHashMap<>();
	/** Maps "proper" node ids to info obejcts */
	private final Int2ObjectMap<NodeInfo> node2info = new Int2ObjectOpenHashMap<>();
	private final Map<NodeInfo, mxCell> sm2graph = new Reference2ObjectOpenHashMap<>();
	private final Map<IqlQueryElement, mxCell> iql2graph = new Reference2ObjectOpenHashMap<>();
	private final List<mxCell> targets = new ArrayList<>();

	private mxCell dummyTarget;

	InteractiveMatcher() {
		stylesheet = createStylesheet();
		mxCell root = createRoot();
		model = createModel(root);
		graph = createGraph(model, stylesheet);
		lanes = new Lanes();
		graphComponent = createGraphComponent(graph);

		initGraphStructure();
	}

	private interface Styles {
		static final String VERTEX = "defaultVertex";

		static final String LANE = "lane";

		static final String SM_BASIC = "sm_basic";
		static final String SM_TERMINAL = "sm_terminal";
		static final String SM_BRANCH_START = "sm_branch_start";
		static final String SM_BRANCH_END = "sm_branch_end";
		static final String SM_NODE = "sm_node";
		static final String SM_OWNER = "sm_owner";
		static final String SM_EDGE = "sm_edge";
		static final String SM_EDGE_ATOM = "sm_edge_atom";
		static final String SM_EDGE_BRANCH = "sm_edge_branch";
		static final String SM_ATOM_ROOT = "sm_atom_root";

		static final String IQL_BASIC = "iql_basic";
		static final String IQL_EDGE = "iql_edge";

		static final String TARGET_BASIC = "target_basic";
		static final String TARGET_DUMMY = "target_dummy";

		static final String LINK_RESULT = "link_result";
		static final String LINK_STEP = "link_step";
	}

	private mxStylesheet createStylesheet() {
		mxStylesheet stylesheet = new mxStylesheet();

		StyleBuilder builder = StyleBuilder.forStylesheet(stylesheet);

		builder.newStyle(Styles.LANE)
			.newEntry(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_SWIMLANE)
			.newEntry(mxConstants.STYLE_SWIMLANE_LINE, "1")
			.newEntry(mxConstants.STYLE_SWIMLANE_FILLCOLOR, "white")
			.newEntry(mxConstants.STYLE_SEPARATORCOLOR, "black")
			.newEntry(mxConstants.STYLE_STARTSIZE, _int(25))
			.newEntry(mxConstants.STYLE_HORIZONTAL, _boolean(false))
			.newEntry(mxConstants.STYLE_SPACING, _int(4))
			.newEntry(mxConstants.STYLE_FOLDABLE, "1")
			.commit();

		builder.newStyle(Styles.SM_EDGE)
			.newEntry(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC)
			.commit();

		builder.newStyle(Styles.SM_EDGE_ATOM, Styles.SM_EDGE)
			.newEntry(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_DIAMOND)
			.commit();

		builder.newStyle(Styles.SM_EDGE_BRANCH, Styles.SM_EDGE)
			.newEntry(mxConstants.STYLE_DASHED, _boolean(true))
			.commit();

		builder.newStyle(Styles.SM_BASIC)
			.newEntry(mxConstants.STYLE_FILLCOLOR, mxConstants.NONE)
			.newEntry(mxConstants.STYLE_STROKECOLOR, "black")
			.newEntry(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE)
			.newEntry(mxConstants.STYLE_STROKEWIDTH, _float(1.5F))
//			.newEntry(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_MIDDLE)
//			.newEntry(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE)
			.newEntry(mxConstants.STYLE_FOLDABLE, "0")
			.newEntry(mxConstants.STYLE_AUTOSIZE, "1")
			.commit();

		builder.newStyle(Styles.SM_NODE, Styles.SM_BASIC)
			.newEntry(mxConstants.STYLE_FILLCOLOR, "#FFFF7F")
			.newEntry(mxConstants.STYLE_STROKECOLOR, "#FF5500")
			.newEntry(mxConstants.STYLE_STROKEWIDTH, _float(1.0F))
			.newEntry(mxConstants.STYLE_SPACING, _int(3))
			.commit();

		builder.newStyle(Styles.SM_OWNER, Styles.SM_BASIC)
			.newEntry(mxConstants.STYLE_FOLDABLE, "1")
			.newEntry(mxConstants.STYLE_STROKEWIDTH, _float(1.0F))
			.newEntry(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_MIDDLE)
			.newEntry(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP)
			.newEntry(mxConstants.STYLE_SPACING_LEFT, _int(8))
			.newEntry(mxConstants.STYLE_SPACING_TOP, _int(3))
			.newEntry(mxConstants.STYLE_SPACING_BOTTOM, _int(5))
			.newEntry(mxConstants.STYLE_SPACING_RIGHT, _int(3))
			.commit();

		builder.newStyle(Styles.SM_BRANCH_START)
			.newEntry(mxConstants.STYLE_FILLCOLOR, "#55AAFF")
			.newEntry(mxConstants.STYLE_STROKECOLOR, "#002AFF")
			.newEntry(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS)
			.newEntry(mxConstants.STYLE_STROKEWIDTH, _float(1.0F))
			.commit();

		builder.newStyle(Styles.SM_BRANCH_END)
			.newEntry(mxConstants.STYLE_FILLCOLOR, "#55AAFF")
			.newEntry(mxConstants.STYLE_STROKECOLOR, "#002AFF")
			.newEntry(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE)
			.newEntry(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_EAST)
			.newEntry(mxConstants.STYLE_STROKEWIDTH, _float(1.0F))
			.commit();

		builder.newStyle(Styles.SM_TERMINAL)
			.newEntry(mxConstants.STYLE_FILLCOLOR, "#FF2A55")
			.newEntry(mxConstants.STYLE_STROKECOLOR, "#FF0000")
			.newEntry(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE)
			.newEntry(mxConstants.STYLE_STROKEWIDTH, _float(1.0F))
			.commit();

		builder.newStyle(Styles.SM_ATOM_ROOT)
			.newEntry(mxConstants.STYLE_FILLCOLOR, "#55FF7F")
			.newEntry(mxConstants.STYLE_STROKECOLOR, "#618B36")
			.newEntry(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS)
			.newEntry(mxConstants.STYLE_STROKEWIDTH, _float(1.0F))
			.commit();

		builder.newStyle(Styles.TARGET_BASIC)
			.newEntry(mxConstants.STYLE_FILLCOLOR, "FFD455")
			.newEntry(mxConstants.STYLE_STROKECOLOR, "black")
			.newEntry(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE)
			.newEntry(mxConstants.STYLE_FOLDABLE, "0")
			.commit();

		builder.newStyle(Styles.TARGET_DUMMY)
			.newEntry(mxConstants.STYLE_FILLCOLOR, "#FF2A55")
			.newEntry(mxConstants.STYLE_STROKECOLOR, "#FF0000")
			.newEntry(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE)
			.newEntry(mxConstants.STYLE_STROKEWIDTH, _float(1.0F))
			.commit();

		//TODO setup additional styles
		return stylesheet;
	}

	private mxCell createRoot() {
		mxCell root = new mxCell();

		root.insert(new mxCell());

		//TODO setup
		return root;
	}

	private mxGraphModel createModel(mxCell root) {
		mxGraphModel model = new mxGraphModel(root);
		//TODO setup
		return model;
	}

	private mxGraph createGraph(mxGraphModel model, mxStylesheet stylesheet) {
		mxGraph graph = new mxGraph(model, stylesheet){
			@Override
			public String convertValueToString(Object cell) {
				String label = createLabel(cell);
				return label!=null ? label : super.convertValueToString(cell);
			}

			@Override
			public String getToolTipForCell(Object cell) {
				String tooltip = createTooltip(cell);
				return tooltip!=null ? tooltip : super.getToolTipForCell(cell);
			}
		}; //TODO

//		graph.setAutoSizeCells(true);
//		graph.setBorder(30);
		graph.setHtmlLabels(false);
//		graph.setGridSize(10);
//		graph.setAllowDanglingEdges(false);
//		graph.setAutoOrigin(true);
//		graph.setCellsCloneable(false);
//		graph.setCellsEditable(false);
//		graph.setCellsMovable(false);
//		graph.setCellsResizable(false);
//		graph.setCellsSelectable(true);
//		graph.setCellsDisconnectable(false);
//		graph.setConnectableEdges(false);
//		graph.setDropEnabled(false);
//		graph.setEdgeLabelsMovable(false);
//		graph.setGridEnabled(true);
//		graph.setKeepEdgesInBackground(true);
		graph.setLabelsVisible(true);
//		graph.setLabelsClipped(false);
//		graph.setSwimlaneNesting(true);
		graph.setExtendParents(true);
//		graph.setExtendParentsOnAdd(true);
//		graph.setDefaultOverlap(0);

		//TODO setup
		return graph;
	}

	private mxGraphComponent createGraphComponent(mxGraph graph) {
		mxGraphComponent component = new mxGraphComponent(graph);
		component.setAutoExtend(true);
		component.setAntiAlias(true);
		component.setTextAntiAlias(true);
		component.setToolTips(true);
//		component.setImportEnabled(false);
		component.setFoldingEnabled(true);
		component.setConnectable(false);
		component.setDragEnabled(false);
//		component.setKeepSelectionVisibleOnZoom(true);

//		component.getViewport().setBackground(Color.white);
		//TODO setup
		return component;
	}

	private void initGraphStructure() {
//		mxSwimlaneManager swimlaneManager = new mxSwimlaneManager(graph);
//		swimlaneManager.setEnabled(true);
//		swimlaneManager.setAddEnabled(true);
//		swimlaneManager.setResizeEnabled(true);
//		swimlaneManager.setHorizontal(false);

		model.beginUpdate();
		lanes.init(graph, graph.getDefaultParent());
		model.endUpdate();
	}

	private String createLabel(Object cell) {
		Object value = model.getValue(cell);
		if(value instanceof Payload) {
			return ((Payload<?>)value).label;
		}
		return null;
	}

	private String createTooltip(Object cell) {
		Object value = model.getValue(cell);
		if(value instanceof Payload) {
			Payload<?> payload = (Payload<?>) value;
			if(payload.data instanceof NodeInfo) {
				NodeInfo info = (NodeInfo) payload.data;
				StringBuilder sb = new StringBuilder(200);
				sb.append("<html>");
				sb.append("<b>").append(info.getClassLabel())
					.append(" [").append(info.getId()).append("]")
					.append("</b><br>");

				List<Field> fields = info.getProperties().keySet().stream()
						.sorted()
						.collect(Collectors.toList());

				sb.append("<table>");
				sb.append("<tr><td>").append("next").append("</td><td>")
					.append(info.getNext()).append("</td></tr>");
				sb.append("<tr><td>").append("logical_next").append("</td><td>")
					.append(info.getLogicalNext()).append("</td></tr>");

				if(!fields.isEmpty()) {
					for(Field field : fields) {
						sb.append("<tr><td>").append(field.name()).append("</td><td>")
							.append(info.getProperty(field)).append("</td></tr>");
					}
				}
				sb.append("</table>");

				return sb.toString();
			}
		}
		return null;
	}

	void displayStateMachine(StructurePattern pattern) {
		model.beginUpdate();

		layoutStateMachine(pattern.info());

		// Pretty mode for lanes
		lanes.sync(graph);

		model.endUpdate();
	}

	void displayQuery(IqlQueryElement element) {
		model.beginUpdate();

		layoutQuery(element);

		// Pretty mode for lanes
		lanes.sync(graph);

		model.endUpdate();
	}

	void displayTarget(Item[] items) {
		model.beginUpdate();

		layoutTarget(items);

		// Pretty mode for lanes
		lanes.sync(graph);

		model.endUpdate();
	}

	private mxCell lookup(NodeInfo node) {
		mxCell cell = sm2graph.get(node);
		if(cell==null)
			throw new IllegalArgumentException("Unknown node: "+node);
		return cell;
	}

	private mxCell makeSMNode(NodeInfo info, Object parent, boolean isOwner) {

		String style = isOwner ? Styles.SM_OWNER : Styles.SM_BASIC;
		String label = "";
		boolean createDefaultLabel = false;
		int w = 0;
		int h = 0;

		switch (info.getType()) {
		case BRANCH: {
			style = Styles.SM_BRANCH_START;
			w = h = 16;
		} break;
		case BRANCH_CONN: {
			style = Styles.SM_BRANCH_END;
			w = 8;
			h = 16;
		} break;

		case BEGIN:
		case FINISH: {
			style = Styles.SM_TERMINAL;
			w = h = 14;
		} break;

		case EMPTY: {
			style = Styles.SM_NODE;
			label = "[]";
		} break;
		case SINGLE: {
			style = Styles.SM_NODE;
			label = "Node "+info.getProperty(Field.MAPPING);
		} break;

		default:
			createDefaultLabel = true;
			break;
		}

		if(label.isEmpty() && createDefaultLabel) {
			label = info.getClassLabel();
		}

		if(!label.isEmpty()) {
			label += (isOwner ? " " : "\n") +"[id: "+info.getId()+"]";
		}

		String id = "sm"+info.getId();
		Object value = new Payload<>(info, label);
		mxCell cell = (mxCell) graph.insertVertex(parent, id, value, 5, 30, w, h, style);

		if(!label.isEmpty()) {
			graph.cellSizeUpdated(cell, true);
		}
		return cell;
	}

	private mxCell makeSMEdge(mxCell source, mxCell target, Object parent, SMLinkType sMLinkType) {
		String style = Styles.SM_EDGE;

		//TODO customize edge style based on link type
		switch (sMLinkType) {
		case STANDARD: {
			// no-op
		} break;

		case BRANCH: {
			style = Styles.SM_EDGE_BRANCH;
		} break;

		case ATOM: {
			style = Styles.SM_EDGE_ATOM;
		} break;

		default:
			break;
		}

		String id = source.getId()+"-"+target.getId();
		mxCell edge = (mxCell) graph.insertEdge(parent, id, null, source, target, style);

		return edge;
	}

	private mxCell makeSMEdge(@Nullable NodeInfo source, NodeInfo target, Object parent, boolean atom) {
		requireNonNull(target);
		if(source==null) {
			return null;
		}
		SMLinkType sMLinkType = (source.getType()==Type.BRANCH || target.getType()==Type.BRANCH_CONN) ?
				SMLinkType.BRANCH : atom ? SMLinkType.ATOM : SMLinkType.STANDARD;
		return makeSMEdge(lookup(source), lookup(target), parent, sMLinkType);
	}

	private mxCell layoutSMNode(NodeInfo info, @Nullable NodeInfo previous, Object parent, boolean atom) {
		mxCell cell = sm2graph.get(info);
		if(cell==null) {
			boolean isOwner = isOwner(info);
			cell = makeSMNode(info, parent, isOwner);
			sm2graph.put(info, cell);

			for(int id : info.getAtoms()) {
				layoutSMNode(id2info.get(id), isOwner ? null : info, isOwner ? cell : parent, isOwner);
			}

			NodeInfo next = id2info.get(info.getLogicalNext());
			if(next!=null) {
				layoutSMNode(next, info, parent, atom);
			}

			adjustSMNode(cell, info);
		}

		makeSMEdge(previous, info, parent, atom);

		return cell;
	}

	private void adjustSMNode(mxCell cell, NodeInfo info) {
		// Apply tree layout to nested cells
		if(isOwner(info)) {
			mxCompactTreeLayout layout = new mxCompactTreeLayout(graph, true);
			layout.setGroupPadding(7); // default is 10
			layout.setNodeDistance(10); // default is 20
			layout.setLevelDistance(5); // default is 10
			layout.setResizeParent(true);
			layout.setMoveTree(true);

			IntList atoms = info.getAtoms();
			mxCell atomRoot;
//			if(atoms.size()>1) {
				atomRoot = (mxCell) graph.insertVertex(cell,
						cell.getId()+"_atoms", null, 10, 10, 16, 16, Styles.SM_ATOM_ROOT);
				for(int atomId : atoms) {
					graph.insertEdge(cell, null, null, atomRoot,
							sm2graph.get(id2info.get(atomId)), Styles.SM_EDGE_BRANCH);
				}
//			} else {
//				atomRoot = sm2graph.get(id2info.get(atoms.getInt(0)));
//			}
			layout.execute(cell, atomRoot);
			mxGeometry geo = model.getGeometry(cell);
			geo.setHeight(geo.getHeight()+15);
			graph.moveCells(graph.getChildCells(cell), 0, 15);
		}
	}

	private boolean isOwner(NodeInfo info) {
		return info.getAtomCount()>0 && info.getType()!=Type.BRANCH;
	}

	private void layoutStateMachine(NodeInfo[] nodes) {
		Object parent = lanes.smLane();

		// clear parent
		graph.removeCells(mxGraphModel.getChildren(model, parent));
		sm2graph.clear();
		id2info.clear();
		node2info.clear();

		if(nodes.length==0) {
			return;
		}

		NodeInfo root = null;
		// First pass: store mappings and filter nodes
		for(NodeInfo info : nodes) {
			id2info.put(info.getId(), info);
			if(info.getType()==Type.SINGLE) {
				int nodeId = ((Number)info.getProperty(Field.MAPPING)).intValue();
				node2info.put(nodeId, info);
			}
			if(info.getType()==Type.BEGIN) {
				root = info;
			}
		}


		checkState("No root node specified", root!=null);
		// Second pass: make nodes
		layoutSMNode(root, null, parent, false);

		mxCompactTreeLayout layout = new mxCompactTreeLayout(graph, true);
		layout.setGroupPadding(40); // default is 10
		layout.setNodeDistance(10); // default is 20
		layout.setLevelDistance(10); // default is 10
		layout.setResizeParent(true);
		layout.execute(parent, sm2graph.get(root));

//		graph.moveCells(graph.getChildCells(parent), 50, 10);
	}

	private boolean isBranch(NodeInfo info) {
		return info.getType()==Type.BRANCH || info.getType()==Type.BRANCH_CONN;
	}

	private enum SMLinkType {
		STANDARD,
		ATOM,
		BRANCH,
		;
	}

	private mxCell makeIqlNode(IqlQueryElement element, Object parent) {
		String style = Styles.IQL_BASIC;
		String id = "iql"+model.getChildCount(parent);
		String label = element.getClass().getSimpleName();
		int w = 0;
		int h = 0;

		//TODO switch on element type to customize style

		Object value = new Payload<>(element, label);
		mxCell cell = (mxCell) graph.insertVertex(parent, id, value, 0, 0, w, h, style);

		if(!label.isEmpty()) {
			graph.cellSizeUpdated(cell, true);
		}
		return cell;
	}

	private mxCell makeIqlEdge(mxCell source, mxCell target, Object parent, String relation) {
		String style = Styles.IQL_EDGE;

		//TODO customize style

		String id = source.getId()+"-"+target.getId();
		mxCell edge = (mxCell) graph.insertEdge(parent, id, relation, source, target, style);

		return edge;
	}

	private mxCell layoutQueryElement(IqlQueryElement element, @Nullable String relation,
			Object parent, @Nullable mxCell owner) {

		mxCell cell = makeIqlNode(element, parent);
		iql2graph.put(element, cell);

		if(owner!=null) {
			makeIqlEdge(owner, cell, parent, relation);
		}

		String label = null;

		switch (element.getType()) {
		case BINDING: {
			layoutQueryElements(((IqlBinding)element).getMembers(), "member", parent, cell);
		} break;
		case DISJUNCTION: {
			layoutQueryElements(((IqlElementDisjunction)element).getAlternatives(), "alt", parent, cell);
		} break;
		case EDGE: {
			layoutQueryElement(((IqlEdge)element).getSource(), "source", parent, cell);
			layoutQueryElement(((IqlEdge)element).getTarget(), "target", parent, cell);
			layoutQueryElement(((IqlEdge)element).getConstraint(), "constraint", parent, cell);
		} break;
		case GROUP: {
			layoutQueryElement(((IqlGroup)element).getGroupBy(), "groupBy", parent, cell);
			layoutQueryElement(((IqlGroup)element).getFilterOn(), "filterOn", parent, cell);
			layoutQueryElement(((IqlGroup)element).getDefaultValue(), "default", parent, cell);
		} break;
		case GROUPING: {
			layoutQueryElements(((IqlGrouping)element).getQuantifiers(), "quant", parent, cell);
			layoutQueryElement(((IqlGrouping)element).getElement(), "element", parent, cell);
		} break;
		case LANE: {
			layoutQueryElement(((IqlLane)element).getElement(), "element", parent, cell);
		} break;
		case MARKER_CALL: {
			IqlMarkerCall call = (IqlMarkerCall)element;
			label = call.getName(); //TODO add arguments to label
		} break;
		case MARKER_EXPRESSION: {
			IqlMarkerExpression exp = (IqlMarkerExpression)element;
			layoutQueryElements(exp.getItems(), exp.getExpressionType()==MarkerExpressionType.CONJUNCTION ? "and" : "or", parent, cell);
		} break;
		case NODE: {
			layoutQueryElements(((IqlNode)element).getQuantifiers(), "quant", parent, cell);
			layoutQueryElement(((IqlNode)element).getMarker(), "marker", parent, cell);
			layoutQueryElement(((IqlNode)element).getConstraint(), "constraint", parent, cell);
		} break;
		case PAYLOAD: {
			layoutQueryElements(((IqlPayload)element).getBindings(), "binding", parent, cell);
			layoutQueryElements(((IqlPayload)element).getLanes(), "lane", parent, cell);
			layoutQueryElement(((IqlPayload)element).getConstraint(), "constraint", parent, cell);
			layoutQueryElement(((IqlPayload)element).getFilter(), "filter", parent, cell);
		} break;
		case PREDICATE: {
			layoutQueryElement(((IqlPredicate)element).getExpression(), "expression", parent, cell);
		} break;
		case QUANTIFIER: {
			label = element.toString();
		} break;
		case QUERY: {
			layoutQueryElements(((IqlQuery)element).getImports(), "import", parent, cell);
			layoutQueryElements(((IqlQuery)element).getSetup(), "setup", parent, cell);
			layoutQueryElements(((IqlQuery)element).getEmbeddedData(), "data", parent, cell);
			layoutQueryElements(((IqlQuery)element).getStreams(), "stream", parent, cell);
		} break;
		case RESULT: {
			layoutQueryElements(((IqlResult)element).getResultInstructions(), "rule", parent, cell);
			layoutQueryElements(((IqlResult)element).getSortings(), "sort", parent, cell);
		} break;
		case RESULT_INSTRUCTION: {
			//TODO add handling once the class gets fleshed out
		} break;
		case SCOPE: {
			layoutQueryElements(((IqlScope)element).getLayers(), "layer", parent, cell);
		} break;
		case SEQUENCE: {
			layoutQueryElements(((IqlSequence)element).getElements(), "element", parent, cell);
		} break;
		case SORTING: {
			layoutQueryElement(((IqlSorting)element).getExpression(), "expression", parent, cell);
		} break;
		case STREAM: {
			layoutQueryElements(((IqlStream)element).getLayers(), "layer", parent, cell);
			layoutQueryElements(((IqlStream)element).getGrouping(), "group", parent, cell);
			layoutQueryElement(((IqlStream)element).getCorpus(), "corpus", parent, cell);
			layoutQueryElement(((IqlStream)element).getScope(), "scope", parent, cell);
			layoutQueryElement(((IqlStream)element).getPayload(), "payload", parent, cell);
			layoutQueryElement(((IqlStream)element).getResult(), "result", parent, cell);

		} break;
		case TERM: {
			IqlTerm term = (IqlTerm) element;
			layoutQueryElements(term.getItems(), term.getOperation()==BooleanOperation.CONJUNCTION ? "and" : "or", parent, cell);
		} break;
		case TREE_NODE: {
			layoutQueryElements(((IqlTreeNode)element).getQuantifiers(), "quant", parent, cell);
			layoutQueryElement(((IqlTreeNode)element).getMarker(), "marker", parent, cell);
			layoutQueryElement(((IqlTreeNode)element).getConstraint(), "constraint", parent, cell);
			layoutQueryElement(((IqlTreeNode)element).getChildren(), "children", parent, cell);
		} break;

		default:
			break;
		}

		if(label!=null) {
			Payload<?> value = (Payload<?>) cell.getValue();
			value.setLabel(label);
			graph.cellSizeUpdated(cell, true);
		}

		return cell;
	}

	private mxCell layoutQueryElement(Optional<? extends IqlQueryElement> element, String relation, Object parent, @Nullable mxCell owner) {
		if(element.isPresent()) {
			return layoutQueryElement(element.get(), relation, parent, owner);
		}
		return null;
	}

	private void layoutQueryElements(List<? extends IqlQueryElement> elements, String relation, Object parent, @Nullable mxCell owner) {
		elements.forEach(element -> layoutQueryElement(element, relation, parent, owner));
	}

	private void layoutQuery(@Nullable IqlQueryElement element) {
		Object parent = lanes.iqlLane();

		// clear parent
		graph.removeCells(mxGraphModel.getChildren(model, parent));
		iql2graph.clear();

		if(element==null) {
			return;
		}

		mxCell root = layoutQueryElement(element, null, parent, null);

		mxCompactTreeLayout layout = new mxCompactTreeLayout(graph, false);
		layout.setGroupPadding(20); // default is 10
		layout.setNodeDistance(15); // default is 20
		layout.setLevelDistance(10); // default is 10
		layout.execute(parent, root);
	}

	private void layoutTarget(Item[] items) {
		Object parent = lanes.targetLane();

		// clear parent
		graph.removeCells(mxGraphModel.getChildren(model, parent));
		targets.clear();
		dummyTarget = null;

		if(items.length==0) {
			return;
		}

		int x = 150;
		int y = 90;

		final int w = 35, h = 20, sep = 100;

		for (int i = 0; i < items.length; i++) {
			Item item = items[i];
			String label = item.toString();
			Object value = new Payload<>(item, label);
			mxCell cell = (mxCell) graph.insertVertex(parent, "t"+i, value, x, y, w, h, Styles.TARGET_BASIC);
			targets.add(cell);

			if(i>0) {
				mxCell prev = targets.get(i-1);
				String id = prev.getId()+"-"+cell.getId();
				graph.insertEdge(parent, id, null, prev, cell, null);
			}

			x += w + sep;
		}

		x += 20;
		dummyTarget = (mxCell) graph.insertVertex(parent, "t_dummy", null, x, y, 18, 18, Styles.TARGET_DUMMY);
	}

	Object[] linkResult(Result result) {
		int size = result.mapping.size();
		Object[] edges = new Object[size];
		Object parent = graph.getDefaultParent();
		for (int i = 0; i < size; i++) {
			Pair<Integer, Integer> m = result.mapping.get(i);
			NodeInfo info = node2info.get(m.first.intValue());
			mxCell source = sm2graph.get(info);
			mxCell target = targets.get(m.second.intValue());
			String id = source.getId()+"-"+target.getId();
			Object value = String.valueOf(result.id+1);
			edges[i] = graph.insertEdge(parent, id, value, source, target, Styles.LINK_RESULT);
		}
		return edges;
	}

	Object[] linkStepsFull(List<Step> steps) {
		List<Object> edges = new ArrayList<>();
		Object parent = graph.getDefaultParent();
		for (int i = 0; i < steps.size(); i++) {
			Step step = steps.get(i);
			NodeInfo info = id2info.get(step.nodeId);
			mxCell source = sm2graph.get(info);
			mxCell target;
			if(info.getType()==Type.FINISH || step.index>=targets.size()) {
				target = dummyTarget;
			} else {
				target = targets.get(step.index);
			}
			String id = source.getId()+"-"+target.getId();
			String value = String.valueOf(i+1);
			Object edge = model.getCell(id);
			if(edge!=null) {
				String oldValue = (String) model.getValue(edge);
				value = oldValue+", "+value;
				model.setValue(edge, value);
			} else {
				edge = graph.insertEdge(parent, id, value, source, target, Styles.LINK_STEP);
			}
			edges.add(edge);
		}
		return edges.toArray();
	}

	Object[] linkSteps(List<Step> steps, boolean onlyCurrentTrace) {
		List<Object> edges = new ArrayList<>();
		Object parent = graph.getDefaultParent();
		if(onlyCurrentTrace) {
			Set<Link> trace = new LinkedHashSet<>();
			for(Step step : steps) {
				Link link = new Link(step);
				if(step.enter) {
					trace.add(link);
				} else {
					trace.remove(link);
				}
			}
			steps = trace.stream().map(l -> l.step).collect(Collectors.toList());
		}
		for (int i = 0; i < steps.size(); i++) {
			Step step = steps.get(i);
			NodeInfo info = id2info.get(step.nodeId);
			mxCell source = sm2graph.get(info);
			mxCell target;
			if(info.getType()==Type.FINISH || step.index>=targets.size()) {
				target = dummyTarget;
			} else {
				target = targets.get(step.index);
			}
			String id = source.getId()+"-"+target.getId();
			String value = String.valueOf(i+1);
			Object edge = model.getCell(id);
			if(edge!=null) {
				String oldValue = (String) model.getValue(edge);
				value = oldValue+", "+value;
				model.setValue(edge, value);
			} else {
				edge = graph.insertEdge(parent, id, value, source, target, Styles.LINK_STEP);
			}
			edges.add(edge);
		}
		return edges.toArray();
	}

	static class Link {
		final Step step;
		Link(Step step) { this.step = step; }
		@Override
		public boolean equals(Object obj) {
			if(obj==this) {
				return true;
			} else if(obj instanceof Link) {
				Link other = (Link) obj;
				return step.nodeId==other.step.nodeId
						&& step.index==other.step.index;
			}
			return false;
		}
		@Override
		public int hashCode() { return (step.nodeId+1) * (step.index+1); }
	}

	void showFrame(String query, String target) {
		if(frame!=null) {
			return;
		}
		SwingUtilities.invokeLater(() -> {
			synchronized (InteractiveMatcher.this) {
				if(frame==null) {
					Control control = new Control();
					control.tfQuery.setText(query);
					control.tfTarget.setText(target);
					frame = new JFrame(TITLE);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.add(control);
					frame.setSize(1400, 850);
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				}
			}
		});
	}

	private static class Lanes {

		private final mxCell[] cells = new mxCell[3];

		private static final int MIN_HEIGHT = 150;

		void init(mxGraph graph, Object parent) {
			cells[0] = (mxCell) graph.insertVertex(parent, "lane_iql",
					"IQL Query", 0, 0, 1000, MIN_HEIGHT, Styles.LANE);
			cells[1] = (mxCell) graph.insertVertex(parent, "lane_sm",
					"State Machine", 0, MIN_HEIGHT, 1000, MIN_HEIGHT, Styles.LANE);
			cells[2] = (mxCell) graph.insertVertex(parent, "lane_ts",
					"Target Sequence", 0, MIN_HEIGHT*2, 1000, MIN_HEIGHT, Styles.LANE);
		}

		void sync(mxGraph graph) {
			mxRectangle[] bounds = new mxRectangle[cells.length];
			double maxWidth = 0.0;
			double y = 0.0;

			// Squeeze all lanes together
			for (int i = 0; i < cells.length; i++) {
				mxGeometry geo = cells[i].getGeometry();
				maxWidth = Math.max(maxWidth, geo.getWidth());
				double height = Math.max(MIN_HEIGHT, geo.getHeight());
				bounds[i] = new mxRectangle(0, y, 0, height);
				y += height;
			}

			// Now enforce same width
			for (int i = 0; i < bounds.length; i++) {
				bounds[i].setWidth(maxWidth);
			}

			graph.resizeCells(cells, bounds);
		}

		mxCell iqlLane() { return cells[0]; }
		mxCell smLane() { return cells[1]; }
		mxCell targetLane() { return cells[2]; }
	}

	private static class Payload<T> implements Serializable {
		private static final long serialVersionUID = 2639239524995613372L;

		final T data;
		String label;

		public Payload(T data, String label) {
			this.data = requireNonNull(data);
			this.label = requireNonNull(label);
		}

		void setLabel(String label) { this.label = requireNonNull(label); }

		@Override
		public String toString() { return label; }
	}

	private void addStep(Step step) {
		//TODO
	}

	private static class Step {
		final boolean enter;
		final int nodeId;
		@Deprecated
		final int pos;
		final boolean result;
		final int last;
		final int prev;
		final int index;
		final Snapshot snapshot;

		Step(boolean enter, Node node, State state, int pos, boolean result) {
			this.enter = enter;
			this.nodeId = node.id;
			this.pos = pos;
			this.index = state.frame.indices[pos];
			this.last = state.last;
			this.prev = state.frame.previousIndex;
			this.result = result;
			this.snapshot = state.snapshot();
		}
	}

	private static class Result {
		final long id;
		final List<Pair<Integer, Integer>> mapping;
		Result(State state) {
			id = state.reported;
			int count = state.entry;
			mapping = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				mapping.add(Pair.pair(state.m_node[i], state.m_index[i]));
			}

			Collections.sort(mapping, (m1, m2) -> {
				int r = m1.first.compareTo(m2.first);
				if(r==0) {
					r = m1.second.compareTo(m2.second);
				}
				return r;
			});

			System.out.println(getClass() + " " + toString());
		}

		void appendTo(StringBuilder buffer) {
			int count = mapping.size();
			buffer.append(id+1).append(": [").append(count).append("] ");

			for (int i = 0; i < count; i++) {
				if(i>0) {
					buffer.append(", ");
				}
				Pair<Integer, Integer> m = mapping.get(i);
				buffer.append(m.first).append("->").append(m.second);
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			appendTo(sb);
			sb.append(']');
			return sb.toString();
		}
	}

	private static class MonitorDelegate implements Monitor {

		private final Consumer<Step> sink;

		public MonitorDelegate(Consumer<Step> sink) { this.sink = requireNonNull(sink); }


		@Override
		public void enterNode(Node node, State state, int pos) {
			sink.accept(new Step(true, node, state, pos, false));
		}

		@Override
		public void exitNode(Node node, State state, int pos, boolean result) {
//			if(!result) {
//				System.out.println("xx");
//			}
			sink.accept(new Step(false, node, state, pos, result));
		}

	}

	class Control extends JPanel {
		private static final long serialVersionUID = 4417635081664995598L;

		private final JTextField tfQuery;
		private final JTextField tfTarget;
		private final JTextField tfTree;
		private final JCheckBox cbExpand, cbPromote;
		private final JButton bParse, bMakeSM, bMatch;
		private final JList<Step> lSteps;
		private final JList<Result> lResults;
		private final JButton bNextStep, bPrevStep;
		private final JLabel lStep;

		private final StateModel stateModel;
		private final JTable tState;

		private transient IqlPayload payload;
		private transient StructurePattern pattern;
		private transient Container target;
		private transient int[] tree;
		private final Vector<Step> steps = new Vector<>();

		/** Temporary edges for state machine <-> target */
		private Object[] tempEdges;

		Control() {
			tfQuery = makeTextField(100);
			tfTarget = makeTextField(100);
			tfTree = makeTextField(100);

			cbExpand = new JCheckBox("Expand", true);
			cbExpand.setToolTipText("Expand $X into $.toString()==\"X\" inside nodes");

			cbPromote = new JCheckBox("Promote", true);
			cbPromote.setToolTipText("Promote empty nodes so that they force mapping entries in the result");

			bParse = new JButton("Parse");
			bParse.addActionListener(this::onParse);

			bMakeSM = new JButton("Build");
			bMakeSM.addActionListener(this::onMakeSM);

			bMatch = new JButton("Match");
			bMatch.addActionListener(this::onMatch);

			lSteps = new JList<>();
			lSteps.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			lSteps.setCellRenderer(new StepRenderer());
			lSteps.addListSelectionListener(this::onStepSelected);

			lResults = new JList<>();
			lResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			lResults.setCellRenderer(new ResultRenderer());
			lResults.addListSelectionListener(this::onResultSelected);

			lStep = new JLabel("?");

			bNextStep = new JButton("->");
			bNextStep.addActionListener(this::onNextStep);

			bPrevStep = new JButton("<-");
			bPrevStep.addActionListener(this::onPrevStep);

			tState = new JTable();
			tState.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			stateModel = new StateModel();

			final TreeTableCellRenderer cellRenderer = new TreeTableCellRenderer(tState, stateModel);
			cellRenderer.setRootVisible(false);
			cellRenderer.setShowsRootHandles(true);

			final TreeTableModel treeTableModel = new TreeTableModel(cellRenderer);
			tState.setModel(treeTableModel);

			TableColumnModel columnModel = tState.getColumnModel();
			TableColumn column = columnModel.getColumn(0);
			column.setCellRenderer(cellRenderer);
			column.setPreferredWidth(75);
			column.setResizable(true);
			column = columnModel.getColumn(1);
			column.setCellRenderer(new StateValueRenderer());
			column.setPreferredWidth(150);
			column.setResizable(true);

			tState.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount()!=2) {
						return;
					}
					int row = tState.rowAtPoint(e.getPoint());
					if(row==-1) {
						return;
					}
					if(cellRenderer.isCollapsed(row)) {
						cellRenderer.expandRow(row);
					} else if(cellRenderer.isExpanded(row)) {
						cellRenderer.collapseRow(row);
					}
				}
			});

			JSplitPane splitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
					new JScrollPane(lSteps), new JScrollPane(lResults));
			splitPaneV.setOneTouchExpandable(true);
			splitPaneV.setDividerLocation(400);

			setLayout(new BorderLayout());

			JPanel topInput = FormBuilder.create()
					.padding(Paddings.DLU4)
					.columns("pref, 5dlu, pref, 6dlu, pref")
					.rows("pref, $lg, pref, $lg, pref")
					.addLabel("Query:").rc(1, 1).add(tfQuery).rc(1, 3)
					.addLabel("Target:").rc(3, 1).add(tfTarget).rc(3, 3)
					.addLabel("Tree:").rc(5, 1).add(tfTree).rc(5, 3)

					.add(Forms.horizontal("4dlu", cbExpand, cbPromote)).rc(1, 5)
					.build();
			add(topInput, BorderLayout.NORTH);

			JPanel rightControl = FormBuilder.create()
					.padding("10dlu, 4dlu, 1dlu, 4dlu")
					.columns("right:pref:grow, 3dlu, center:pref, 3dlu, left:pref:grow")
					.rows("pref, 3dlu, pref, 5dlu, fill:pref:grow")
					.add(bParse).rc(1, 1)
					.add(bMakeSM).rc(1, 3)
					.add(bMatch).rc(1, 5)
					.add(bPrevStep).rc(3, 1)
					.add(lStep).rc(3, 3)
					.add(bNextStep).rc(3, 5)
					.add(splitPaneV).rcw(5, 1, 5)
					.build();

			JPanel stateOutline = FormBuilder.create()
					.padding("10dlu, 4dlu, 1dlu, 4dlu")
					.columns("fill:pref:grow")
					.rows("fill:pref:grow")
					.addScrolled(tState).rc(1, 1)
					.build();

			JSplitPane splitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
					rightControl, stateOutline);
			splitPaneH.setOneTouchExpandable(true);
			splitPaneH.setDividerLocation(300);

			add(splitPaneH, BorderLayout.EAST);

			add(graphComponent, BorderLayout.CENTER);

			refreshButtons();
		}

		private JTextField makeTextField(int columns) {
			JTextField tf = new JTextField(columns);
		    final Font currFont = tf.getFont();
		    tf.setFont(new Font("Courier New", currFont.getStyle(), currFont.getSize()));
		    return tf;
		}

		private Item item(int index, char c) {
			return new DefaultItem() {
				@Override
				public long getIndex() { return index; }
				@Override
				public String toString() { return String.valueOf(c); }
			};
		}

		private void clearTempEdges() {
			if(tempEdges!=null) {
				graph.removeCells(tempEdges);
				tempEdges = null;
			}
		}

		private void onParse(ActionEvent e) {

			String encodedTree = tfTree.getText();

			if(encodedTree.isEmpty()) {
				tree = null;
			} else {
				tree = EvaluationUtils.parseTree(encodedTree, encodedTree.contains(" "));
			}

			String input = tfTarget.getText();
			if(input.isEmpty()) {
				target = null;
			} else {
				Item[] items = IntStream.range(0, input.length())
						.mapToObj(i -> item(i, input.charAt(i)))
						.toArray(Item[]::new);
				target = mockContainer(items);

				displayTarget(items);
			}

			String rawPayload = tfQuery.getText();
			if(rawPayload.isEmpty()) {
				payload = null;
			} else {
				if(cbExpand.isSelected()) {
					rawPayload = QueryTestUtils.expand(rawPayload);
				}

				payload = new QueryProcessor().processPayload(rawPayload);

				displayQuery(payload);
			}

			refreshButtons();
		}

		private void onMakeSM(ActionEvent e) {
			if(payload==null) {
				return;
			}

			pattern = null;

			IqlLane lane = payload.getLanes().get(0);

			StructurePattern.Builder builder = StructurePattern.builder();
			builder.source(lane);
			builder.id(1);
			builder.role(Role.SINGLETON);
			RootContext rootContext = EvaluationContext.rootBuilder(QueryTestUtils.dummyCorpus())
					.addEnvironment(SharedUtilityEnvironments.all())
					.build();
			LaneContext context = rootContext.derive()
					.lane(QueryTestUtils.lane())
					.build();
			builder.context(context);
			builder.allowMonitor(true);

			if(cbPromote.isSelected()) {
				builder.nodeTransform(StructurePatternTest.PROMOTE_NODE);
			}

			if(target!=null) {
				builder.initialBufferSize(strictToInt(target.getItemCount() + 1));
			}

			pattern = builder.build();

			displayStateMachine(pattern);
			refreshButtons();
		}

		private void onMatch(ActionEvent e) {
			if(pattern==null || target==null) {
				return;
			}

			steps.clear();

			new SwingWorker<Boolean, Step>() {

				final Monitor monitor = new MonitorDelegate(steps::add);
				final Vector<Result> results = new Vector<>();

				@Override
				protected Boolean doInBackground() throws Exception {
					StructureMatcher matcher = pattern.matcherBuilder()
							.threadVerifier(ThreadVerifier.forCurrentThread("interactive-matcher"))
							.build();

					if(tree!=null) {
						StructurePatternTest.applyTree(matcher, tree);
					}
					matcher.monitor(monitor);
					matcher.resultConsumer(state -> results.add(new Result(state)));
					return _boolean(matcher.matches(0, target));
				}

				@Override
				protected void done() {
					lSteps.setListData(steps);
					lResults.setListData(results);
					refreshButtons();
				}
			}.execute();

			refreshButtons();
		}

		private void onNextStep(ActionEvent e) {
			int idx = lSteps.getSelectedIndex();
			int max = lSteps.getModel().getSize();
			if(idx==-1) {
				idx = 0;
			} else {
				idx++;
			}
			idx = Math.max(idx, max-1);
			lSteps.setSelectedIndex(idx);
		}

		private void onPrevStep(ActionEvent e) {
			int idx = lSteps.getSelectedIndex();
			idx--;
			idx = Math.max(idx, -1);
			lSteps.setSelectedIndex(idx);
		}

		private void onStepSelected(ListSelectionEvent e) {
			model.beginUpdate();
			clearTempEdges();
			if(!e.getValueIsAdjusting()) {
				int idx = lSteps.getSelectedIndex();
				if(idx!=-1) {
					stateModel.setData(steps.get(idx).snapshot);
					tempEdges = linkSteps(steps.subList(0, idx+1), true);
					lStep.setText(String.valueOf(idx+1));
				} else {
					lStep.setText("?");
				}
			}
			model.endUpdate();
		}

		private void onResultSelected(ListSelectionEvent e) {
			model.beginUpdate();
			clearTempEdges();
			if(!e.getValueIsAdjusting()) {
				Result result = lResults.getSelectedValue();
				if(result!=null) {
					tempEdges = linkResult(result);
				}
			}
			model.endUpdate();
		}

		private void refreshButtons() {
			bMakeSM.setEnabled(payload!=null);
			bMatch.setEnabled(pattern!=null && target!=null);

			int stepCount = lSteps.getModel().getSize();
			int step = lSteps.getSelectedIndex();
			bNextStep.setEnabled(stepCount > 0 && step < stepCount-1);
			bPrevStep.setEnabled(stepCount > 0 && step > 0);
		}
	}

	private static class StateField implements Comparable<StateField> {
		final String name;
		final Object value;

		public StateField(String name, Object value) {
			this.name = requireNonNull(name);
			this.value = value;
		}

		@Override
		public int compareTo(StateField o) { return name.compareTo(o.name); }

		@Override
		public String toString() { return name + " - " + String.valueOf(value); }
	}

	private static class StateModel implements TreeModel {
		private final List<TreeModelListener> listeners = new ObjectArrayList<>();
		private Snapshot snapshot;
		private final Object root = new Object();
		private final List<StateField> fields = new ObjectArrayList<>();

		void setData(Snapshot snapshot) {
			if(this.snapshot==snapshot) {
				return;
			}

			this.snapshot = snapshot;
			fields.clear();
			if(snapshot!=null) {
				for(java.lang.reflect.Field field : snapshot.getClass().getFields()) {
					try {
						fields.add(new StateField(field.getName(), field.get(snapshot)));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
						// not supposed to happen anyway
					}
				}
				Collections.sort(fields);
			}

			final TreeModelEvent e = new TreeModelEvent(this, new Object[] {root});
			listeners.forEach(l -> l.treeStructureChanged(e));
		}

		@Override
		public Object getRoot() { return root; }

		@Override
		public Object getChild(Object parent, int index) {
			if(parent==root) {
				return fields.get(index);
			}

			if(parent instanceof StateField) {
				parent = ((StateField)parent).value;
			}

			if(parent instanceof Pair) {
				parent = ((Pair<?,?>)parent).second;
			} else if(parent.getClass().isArray()) {
				return Pair.pair(_int(index), Array.get(parent, index));
			}

			throw new NoSuchElementException();
		}

		@Override
		public int getChildCount(Object parent) {
			if(parent==root) {
				return fields.size();
			}

			if(parent instanceof StateField) {
				parent = ((StateField)parent).value;
			} else if(parent instanceof Pair) {
				parent = ((Pair<?,?>)parent).second;
			}

			if(parent.getClass().isArray()) {
				return Array.getLength(parent);
			}

			return 0;
		}

		@Override
		public boolean isLeaf(Object node) {
			return getChildCount(node)==0;
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public int getIndexOfChild(Object parent, Object child) {
			if(parent==root) {
				return fields.indexOf(child);
			}

			if(parent instanceof Pair) {
				return ((Pair<Integer,?>)parent).first.intValue();
			}

			if(parent instanceof StateField) {
				parent = ((StateField)parent).value;
			}

			if(parent.getClass().isArray()) {
				//TODO do we even need this method?
			}

			return -1;
		}

		@Override
		public void addTreeModelListener(TreeModelListener l) { listeners.add(l); }

		@Override
		public void removeTreeModelListener(TreeModelListener l) { listeners.remove(l); }
	}

	@SuppressWarnings("serial")
	private static class TreeTableModel extends AbstractTableModel
			implements TreeExpansionListener, TreeModelListener {

		private final JTree tree;

		TreeTableModel(JTree tree) {
			this.tree = requireNonNull(tree);
			tree.addTreeExpansionListener(this);
			tree.getModel().addTreeModelListener(this);
		}

		@Override
		public int getRowCount() { return tree.getRowCount(); }

		@Override
		public int getColumnCount() { return 2; }

		@Override
		public String getColumnName(int column) { return column==0 ? "Field" : "Value"; }

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			TreePath path = tree.getPathForRow(rowIndex);
			if(path==null) {
				return null;
			}
			Object value = path.getLastPathComponent();

			// TODO Auto-generated method stub
			return value;
		}

		private void refreshTree() {
			tree.expandPath(new TreePath(tree.getModel().getRoot()));
			fireTableDataChanged();
		}

		@Override
		public void treeNodesChanged(TreeModelEvent e) { refreshTree(); }

		@Override
		public void treeNodesInserted(TreeModelEvent e) { refreshTree(); }

		@Override
		public void treeNodesRemoved(TreeModelEvent e) { refreshTree(); }

		@Override
		public void treeStructureChanged(TreeModelEvent e) { refreshTree(); }

		@Override
		public void treeExpanded(TreeExpansionEvent event) { refreshTree(); }

		@Override
		public void treeCollapsed(TreeExpansionEvent event) { refreshTree(); }
	}

	@SuppressWarnings("serial")
	private static class StateValueRenderer extends DefaultTableCellRenderer {

		private static final Map<Class<?>, Function<Object, String>> renderers = new HashMap<>();
		static {
			renderers.put(int[].class, obj -> Arrays.toString((int[])obj));
			renderers.put(long[].class, obj -> Arrays.toString((long[])obj));
			renderers.put(float[].class, obj -> Arrays.toString((float[])obj));
			renderers.put(double[].class, obj -> Arrays.toString((double[])obj));
			renderers.put(boolean[].class, obj -> Arrays.toString((boolean[])obj));
			renderers.put(short[].class, obj -> Arrays.toString((short[])obj));
			renderers.put(char[].class, obj -> Arrays.toString((char[])obj));
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if(value instanceof StateField) {
				value = ((StateField)value).value;
			} else if(value instanceof Pair) {
				value = ((Pair<?,?>)value).second;
			}

			if(value!=null) {
				Function<Object, String> renderer = renderers.get(value.getClass());
				if(renderer!=null) {
					value = renderer.apply(value);
				} else if(value.getClass().isArray()) {
					// should only be object arrays now
					value = Arrays.toString((Object[])value);
				}
			}

			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			return this;
		}
	}

	@SuppressWarnings("serial")
	private static class TreeTableCellRenderer extends JTree
		implements TableCellRenderer, ListSelectionListener {

		/** last rendered row */
		protected int visibleRow;

		private JTable table;

		public TreeTableCellRenderer(JTable table, TreeModel model) {
			super(model);
			this.table = table;

			setRowHeight(getRowHeight());
			setEditable(false);
			TreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
			selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			setSelectionModel(selectionModel);
			setExpandsSelectedPaths(true);

			table.getSelectionModel().addListSelectionListener(this);

			setCellRenderer(new DefaultTreeCellRenderer() {
				@SuppressWarnings("unchecked")
				@Override
				public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
						boolean leaf, int row, boolean hasFocus) {
					if(value instanceof StateField) {
						value = ((StateField)value).name;
					} else if(value instanceof Pair) {
						value = ((Pair<Integer,?>)value).first;
					}

					return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				}
			});
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			setSelectionRow(e.getFirstIndex());
		}

		@Override
		public void setRowHeight(int rowHeight) {
			if (rowHeight > 0) {
				super.setRowHeight(rowHeight);
				if (table != null && table.getRowHeight() != rowHeight) {
					table.setRowHeight(getRowHeight());
				}
			}
		}

		@Override
		public void setBounds(int x, int y, int w, int h) {
			super.setBounds(x, 0, w, table.getHeight());
		}

		@Override
		public void paint(Graphics g) {
			g.translate(0, -visibleRow * getRowHeight());

			super.paint(g);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected)
				setBackground(table.getSelectionBackground());
			else
				setBackground(table.getBackground());

			visibleRow = row;
			return this;
		}
	}

	private class StepRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 7466797464454660084L;

		private final StepIcon icon = new StepIcon();

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if(value instanceof Step) {
				Step step = (Step) value;
				icon.setup(step.enter, step.result);
				NodeInfo info = id2info.get(step.nodeId);
				String text = String.format("%2d: %s[%d] at %d -> %d", _int(index+1),
						info.getClassLabel(), _int(info.getId()), _int(step.pos), _int(step.index));
				if(step.enter) {
					text += String.format(" (last=%d)",  _int(step.last));
					text += String.format(" (prev=%d)",  _int(step.prev));
				}
				value = text;
			}

			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			setIcon(icon);

			return this;
		}
	}

	private static class ResultRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 4296826121562843768L;
		private StringBuilder buffer = new StringBuilder(100);
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if(value instanceof Result) {
				Result result = (Result) value;
				buffer.setLength(0);
				result.appendTo(buffer);
				value = buffer.toString();
			}

			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			return this;
		}
	}

	private static class StepIcon implements Icon {

		private boolean enter;
		private boolean success;

		private static final int W = 17;
		private static final int H = 17;

		void setup(boolean enter, boolean success) {
			this.enter = enter;
			this.success = success;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int[] px = new int[3];
			int[] py = new int[3];
			Color color;

			if(enter) {
				px[0] =  0; px[1] = 8; px[2] =  0;
				py[0] =  0; py[1] = 8; py[2] = 16;
				color = Color.BLUE;
			} else {
				px[0] = 16; px[1] = 8; px[2] = 16;
				py[0] =  0; py[1] = 8; py[2] = 16;
				color = success ? Color.GREEN : Color.RED;
			}

			g2d.setColor(color);
			g2d.fillPolygon(px, py, 3);
		}

		@Override
		public int getIconWidth() { return W; }

		@Override
		public int getIconHeight() { return H; }
	}

	static class StyleBuilder {

		public static StyleBuilder forStylesheet(mxStylesheet stylesheet) {
			return new StyleBuilder(stylesheet);
		}

		private final mxStylesheet stylesheet;

		private Map<String, Object> style;
		private String name;
		private String key;

		private StyleBuilder(mxStylesheet stylesheet) {
			this.stylesheet = requireNonNull(stylesheet);
		}

		private void checkNoEntry() {
			checkState("Uncommited entry with key: "+key, key==null);
		}

		private void checkNoStyle() {
			checkState("Uncommited style", name==null && style==null);
		}

		private void checkEntry() {
			checkState("No entry context", key!=null);
		}

		private void checkStyle() {
			checkState("No style context", style!=null);
		}

		private void checkCommit() {
			checkNoEntry();

			checkStyle();

			checkState("No style name for commit available", name!=null);
		}

		private void set(String name, Map<String, Object> style) {
			this.name = name;
			this.style = style;
			this.key = null;
		}

		private void set(String key) {
			this.key = key;
		}

		private void clear() {
			this.name = null;
			this.style = null;
			this.key = null;
		}

		public StyleBuilder newStyle(String name) {
			requireNonNull(name);

			if(stylesheet.getStyles().containsKey(name))
				throw new IllegalArgumentException("Style already exists: "+name);

			checkNoStyle();
			checkNoEntry();

			set(name, new HashMap<>());

			return this;
		}

		public StyleBuilder newStyle(String name, String baseStyle) {
			requireNonNull(name);
			requireNonNull(baseStyle);

			if(stylesheet.getStyles().containsKey(name))
				throw new IllegalArgumentException("Style already exists: "+name);

			checkNoStyle();
			checkNoEntry();

			set(name, stylesheet.getCellStyle(baseStyle, new HashMap<>()));

			return this;
		}

		public StyleBuilder newStyle(String name, Map<String, Object> baseStyle) {
			requireNonNull(name);
			requireNonNull(baseStyle);

			if(stylesheet.getStyles().containsKey(name))
				throw new IllegalArgumentException("Style already exists: "+name);

			checkNoStyle();
			checkNoEntry();

			set(name, new HashMap<>(baseStyle));

			return this;
		}

		public StyleBuilder newEntry(String key, Object value) {
			requireNonNull(key);
			requireNonNull(value);

			checkNoEntry();
			checkStyle();

			style.put(key, value);

			return this;
		}

		public StyleBuilder key(String key) {
			requireNonNull(key);

			checkNoEntry();
			checkStyle();

			set(key);

			return this;
		}

		public StyleBuilder value(Object value) {
			requireNonNull(value);

			checkEntry();
			checkStyle();

			style.put(key, value);
			set(null);

			return this;
		}

		public StyleBuilder modifyStyle(String name) {
			requireNonNull(name);

			checkNoEntry();
			checkNoStyle();

			Map<String, Object> style = stylesheet.getStyles().get(key);

			if(style==null)
				throw new IllegalArgumentException("No such style: "+name);

			this.style = style;

			return this;
		}

		public StyleBuilder modifyDefaultVertexStyle() {
			checkNoEntry();
			checkNoStyle();

			this.style = stylesheet.getDefaultVertexStyle();

			return this;
		}

		public StyleBuilder modifyDefaultEdgeStyle() {
			checkNoEntry();
			checkNoStyle();

			this.style = stylesheet.getDefaultEdgeStyle();

			return this;
		}

		public void commit() {
			checkCommit();

			stylesheet.putCellStyle(name, style);

			clear();
		}

		public void done() {
			clear();
		}
	}
}
