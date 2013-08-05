/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Cytoscape.plugin.BNMatch;
/**
 *
 * @author YULEI
 */
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.SelectEvent;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.parsers.ObjectToString;
import giny.model.Node;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author YULEI
 */
public class RefreshAction implements ActionListener
{
    boolean isRefreshOnce=true;
    CyNetwork cyNetwork;
    String str;
    int networkId;
    CyNetworkView largeView;
    CyNetworkView targetView;
    ArrayList<CyNode> targetAL;
    ArrayList<CyNode> largeAL;
    int nCount;
    long time=1000;
    static RefreshImmediately immediately;
    static volatile boolean stop = false;
    CyAttributes attrs = Cytoscape.getNodeAttributes();
    VisualPropertyType nodeShapeType = VisualPropertyType.NODE_SHAPE;
    VisualPropertyType nodeColorType = VisualPropertyType.NODE_FILL_COLOR;
   
    RefreshAction(boolean isRefreshOnce)
    {
       this.isRefreshOnce=isRefreshOnce;
    }
    
    public void actionPerformed(ActionEvent e)
    {
        cyNetwork = Cytoscape.getCurrentNetwork();
        str = cyNetwork.getIdentifier();
        try
        {
         networkId=Integer.valueOf(str).intValue();
        }catch(NumberFormatException numberE)
        {
          JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Please select the correct network!");
          MainPanel.refreshImmediately.setSelected(false);
          return;
        }

        if (cyNetwork == null)
          {
            System.err.println("Can't get a network.");
            MainPanel.refreshImmediately.setSelected(false);
            return;
          }
        
        if (cyNetwork.getNodeCount() < 1)
          {
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
                    "Network has not been loaded!", "Error", JOptionPane.WARNING_MESSAGE);
            MainPanel.refreshImmediately.setSelected(false);
            return;
          }
        
        if(!Cytoscape.getDesktop().getNetworkPanel().getNetworkNode(str).isLeaf())
        {
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Please select the correct network!");
            MainPanel.refreshImmediately.setSelected(false);
            return;
        }
 
        if(Cytoscape.getNetwork(String.valueOf(networkId)).getTitle().contains("Target"))//the current network is target network
        {
            if(Cytoscape.getDesktop().getNetworkPanel().getNetworkNode(
                    Cytoscape.getNetwork(String.valueOf(networkId-1)).getIdentifier())==null)
                                                                                                 //the network id of 
                                                                                                //large network is 1 less 
                                                                                                 //than target network's
            {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "The correspond large network has been destroyed!");
                MainPanel.refreshImmediately.setSelected(false);
                return;
            }
             largeView=Cytoscape.createNetworkView(Cytoscape.getNetwork(String.valueOf(networkId-1)));
             targetView =  Cytoscape.createNetworkView(cyNetwork);             
        }        
        else
        {
            System.err.println(networkId);
            if(Cytoscape.getDesktop().getNetworkPanel().getNetworkNode(
                    Cytoscape.getNetwork(String.valueOf(networkId+1)).getIdentifier())==null)
            {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "The correspond target network has been destroyed!");
                MainPanel.refreshImmediately.setSelected(false);
                return;
            }
            largeView= Cytoscape.createNetworkView(cyNetwork);
            targetView=Cytoscape.createNetworkView(Cytoscape.getNetwork(String.valueOf(networkId+1)));
        }
        
        targetAL = BNMatchDataStructure.createTargetNetworkMatchNodes();
        largeAL = BNMatchDataStructure.createLargeNetworkMatchNodes();
        nCount=targetAL.size();
        
        if (!isRefreshOnce)
        {
            if (MainPanel.refreshImmediately.isSelected())
            {
                MainPanel.analyzeButton.setEnabled(false);
                MainPanel.refreshButton.setEnabled(false);
                MainPanel.resetButton.setEnabled(false);
                long start = System.currentTimeMillis();
                refresh();
                long end = System.currentTimeMillis();
                time = end - start;
                time = time + 150;

                immediately = new RefreshImmediately();
                immediately.start();
            /*
            RefreshImmediately immediately=new RefreshImmediately();
            immediately.run();*/
            }
            if (!MainPanel.refreshImmediately.isSelected())
            {
                stop = true;               
                MainPanel.analyzeButton.setEnabled(true);
                MainPanel.refreshButton.setEnabled(true);
                MainPanel.resetButton.setEnabled(true);
                refreshOnce();
            }
        } else
        {
            refreshOnce();
        }
    }
    

    public void refreshOnce()
    {        
       final ArrayList arrMatchNodes = AnalyzeAction.matchNodes;//
       final SwingWorker worker=new SwingWorker()
       {
           public Object construct()
           {
                refresh();
                return null;
           }
       };     
       worker.start();
    }

 class RefreshImmediately extends Thread
{     
    public void run()
    {
        while (!stop)
        {
            refresh();
            try
            {            
                Thread.sleep(time);
            } catch (InterruptedException ex)
            {
                Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
       stop=false;
    }
    
 }
 
 /**
  * refresh the node position,node shape and node color of target network view
  * 
  */
 public void refresh()
{
    Set selectedNodes = cyNetwork.getSelectedNodes();
    if(selectedNodes.size()==0)
    {
        return;
    }
    
    Iterator<Node> it = selectedNodes.iterator();
    CyNode tNode=null;
    while(it.hasNext())
    {
        Node node=it.next();
        tNode=Cytoscape.getCyNode(BNMatchDataStructure.matchNodesMap.get(node.getIdentifier()));
         /*  node positon */
       targetView.getNodeView(tNode.getRootGraphIndex()).setXPosition(largeView.getNodeView(node.getRootGraphIndex()).getXPosition()); 
        targetView.getNodeView(tNode.getRootGraphIndex()).setYPosition(largeView.getNodeView(node.getRootGraphIndex()).getYPosition()); 
        /*  node shape */
        attrs.setAttribute(tNode.getIdentifier(), nodeShapeType.getBypassAttrName(),
                   (NodeShape.getNodeShape((largeView.getNodeView(node.getRootGraphIndex())).getShape())).getShapeName());
        /*  node color */
        attrs.setAttribute(tNode.getIdentifier(), nodeColorType.getBypassAttrName(),
                   ObjectToString.getStringValue(largeView.getNodeView(node.getRootGraphIndex()).getUnselectedPaint()));
    }
    targetView.redrawGraph(false, true);
    targetView.fitContent();
}

}
