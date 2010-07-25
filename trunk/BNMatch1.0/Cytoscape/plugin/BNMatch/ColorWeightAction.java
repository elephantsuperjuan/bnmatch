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
import cytoscape.view.CyNetworkView;
import cytoscape.visual.VisualPropertyType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author YULEI
 */
class ColorWeightAction implements ActionListener
{
    boolean isRefreshOnce=true;
    CyNetwork cyNetwork;
    String str;
    int networkId;
    CyNetwork largeNetwork;
    CyNetwork targetNetwork;
    CyNetworkView largeView;
    CyNetworkView targetView;
    ArrayList<CyNode> targetAL;
    ArrayList<CyNode> largeAL;


    CyAttributes attrs = Cytoscape.getNodeAttributes();
    
    public ColorWeightAction()
    {
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
 
        if(networkId%2==0)//the current network is target network
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
            largeNetwork=Cytoscape.getNetwork(String.valueOf(networkId-1));
            targetNetwork=cyNetwork;
             largeView=Cytoscape.createNetworkView(largeNetwork);
             targetView =  Cytoscape.createNetworkView(cyNetwork);             
        }
        else
        {
            if(Cytoscape.getDesktop().getNetworkPanel().getNetworkNode(
                    Cytoscape.getNetwork(String.valueOf(networkId+1)).getIdentifier())==null)
            {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "The correspond target network has been destroyed!");
                MainPanel.refreshImmediately.setSelected(false);
                return;
            }
            largeNetwork=cyNetwork;
            targetNetwork=Cytoscape.getNetwork(String.valueOf(networkId+1));
            largeView= Cytoscape.createNetworkView(cyNetwork);
            targetView=Cytoscape.createNetworkView(targetNetwork);
        }
        
        targetAL = BNMatchDataStructure.createTargetNetworkMatchNodes();
        largeAL = BNMatchDataStructure.createLargeNetworkMatchNodes();
       
         if (MainPanel.colorWeight.isSelected())
         {
          BNMatchVizMapperUtils.setNodeGradientColor(largeNetwork, largeAL,VisualPropertyType.NODE_FILL_COLOR);
          BNMatchVizMapperUtils.setNodeGradientColor(targetNetwork, targetAL,VisualPropertyType.NODE_FILL_COLOR);
         }
         else
         {
          BNMatchVizMapperUtils.setNodeColor(largeNetwork, largeAL,VisualPropertyType.NODE_FILL_COLOR);
          BNMatchVizMapperUtils.setNodeColor(targetNetwork, targetAL,VisualPropertyType.NODE_FILL_COLOR);
         }
    }
}
