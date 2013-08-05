package Cytoscape.plugin.BNMatch;

/**
 *
 * @author YULEI
 */
import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.parsers.ValueParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JOptionPane;

/**
 * 
 * set the visual style of different nodes and edges.
 */
public class BNMatchVizMapperUtils {

	public static final String[] colorStr = { "red", "green", "blue", "cyan",
			"magenta", "green2", "black" };

	public static final String[] shapeStr = { "diamond", "ellipse", "hexagon",
			"octagon", "parallelogram", "rect", "triangle" };
	static List<ColourShape> shapeColor = null;
	public BNMatchVizMapperUtils() {

	}

	/** Map for converting Strings to colors */
	public static final Map<String, String> COLORSTRINGS = Collections
			.unmodifiableMap(new HashMap<String, String>() {
				{
					put("red", BNMatchVizMapperUtils.getColor(255, 0, 0));
					put("green", BNMatchVizMapperUtils.getColor(0, 255, 0));
					put("blue", BNMatchVizMapperUtils.getColor(0, 0, 255));
					put("cyan", BNMatchVizMapperUtils.getColor(255, 0, 255));
					put("magenta", BNMatchVizMapperUtils.getColor(0, 255, 255));
					put("green2", BNMatchVizMapperUtils.getColor(0, 102, 51));
					put("black", BNMatchVizMapperUtils.getColor(0, 51, 51));
				}
			});

	/** Map for converting Strings to node shapes. */
	public static final Map<String, NodeShape> SHAPESTRINGS = Collections
			.unmodifiableMap(new HashMap<String, NodeShape>() {
				{
					put("diamond", NodeShape.DIAMOND);
					put("ellipse", NodeShape.ELLIPSE);
					put("hexagon", NodeShape.HEXAGON);
					put("octagon", NodeShape.OCTAGON);
					put("parallelogram", NodeShape.PARALLELOGRAM);
					put("rect", NodeShape.RECT);
					put("triangle", NodeShape.TRIANGLE);
				}
			});

	/**
	 * Method to retrieve a VisualPropertyType based on the supplied name.
	 * 
	 * @param propertyName
	 *            Name of the VisualProperty.
	 * @return the requested VisualPropertyType
	 */
	public static VisualPropertyType getVisualPropertyType(String propertyName) {
		for (VisualPropertyType vpt : VisualPropertyType.values()) {
			if (vpt.getName().equals(propertyName)) {
				return vpt;
			}
		}
		return null;
	}

	/**
	 * Set a visual property to overwrite whatever the VizMapper is doing.
	 * 
	 * @param id
	 *            ID of the item (node or edge)
	 * @param attrs
	 *            Attributes where the property should be written to.
	 * @param propertyName
	 *            Name of the property that should be set.
	 * @param propertyValue
	 *            String representation of the property value.
	 * @return true
	 */
	private static boolean setProperty(String id, CyAttributes attrs,
			String propertyName, String propertyValue) {
		VisualPropertyType visPropType = getVisualPropertyType(propertyName);
		ValueParser parser = visPropType.getValueParser();
		String value = parser.parseStringValue(propertyValue).toString();
		attrs.setAttribute(id, visPropType.getBypassAttrName(), value);

		return true;
	}

	/**
	 * Set a visual property for a node to overwrite whatever the VizMapper is
	 * doing.
	 * 
	 * @param nodeid
	 *            ID of the node that should be changed.
	 * @param propertyName
	 *            name of the property.
	 * @param propertyValue
	 *            String representation of the property value.
	 * @return true
	 */
	public static boolean setNodeProperty(String nodeid, String propertyName,
			String propertyValue) {
		CyAttributes attrs = Cytoscape.getNodeAttributes();
		return setProperty(nodeid, attrs, propertyName, propertyValue);
	}

	/**
	 * Set a visual property for an edge to overwrite whatever the VizMapper is
	 * doing.
	 * 
	 * @param edgeid
	 *            ID of the edge that should be changed.
	 * @param propertyName
	 *            name of the property.
	 * @param propertyValue
	 *            String representation of the property value.
	 * @return true
	 */
	public static boolean setEdgeProperty(String edgeid, String propertyName,
			String propertyValue) {
		CyAttributes attrs = Cytoscape.getEdgeAttributes();
		return setProperty(edgeid, attrs, propertyName, propertyValue);
	}

