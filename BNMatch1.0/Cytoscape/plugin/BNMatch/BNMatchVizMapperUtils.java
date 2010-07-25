/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.Map;

/**
 * A visual style for ClusterPlugin 
 * set the visual style of different node shape and color.
 */
public class BNMatchVizMapperUtils 
{

    public static final String[] colorStr = {"red", "green",
        "blue", "cyan", "magenta", "green2", "black"
    };
    
    public static final String[] shapeStr={"diamond","ellipse",
    "hexagon","octagon","parallelogram","rect","triangle"};

    public BNMatchVizMapperUtils()
    {

    }

    /** Map for converting Strings to colors*/
    public static final Map<String, String> COLORSTRINGS =
            Collections.unmodifiableMap(new HashMap<String, String>() { {
                    put("red",BNMatchVizMapperUtils.getColor(255, 0, 0) );
                    put("green",BNMatchVizMapperUtils.getColor(0, 255, 0)  );
                    put("blue",BNMatchVizMapperUtils.getColor(0, 0, 255)  );
                    put("cyan",BNMatchVizMapperUtils.getColor(255, 0, 255)  );
                    put("magenta",BNMatchVizMapperUtils.getColor(0, 255, 255) );
                    put("green2", BNMatchVizMapperUtils.getColor(0, 102, 51) );
                    put("black", BNMatchVizMapperUtils.getColor(0, 51, 51) );
            } });
            
    /** Map for converting Strings to node shapes.*/
    public static final Map<String, NodeShape> SHAPESTRINGS =
            Collections.unmodifiableMap(new HashMap<String, NodeShape>() { {
                    put("diamond", NodeShape.DIAMOND);
                    put("ellipse", NodeShape.ELLIPSE);
                    put("hexagon", NodeShape.HEXAGON);
                    put("octagon", NodeShape.OCTAGON);
                    put("parallelogram", NodeShape.PARALLELOGRAM);
                    put("rect", NodeShape.RECT);
                    put("triangle", NodeShape.TRIANGLE);
            } });
            
    /**
     * Method to retrieve a VisualPropertyType based on the supplied name.
     * @param propertyName Name of the VisualProperty.
     * @return the requested VisualPropertyType
     */
    public static VisualPropertyType getVisualPropertyType(String propertyName)
    {
        for (VisualPropertyType vpt : VisualPropertyType.values())
        {
            if (vpt.getName().equals(propertyName))
            {
                return vpt;
            }
        }
        return null;
    }

    /**
     * Set a visual property to overwrite whatever the VizMapper is doing.
     * @param id ID of the item (node or edge)
     * @param attrs Attributes where the property should be written to.
     * @param propertyName Name of the property that should be set.
     * @param propertyValue String representation of the property value.
     * @return true
     */
    private static boolean setProperty(String id, CyAttributes attrs,
            String propertyName, String propertyValue)
    {
        VisualPropertyType visPropType = getVisualPropertyType(propertyName);
        ValueParser parser = visPropType.getValueParser();
        String value = parser.parseStringValue(propertyValue).toString();
        attrs.setAttribute(id, visPropType.getBypassAttrName(), value);

        return true;
    }

    /**
     * Set a visual property for a node to overwrite whatever the VizMapper is
     * doing.
     * @param nodeid ID of the node that should be changed.
     * @param propertyName name of the property.
     * @param propertyValue String representation of the property value.
     * @return true
     */
    public static boolean setNodeProperty(String nodeid, String propertyName,
            String propertyValue)
    {
        CyAttributes attrs = Cytoscape.getNodeAttributes();
        return setProperty(nodeid, attrs, propertyName, propertyValue);
    }

    /**
     * Set a visual property for an edge to overwrite whatever the VizMapper is
     * doing.
     * @param edgeid ID of the edge that should be changed.
     * @param propertyName name of the property.
     * @param propertyValue String representation of the property value.
     * @return true
     */
    public static boolean setEdgeProperty(String edgeid, String propertyName,
            String propertyValue)
    {
        CyAttributes attrs = Cytoscape.getEdgeAttributes();
        return setProperty(edgeid, attrs, propertyName, propertyValue);
    }
    
     /**
     * Function that actually performs the color changes for nodes.
     * @param network the network where this function needs to be
     * performed.
     * @param nodeAL nodeAL of the nodes whose color needs to be changed.
     * @param type Type of paramter you want to change (labelcolor,
     * bordercolor, etc.)
     * @return True when the operation succees.
     */
    public static boolean setNodeColor(final CyNetwork network,
            final ArrayList<CyNode> nodeAL, final VisualPropertyType type)
    {
        CyNetworkView cnv = Cytoscape.getNetworkView(network.getIdentifier());
        CyAttributes attrs = Cytoscape.getNodeAttributes();
        
        int count=nodeAL.size();
        for (int i=0;i<count;i++)
        {
            attrs.setAttribute(nodeAL.get(i).getIdentifier(), type.getBypassAttrName(),
                   COLORSTRINGS.get(colorStr[i%7]));
        }
        // Set the visability of this 'hack' to false.
        attrs.setUserVisible(type.getBypassAttrName(), false);
        cnv.redrawGraph(false, true);
        return true;
    }   

