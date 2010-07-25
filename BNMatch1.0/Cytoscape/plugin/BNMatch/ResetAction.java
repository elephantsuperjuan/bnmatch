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
import ding.view.DGraphView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author e467941
 */
public class ResetAction implements ActionListener 
{
   ResetAction()
   {
       
   }
   
    public void actionPerformed(ActionEvent e)
    {
        CyNetwork cyNetwork = Cytoscape.getCurrentNetwork();
        String str = cyNetwork.getIdentifier();
        int networkId;

        try
        {
         networkId=Integer.valueOf(str).intValue();
        }catch(NumberFormatException numberE)
        {
          JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Please select the correct network!");
          return;
        }

        final DGraphView largeView;
        final DGraphView targetView;
       // System.err.println(networkId);
        if (cyNetwork == null)
          {
            System.err.println("Can't get a network.");
            return;
          }
        
        if (cyNetwork.getNodeCount() < 1)
          {
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
                    "Network has not been loaded!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
          }
        
        if(!Cytoscape.getDesktop().getNetworkPanel().getNetworkNode(str).isLeaf())
        {
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Please select the correct network!");
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
                return;
            }
             largeView=(DGraphView) Cytoscape.createNetworkView(Cytoscape.getNetwork(String.valueOf(networkId-1)));
             targetView = (DGraphView) Cytoscape.createNetworkView(cyNetwork);             
        }
        else
        {
            if(Cytoscape.getDesktop().getNetworkPanel().getNetworkNode(
                    Cytoscape.getNetwork(String.valueOf(networkId+1)).getIdentifier())==null)
            {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "The correspond target network has been destroyed!");
                return;
            }
            largeView=(DGraphView) Cytoscape.createNetworkView(cyNetwork);
            targetView=(DGraphView)Cytoscape.createNetworkView(Cytoscape.getNetwork(String.valueOf(networkId+1)));
        }
       
       final ArrayList<CyNode> arrMatchNodes = AnalyzeAction.matchNodes;//

       final SwingWorker worker=new SwingWorker()
       {
           public Object construct()
           {
               ArrayList<CyNode> targetAL=BNMatchDataStructure.createTargetNetworkMatchNodes();
               ArrayList<CyNode> largeAL=BNMatchDataStructure.createLargeNetworkMatchNodes();

                
                BNMatchLayout largeLayout = new BNMatchLayout(largeView);
                BNMatchLayout targetLayout = new BNMatchLayout(targetView);

                largeLayout.doLayout(0, 0, 0, null);
                targetLayout.doLayout(0, 0, 0, null);
                largeView.fitContent();//Fits all Viewable elements onto the Graph
                int matchNodesCount=arrMatchNodes.size()/2;
                for (int j = 0; j < matchNodesCount; j++)
                    {
                    targetView.getNodeView(targetAL.get(j).getRootGraphIndex()).setXPosition(largeView.getNodeView(largeAL.get(j).getRootGraphIndex()).getXPosition());
                    targetView.getNodeView(targetAL.get(j).getRootGraphIndex()).setYPosition(largeView.getNodeView(largeAL.get(j).getRootGraphIndex()).getYPosition());
                    }
                targetView.fitContent();
                return null;
           }
       };     
       worker.start();
  
    }
}