	/**
	 * Function that actually performs the color changes for nodes.
	 * 
	 * @param network
	 *            the network where this function needs to be performed.
	 * @param nodeAL
	 *            nodeAL of the nodes whose color needs to be changed.
	 * @param type
	 *            Type of paramter you want to change (labelcolor, bordercolor,
	 *            etc.)
	 * @return True when the operation succees.
	 */
	public static boolean setNodeColor(final CyNetwork network,
			final ArrayList<CyNode> nodeAL, final VisualPropertyType type) {
		CyNetworkView cnv = Cytoscape.getNetworkView(network.getIdentifier());
		CyAttributes attrs = Cytoscape.getNodeAttributes();

		int count = nodeAL.size();
		updateColourShape(nodeAL.size());
		for (int i = 0; i < count; i++) {
			attrs.setAttribute(nodeAL.get(i).getIdentifier(),
					type.getBypassAttrName(), shapeColor.get(i).color);
		}
		// Set the visability of this 'hack' to false.
		attrs.setUserVisible(type.getBypassAttrName(), false);
		cnv.redrawGraph(false, true);
		return true;
	}

	/**
	 * Function that actually performs the color for nodes according to color
	 * weight.
	 * 
	 * @param network
	 *            the network where this function needs to be performed.
	 * @param nodeAL
	 *            nodeAL of the nodes whose color needs to be changed.
	 * @param type
	 *            Type of paramter you want to change (labelcolor, bordercolor,
	 *            etc.)
	 * @return True when the operation succees.
	 */
	public static boolean setNodeGradientColor(final CyNetwork network,
			final ArrayList<CyNode> nodeAL, final VisualPropertyType type) {
		CyNetworkView cnv = Cytoscape.getNetworkView(network.getIdentifier());
		CyAttributes attrs = Cytoscape.getNodeAttributes();

		int count = nodeAL.size();
		String[] str = null;
		float colorWeight = 0.0f;
		for (int i = 0; i < count; i++) {
			str = nodeAL.get(i).getIdentifier().split("[\\(\\)]");// split
																	// string
			if (str.length != 1) {
				colorWeight = Float.valueOf(str[1]).floatValue();
				attrs.setAttribute(nodeAL.get(i).getIdentifier(), type
						.getBypassAttrName(), BNMatchVizMapperUtils.getColor(
						(int) (255 - 255 * colorWeight), 255,
						(int) (255 - 255 * colorWeight)));
			} else {
				attrs.setAttribute(nodeAL.get(i).getIdentifier(),
						type.getBypassAttrName(),
						COLORSTRINGS.get(colorStr[i % 7]));
			}
		}
		// Set the visability of this 'hack' to false.
		attrs.setUserVisible(type.getBypassAttrName(), false);
		cnv.redrawGraph(false, true);
		return true;
	}

	/**
	 * Sets the node shapes of the provided nodes in the provided network.
	 * 
	 * @param networkID
	 * @param nodeIDs
	 * @param shapestr
	 * @return
	 * @throws XmlRpcException
	 */
	public static Boolean setNodeShape(final CyNetwork network,
			final ArrayList<CyNode> totalNodes, final ArrayList<CyNode> nodeAL,
			Boolean Show_Common) {
		CyNetworkView cn = Cytoscape.getNetworkView(network.getIdentifier());
		CyAttributes attrs = Cytoscape.getNodeAttributes();
		VisualPropertyType type = VisualPropertyType.NODE_SHAPE;
		updateColourShape(nodeAL.size());
		int count = nodeAL.size();
		for (int i = 0; i < count; i++) {
			
			String Cur_node_name = nodeAL.get(i).getIdentifier();
			attrs.setAttribute(Cur_node_name, type.getBypassAttrName(),
					shapeColor.get(i).shape);
			attrs.setAttribute(Cur_node_name,
					VisualPropertyType.NODE_LINE_WIDTH.getBypassAttrName(),
					"10");
		}

		int nodesnum = totalNodes.size();
		for (int j = 0; j < nodesnum; j++) {

			String Cur_node_name = totalNodes.get(j).getIdentifier();
			attrs.setAttribute(Cur_node_name,
					VisualPropertyType.NODE_SIZE.getBypassAttrName(), "160");
			attrs.setAttribute(Cur_node_name,
					VisualPropertyType.NODE_FONT_SIZE.getBypassAttrName(), "40");

		}

		// Set the visability of this 'hack' to false.
		attrs.setUserVisible(type.getBypassAttrName(), false);
		cn.redrawGraph(false, true);
		return true;
	}