     /**
     * Function that actually performs the color  for nodes according to color weight.
     * @param network the network where this function needs to be
     * performed.
     * @param nodeAL nodeAL of the nodes whose color needs to be changed.
     * @param type Type of paramter you want to change (labelcolor,
     * bordercolor, etc.)
     * @return True when the operation succees.
     */
    public static boolean setNodeGradientColor(final CyNetwork network,
            final ArrayList<CyNode> nodeAL, final VisualPropertyType type)
    {
        CyNetworkView cnv = Cytoscape.getNetworkView(network.getIdentifier());
        CyAttributes attrs = Cytoscape.getNodeAttributes();
        
        int count=nodeAL.size();
        String[] str=null;
        float colorWeight=0.0f;
        for (int i=0;i<count;i++)
        {
            str=nodeAL.get(i).getIdentifier().split("[\\(\\)]");//split string 
            if(str.length!=1)
            {
            colorWeight=Float.valueOf(str[1]).floatValue();
            attrs.setAttribute(nodeAL.get(i).getIdentifier(), type.getBypassAttrName(),
                   BNMatchVizMapperUtils.getColor((int)(255-255*colorWeight),255,(int)(255-255*colorWeight)));
            }
            else
            {
            attrs.setAttribute(nodeAL.get(i).getIdentifier(), type.getBypassAttrName(),
                   COLORSTRINGS.get(colorStr[i%7]));  
            }
        }
        // Set the visability of this 'hack' to false.
        attrs.setUserVisible(type.getBypassAttrName(), false);
        cnv.redrawGraph(false, true);
        return true;
    }   
    /**
     * Sets the node shapes of the provided nodes in the provided network.
     * @param networkID
     * @param nodeIDs
     * @param shapestr
     * @return
     * @throws XmlRpcException
     */
    public static Boolean setNodeShape(final CyNetwork network,
            final ArrayList<CyNode> nodeAL) 
    {
        CyNetworkView cn = Cytoscape.getNetworkView(network.getIdentifier());
        CyAttributes attrs = Cytoscape.getNodeAttributes();
        VisualPropertyType type = VisualPropertyType.NODE_SHAPE;

        int count=nodeAL.size();
        for (int i=0;i<count;i++) 
        {
            attrs.setAttribute(
                   nodeAL.get(i).getIdentifier(),
                    type.getBypassAttrName(),
                    BNMatchVizMapperUtils.SHAPESTRINGS.get(shapeStr[i%7]).getShapeName());
        }
        // Set the visability of this 'hack' to false.
        attrs.setUserVisible(type.getBypassAttrName(), false);
        cn.redrawGraph(false, true);
        return true;
    }
     /**
     * Function that actually performs the color changes for edges.
     * @param network the network where this function needs to be
     * performed.
     * @param edgeAL nodeAL of the nodes whose color needs to be changed.
     * @param type Type of paramter you want to change (labelcolor,
     * bordercolor, etc.)
     * @return True when the operation succees.
     */
    public static boolean setEdgeColor(final CyNetwork network,
            final ArrayList<CyEdge> edgeAL, final VisualPropertyType type)
    {
        CyNetworkView cnv = Cytoscape.getNetworkView(network.getIdentifier());
        CyAttributes attrs = Cytoscape.getNodeAttributes();
        
        int count=edgeAL.size();
        for (int i=0;i<count;i++)
        {
            attrs.setAttribute(edgeAL.get(i).getIdentifier(), type.getBypassAttrName(),
                   COLORSTRINGS.get(colorStr[6]));
        }
        // Set the visability of this 'hack' to false.
        attrs.setUserVisible(type.getBypassAttrName(), false);
        cnv.redrawGraph(false, true);
        return true;
    }  
/**
     * Creates a color string as used by Cytoscape from the specified red,
     * green and blue parameters.
     * @param r red component
     * @param g green component
     * @param b blue component
     * @return a string containing the color representation used in Cytoscape.
     * @throws XmlRpcException
     */
    public static String getColor(final int r, final int g, final int b)
    {
        if(r < 0 || r > 255){
            System.err.println("Invalid value for red, please specify " +
                    "an integer between 0 and 255.");
        }
        if(g < 0 || g > 255){
            System.err.println("Invalid value for green, please specify "
                    + "an integer between 0 and 255.");
        }
        if(b < 0 || b > 255){
            System.err.println("Invalid value for blue, please specify "
                    + "an integer between 0 and 255.");
        }
        return Integer.toString(r) + "," + Integer.toString(g) + "," +
                Integer.toString(b);
    }
 
}
