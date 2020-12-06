/**
 *
 */
package de.ims.icarus2.query.api.engine.matcher;

import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.lang.Primitives._boolean;
import static de.ims.icarus2.util.lang.Primitives._float;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.FormBuilder;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Monitor;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Node;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.NodeInfo;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.NodeInfo.Field;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.NodeInfo.Type;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.State;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class InteractiveMatcher extends JPanel {

	public static void main(String[] args) {

		String payload = SequencePatternTest.expand(
				"{{[isAt(2) || isFirst, $X][]}[isNotAt(3) || isLast,$X]}");
		SequencePattern pattern = SequencePatternTest.builder(payload)
				.allowMonitor(true)
				.build();

		SwingUtilities.invokeLater(() -> {
			InteractiveMatcher im = new InteractiveMatcher();
			im.reset(pattern);

			im.display();
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

	private static final long serialVersionUID = 4007774984974640067L;

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

	private final Map<NodeInfo, mxCell> sm2graph = new Reference2ObjectOpenHashMap<>();

	InteractiveMatcher() {
		stylesheet = createStylesheet();
		mxCell root = createRoot();
		model = createModel(root);
		graph = createGraph(model, stylesheet);
		lanes = null/*createLanes(graph, root)*/; //TODO
		graphComponent = createGraphComponent(graph);

		initGraphStructure();

		setLayout(new BorderLayout());
		add(graphComponent, BorderLayout.CENTER);
	}

	private interface Styles {
		static final String VERTEX = "defaultVertex";

		static final String LANE = "lane";

		static final String SM_BASIC = "sm_basic";
		static final String SM_TERMINAL = "sm_terminal";
		static final String SM_BRANCH_START = "sm_branch_start";
		static final String SM_BRANCH_END = "sm_branch_end";
		static final String SM_NODE = "sm_node";

		static final String IQL_BASIC = "iql_basic";
	}

	private mxStylesheet createStylesheet() {
		mxStylesheet stylesheet = new mxStylesheet();

		StyleBuilder builder = StyleBuilder.forStylesheet(stylesheet);

		builder.modifyDefaultEdgeStyle()
			.newEntry(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_SIDETOSIDE)
			.done();

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

		builder.newStyle(Styles.SM_BASIC)
			.newEntry(mxConstants.STYLE_FILLCOLOR, mxConstants.NONE)
			.newEntry(mxConstants.STYLE_STROKECOLOR, "black")
			.newEntry(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE)
			.newEntry(mxConstants.STYLE_STROKEWIDTH, _float(1.5F))
			.newEntry(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_CENTER)
			.newEntry(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_CENTER)
			.newEntry(mxConstants.STYLE_FOLDABLE, "0")
			.newEntry(mxConstants.STYLE_AUTOSIZE, "1")
			.commit();

		builder.newStyle(Styles.SM_NODE, Styles.SM_BASIC)
			.newEntry(mxConstants.STYLE_FILLCOLOR, "#FFFF7F")
			.newEntry(mxConstants.STYLE_STROKECOLOR, "#FF5500")
			.newEntry(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE)
			.newEntry(mxConstants.STYLE_STROKEWIDTH, _float(1.0F))
			.newEntry(mxConstants.STYLE_SPACING, _int(3))
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

	private Lanes createLanes(mxGraph graph, mxCell parent) {
		return new Lanes(graph, parent);
	}

	private mxGraph createGraph(mxGraphModel model, mxStylesheet stylesheet) {
		mxGraph graph = new mxGraph(model, stylesheet)/*{
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
		}*/; //TODO

		graph.setAutoSizeCells(true);
		graph.setBorder(30);
		graph.setHtmlLabels(false);
//		graph.setGridSize(10);
//		graph.setAllowDanglingEdges(false);
		graph.setAutoOrigin(true);
//		graph.setCellsCloneable(false);
		graph.setCellsEditable(false);
		graph.setCellsMovable(false);
		graph.setCellsResizable(false);
		graph.setCellsSelectable(true);
//		graph.setCellsDisconnectable(false);
//		graph.setConnectableEdges(false);
		graph.setDropEnabled(false);
//		graph.setEdgeLabelsMovable(false);
//		graph.setGridEnabled(true);
//		graph.setKeepEdgesInBackground(true);
		graph.setLabelsVisible(true);
//		graph.setLabelsClipped(false);
//		graph.setSwimlaneNesting(true);
		graph.setExtendParents(true);
		graph.setExtendParentsOnAdd(true);
		graph.setDefaultOverlap(0);

		//TODO setup
		return graph;
	}

	private mxGraphComponent createGraphComponent(mxGraph graph) {
		mxGraphComponent component = new mxGraphComponent(graph);
		component.setAutoExtend(true);
		component.setAntiAlias(true);
		component.setTextAntiAlias(true);
//		component.setToolTips(true);
//		component.setImportEnabled(false);
		component.setFoldingEnabled(true);
//		component.setConnectable(true);
//		component.setDragEnabled(false);
//		component.setKeepSelectionVisibleOnZoom(true);

//		component.getViewport().setBackground(Color.white);
		//TODO setup
		return component;
	}

	private void initGraphStructure() {
		Object root = model.getRoot();
	}

	private String createLabel(Object cell) {
		Object value = model.getValue(cell);
		if(value instanceof Payload) {
			return ((Payload)value).label;
		}
		return null;
	}

	private String createTooltip(Object cell) {
		return null;
	}

	void reset(SequencePattern pattern) {
		model.beginUpdate();
		try {
			layoutStateMachine(pattern.info());
		} finally {
			model.endUpdate();
		}
	}

	private mxCell lookup(NodeInfo node) {
		mxCell cell = sm2graph.get(node);
		if(cell==null)
			throw new IllegalArgumentException("Unknown node: "+node);
		return cell;
	}

	private void resize(mxCell cell, int width, int height) {
		mxGeometry geo = cell.getGeometry();
		geo.setWidth(width);
		geo.setHeight(height);
	}

	private mxCell makeSMNode(NodeInfo info, Object parent) {

		String style = Styles.SM_BASIC;
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
			label = "Node "+info.getProperty(Field.NODE);
		} break;

		default:
			createDefaultLabel = true;
			break;
		}

		if(label.isEmpty() && createDefaultLabel) {
			label = info.getClassLabel();
		}

		mxCell cell = (mxCell) graph.insertVertex(parent, "sm"+info.getId(),
				new Payload(info, label), 0, 0, w, h, style);

		if(!label.isEmpty()) {
			graph.cellSizeUpdated(cell, true);
		}
		return cell;
	}

	private mxCell makeSMEdge(mxCell source, mxCell target, Object parent, LinkType linkType) {
		String style = null;
		//TODO customize edge style based on link type

		mxCell edge = (mxCell) graph.insertEdge(parent, source.getId()+"-"+target.getId(),
				null, source, target, style);

		return edge;
	}

	private mxCell makeSMEdge(NodeInfo source, @Nullable NodeInfo target, Object parent, LinkType linkType) {
		requireNonNull(source);
		if(target==null) {
			return null;
		}
		return makeSMEdge(lookup(source), lookup(target), parent, linkType);
	}

	private void layoutStateMachine(NodeInfo[] nodes) {
		Object parent = graph.getDefaultParent();

		// clear parent
		graph.removeCells(mxGraphModel.getChildren(model, parent));

		Int2ObjectMap<NodeInfo> infoLut = new Int2ObjectOpenHashMap<>();

		mxCell root = null;

		// First pass: make nodes
		for(NodeInfo info : nodes) {
			mxCell cell = makeSMNode(info, parent);
			sm2graph.put(info, cell);
			infoLut.put(info.getId(), info);
			if(info.getType()==Type.BEGIN) {
				root = cell;
			}
		}

		// Second pass: now do the linking
		for(NodeInfo info : nodes) {
			makeSMEdge(info, infoLut.get(info.getLogicalNext()), parent, LinkType.STANDARD);

			LinkType atomLink = info.getType()==Type.BRANCH ? LinkType.BRANCH : LinkType.ATOM;
			IntList atoms = info.getAtoms();
			for (int i = 0; i < atoms.size(); i++) {
				makeSMEdge(info, infoLut.get(atoms.getInt(i)), parent, atomLink);
			}
		}

		mxHierarchicalLayout layout = new mxHierarchicalLayout(graph, SwingConstants.WEST);
		layout.setResizeParent(true);
		layout.setInterRankCellSpacing(25); // default is 50
		layout.setIntraCellSpacing(15); // default is 30
		layout.setInterHierarchySpacing(20); // default is 60
		layout.execute(parent, Arrays.asList(root));

//		mxCompactTreeLayout layout = new mxCompactTreeLayout(graph, true);
//		layout.execute(proxy, root);
	}

	private enum LinkType {
		STANDARD,
		ATOM,
		BRANCH,
		;
	}

	void display() {
		if(frame!=null) {
			return;
		}
		SwingUtilities.invokeLater(() -> {
			synchronized (InteractiveMatcher.this) {
				if(frame==null) {
					frame = new JFrame(TITLE);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.add(this);
					frame.setSize(1400, 700);
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				}
			}
		});
	}

	private static class Lanes {
		final mxCell iqlLane;
		final mxCell smLane;
		final mxCell targetLane;

		Lanes(mxGraph graph, mxCell parent) {
			iqlLane = (mxCell) graph.insertVertex(parent, "lane_iql",
					"IQL Query", 0, 0, 1000, 200, Styles.LANE);
			smLane = (mxCell) graph.insertVertex(parent, "lane_sm",
					"State Machine", 0, 200, 1000, 200, Styles.LANE);
			targetLane = (mxCell) graph.insertVertex(parent, "lane_ts",
					"Target Sequence", 0, 400, 1000, 200, Styles.LANE);
		}

		private mxCell makeLane(String id, String title) {
			mxCell lane = new mxCell(title, new mxGeometry(), Styles.LANE);
			lane.setVertex(true);
			lane.setConnectable(false);
			lane.setId(id);
			return lane;
		}
	}

	private static class Payload implements Serializable {
		private static final long serialVersionUID = 2639239524995613372L;

		final NodeInfo info;
		final String label;

		public Payload(NodeInfo info, String label) {
			this.info = requireNonNull(info);
			this.label = requireNonNull(label);
		}

		@Override
		public String toString() { return label; }
	}

	private void addStep(Step step) {
		//TODO
	}

	private static class Step {
		final boolean enter;
		final Node node;
		final int pos;
		final boolean result;

		Step(boolean enter, Node node, int pos, boolean result) {
			this.enter = enter;
			this.node = requireNonNull(node);
			this.pos = pos;
			this.result = result;
		}
	}

	private class MonitorDelegate implements Monitor {

		@Override
		public void enterNode(Node node, State state, int pos) {
			addStep(new Step(true, node, pos, false));
		}

		@Override
		public void exitNode(Node node, State state, int pos, boolean result) {
			addStep(new Step(false, node, pos, result));
		}

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

	@Deprecated
	private class CameraControl extends JPanel {

		private final JButton up, down, left, right, center;

		CameraControl() {

			up = makeNavButton(Direction.TOP);
			down = makeNavButton(Direction.BOTTOM);
			left = makeNavButton(Direction.LEFT);
			right = makeNavButton(Direction.RIGHT);
			center = makeNavButton(Direction.CENTER);

			FormBuilder builder = FormBuilder.create()
					.panel(this)
					.columns("right:pref:grow, 4dlu, pref, 4dlu, left:pref:grow")
					.rows("pref, 4dlu, pref, 4dlu, top:pref:grow")
					.padding("4dlu, 4dlu, 4dlu, 4dlu")
					.add(up).rc(1, 3);
			//TODO
		}

		private JButton makeNavButton(Direction dir) {
			JButton b = new JButton(new DirectionIcon(dir));
			b.addActionListener(this::onNavAction);
			b.putClientProperty("direction", dir);
			return b;
		}

		private void onNavAction(ActionEvent ae) {

		}

		private void onZoomAction(ActionEvent ae) {

		}
	}

	@Deprecated
	private enum Direction {
		TOP(true, false),
		BOTTOM(true, false),
		LEFT(false, true),
		RIGHT(false, true),
		CENTER(false, false),
		;

		final boolean vertical, horizontal;

		private Direction(boolean vertical, boolean horizontal) {
			this.vertical = vertical;
			this.horizontal = horizontal;
		}
	}

	@Deprecated
	private enum Zoom {
		ZOOM_IN,
		ZOOM_OUT,
		RESET,
		;
	}

	@Deprecated
	private static class DirectionIcon implements Icon {

		private final Direction dir;

		private static final int W = 21;
		private static final int H = 11;

		DirectionIcon(Direction dir) { this.dir = requireNonNull(dir); }

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2d = (Graphics2D)g;
			g2d.setColor(c.getForeground());
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int[] px = new int[3];
			int[] py = new int[3];

			switch (dir) {
			case TOP:
				px[0] =  0; px[1] = 10; px[2] = 20;
				py[0] = 10; py[1] =  0; py[2] = 10;
				break;
			case BOTTOM:
				px[0] =  0; px[1] = 10; px[2] = 20;
				py[0] =  0; py[1] = 10; py[2] =  0;
				break;
			case LEFT:
				px[0] = 10; px[1] =  0; px[2] = 10;
				py[0] =  0; py[1] = 10; py[2] = 20;
				break;
			case RIGHT:
				px[0] =  0; px[1] = 10; px[2] = 0;
				py[0] =  0; py[1] = 10; py[2] = 20;
				break;
			default:
				break;
			}

			g2d.fillPolygon(px, py, 3);
		}

		@Override
		public int getIconWidth() { return dir.horizontal ? H : W; }

		@Override
		public int getIconHeight() { return dir.vertical ? H : W; }
	}
}