	/**
	 * Function that actually performs the color changes for edges.
	 * 
	 * @param network
	 *            the network where this function needs to be performed.
	 * @param edgeAL
	 *            nodeAL of the nodes whose color needs to be changed.
	 * @param type
	 *            Type of paramter you want to change (labelcolor, bordercolor,
	 *            etc.)
	 * @return True when the operation succees.
	 */
	public static boolean setEdgeColor(final CyNetwork network,
			final ArrayList<CyEdge> edgeAL, final VisualPropertyType type) {
		CyNetworkView cnv = Cytoscape.getNetworkView(network.getIdentifier());
		CyAttributes attrs = Cytoscape.getNodeAttributes();

		int count = edgeAL.size();
		for (int i = 0; i < count; i++) {
			attrs.setAttribute(edgeAL.get(i).getIdentifier(),
					type.getBypassAttrName(), COLORSTRINGS.get(colorStr[6]));
			attrs.setAttribute(edgeAL.get(i).getIdentifier(),
					VisualPropertyType.EDGE_LINE_WIDTH.getBypassAttrName(),
					"20");
		}
		// Set the visability of this 'hack' to false.
		attrs.setUserVisible(type.getBypassAttrName(), false);
		cnv.redrawGraph(false, true);
		return true;
	}

	/**
	 * Creates a color string as used by Cytoscape from the specified red, green
	 * and blue parameters.
	 * 
	 * @param r
	 *            red component
	 * @param g
	 *            green component
	 * @param b
	 *            blue component
	 * @return a string containing the color representation used in Cytoscape.
	 * @throws XmlRpcException
	 */
	public static String getColor(final int r, final int g, final int b) {
		if (r < 0 || r > 255) {
			System.err.println("Invalid value for red, please specify "
					+ "an integer between 0 and 255.");
		}
		if (g < 0 || g > 255) {
			System.err.println("Invalid value for green, please specify "
					+ "an integer between 0 and 255.");
		}
		if (b < 0 || b > 255) {
			System.err.println("Invalid value for blue, please specify "
					+ "an integer between 0 and 255.");
		}
		return Integer.toString(r) + "," + Integer.toString(g) + ","
				+ Integer.toString(b);
	}
	 
	public static void updateColourShape(int size)
	{
		if (shapeColor==null)
			{
			shapeColor = new ArrayList<ColourShape>();
			}
		if(shapeColor.size()<size)
		{
		Random random = new Random();
		int n=size-shapeColor.size()+1;
		for (int index = 0; index < n; index++) {
			if (index < 7) {
				ColourShape mColourShape = new ColourShape();
				String color = COLORSTRINGS
						.get(colorStr[index]);
				String shape = SHAPESTRINGS
						.get(shapeStr[index]).getShapeName();
				mColourShape.color = color;
				mColourShape.shape = shape;
				shapeColor.add(mColourShape);
			} else {
				ColourShape mColourShape = new ColourShape();

				int find = 1;
				while (find == 1) {
					find = 0;
					String color = COLORSTRINGS
							.get(colorStr[random.nextInt(7)]);
					String shape = SHAPESTRINGS
							.get(shapeStr[random.nextInt(7)]).getShapeName();
					mColourShape.color = color;
					mColourShape.shape = shape;
					for (int nums = 0; nums < shapeColor.size(); nums++) {
						if (shapeColor.get(nums).IsSame(mColourShape)) {
							find = 1;
							break;
						}
					}
				}
				shapeColor.add(mColourShape);
			}
		}
		}
	}
	public static void UpdateNetwork(CyNetwork totalNetwork,
			Map<CyNode, CyNode> matchNodes, Map<CyEdge, CyEdge> matchEdges) {
		int steps=0;
		MainPanel.runInformation.append(String.format("Step %d...\n",++steps));
		CyNetworkView cnv = Cytoscape.getNetworkView(totalNetwork
				.getIdentifier());
		CyAttributes attrs = Cytoscape.getNodeAttributes();
		// Set the visability of this 'hack' to false.

		Set<CyNode> key = matchNodes.keySet();
		MainPanel.runInformation.append(String.format("Step %d...\n",++steps));
		VisualPropertyType type = VisualPropertyType.NODE_FILL_COLOR;
		int i = 0;
		
		MainPanel.runInformation.append(String.format("Step %d...\n",++steps));
		updateColourShape(matchNodes.size());
		MainPanel.runInformation.append(String.format("Step %d...\n",++steps));
		for (Iterator<CyNode> it = key.iterator(); it.hasNext();) {
			CyNode target = (CyNode) it.next();
			CyNode src = matchNodes.get(target);
			String targetName = target.getIdentifier();
			String srcName = src.getIdentifier();
			attrs.setAttribute(targetName,
					VisualPropertyType.NODE_FILL_COLOR.getBypassAttrName(),
					shapeColor.get(i).color);
			attrs.setAttribute(srcName,
					VisualPropertyType.NODE_FILL_COLOR.getBypassAttrName(),
					shapeColor.get(i).color);

			attrs.setAttribute(targetName,
					VisualPropertyType.NODE_SHAPE.getBypassAttrName(),
					shapeColor.get(i).shape);
			attrs.setAttribute(srcName,
					VisualPropertyType.NODE_SHAPE.getBypassAttrName(),
					shapeColor.get(i).shape);

			/*attrs.setAttribute(targetName,
					VisualPropertyType.NODE_SIZE.getBypassAttrName(), "160");
			attrs.setAttribute(srcName,
					VisualPropertyType.NODE_SIZE.getBypassAttrName(), "160");

			attrs.setAttribute(targetName,
					VisualPropertyType.NODE_LINE_WIDTH.getBypassAttrName(),
					"10");
			attrs.setAttribute(srcName,
					VisualPropertyType.NODE_LINE_WIDTH.getBypassAttrName(),
					"10");

			attrs.setAttribute(targetName,
					VisualPropertyType.NODE_FONT_SIZE.getBypassAttrName(), "40");
			attrs.setAttribute(srcName,
					VisualPropertyType.NODE_FONT_SIZE.getBypassAttrName(), "40");*/
			i++;
		}
		MainPanel.runInformation.append(String.format("Step %d...\n",++steps));
		Set<CyEdge> keyofEdge = matchEdges.keySet();
		for (Iterator<CyEdge> it = keyofEdge.iterator(); it.hasNext();) {
			CyEdge target = (CyEdge) it.next();
			CyEdge src = matchEdges.get(target);
			String targetName = target.getIdentifier();
			String srcName = src.getIdentifier();
			attrs.setAttribute(targetName,
					VisualPropertyType.EDGE_LINE_WIDTH.getBypassAttrName(),
					"10");
			attrs.setAttribute(srcName,
					VisualPropertyType.EDGE_LINE_WIDTH.getBypassAttrName(),
					"10");
		}
		MainPanel.runInformation.append(String.format("Step %d...\n",++steps));
		attrs.setUserVisible(type.getBypassAttrName(), false);
		cnv.redrawGraph(false, true);
		Cytoscape.getNetworkView(totalNetwork.getIdentifier()).redrawGraph(
				false, true);
	}
	
static	class ColourShape {
		public String color, shape;

		public Boolean IsSame(ColourShape s) {
			return color.equals(s.color) && shape.equals(s.shape);
		}
	}

}
