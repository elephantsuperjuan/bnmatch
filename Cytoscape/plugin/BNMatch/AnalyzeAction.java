/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Cytoscape.plugin.BNMatch;
/**
 *
 * @author YULEI
 */
import Cytoscape.plugin.BNMatch.INM.Config;
import Cytoscape.plugin.BNMatch.INM.INM;
import Cytoscape.plugin.BNMatch.INM.INMAlgorithm;
import cytoscape.CyEdge;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.readers.GraphReader;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;
import cytoscape.visual.VisualPropertyType;

import ding.view.DGraphView;
import giny.model.GraphPerspective;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 *
 * @author e467941
 */
public class AnalyzeAction implements ActionListener
{
    static ArrayList<CyNode> matchNodes;
    static ArrayList<CyEdge> matchEdges;
    ArrayList<CyNode> targetAL;
    ArrayList<CyNode> largeAL;
    ArrayList<CyEdge> targetEAL;
    ArrayList<CyEdge> largeEAL;    
    int matchNodesCount;
    int matchEdgesCount;
    CyNetwork largeNetwork;
    CyNetwork targetNetwork;
    DGraphView largeView;
    DGraphView targetView;
    int resultIndex=0;
    static int matchNodesPanelNO=1;
    String panelTitle;
    BNMatchNetworks BNMNetworks;
    CyNetwork cyNetwork;
    String networkIdentifier;
    AnalyzeAction()
    {
    }

    public void actionPerformed(ActionEvent e)
    {
        if(MainPanel.extenalSIFFile.isSelected())//analyze network from existing file
        {
         cyNetwork = Cytoscape.getCurrentNetwork();
        networkIdentifier = cyNetwork.getIdentifier();        
//        JOptionPane.showMessageDialog(Cytoscape.getDesktop(), str);
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
        
        if(Cytoscape.getDesktop().getNetworkPanel().getNetworkNode(networkIdentifier).getLevel()!=1)
        {
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Please select the correct network!");
            return;
        }
 



        }
         else
        {
            
            Config.setWeight(Double.parseDouble(MainPanel.weightField.getText()));
            INM.runINM();
            MainPanel.runInformation.append("Reading networks...\n");
            GraphReader reader = Cytoscape.getImportHandler().getReader(INMAlgorithm.fileAbsolutePath);
            cyNetwork = Cytoscape.createNetwork(reader, true, null);
            Cytoscape.createNetworkView(cyNetwork);
            networkIdentifier=INMAlgorithm.fileName;
        }

         BNMNetworks = new BNMatchNetworks(cyNetwork);//initialize matchNodes of BNMatchNetworks
          if(BNMNetworks.gpClique==null)
          {
              return;//immediately return
          }
         BNMatchAnalyzeTask task=new BNMatchAnalyzeTask(cyNetwork,networkIdentifier);
         JTaskConfig config = new JTaskConfig();
         //config.displayCancelButton(true);
         config.displayStatus(true);
            //Execute Task via TaskManager
            //This automatically pops-open a JTask Dialog Box
         TaskManager.executeTask(task, config);
         
         if(task.isCompletedSuccessfully==true)
         {
             MainPanel.colorWeight.setEnabled(true);
             MainPanel.resetButton.setEnabled(true);
             MainPanel.refreshImmediately.setEnabled(true);
             MainPanel.refreshButton.setEnabled(true);
             
             createMatchNodesPanel(matchNodes,matchEdges,largeNetwork, targetNetwork);

             applyVisualMappingBypass(largeNetwork,targetNetwork,
                     matchNodes,largeAL,targetAL,matchEdges,largeEAL,targetEAL);
             MainPanel.runInformation.append("Complete successfully!\n");
             JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
                     "" + matchNodesCount + " pairs match nodes,"+ matchEdgesCount+" pairs match edges have been found.", "Completed",
                     JOptionPane.INFORMATION_MESSAGE);
         }
        

    }

            
    public void createMatchNodesPanel(ArrayList<CyNode> matchNodes,ArrayList<CyEdge>matchEdges,CyNetwork lNetwork, CyNetwork tNetwork)
    {
        panelTitle="result "+matchNodesPanelNO;
        
        BNMatchMatchResultPanel matchResultPanel = new BNMatchMatchResultPanel(matchNodes,matchEdges,lNetwork,
                tNetwork);//create match nodes table panel
        matchResultPanel.setPanelTitle(panelTitle);
        CytoPanel cytoPanel = Cytoscape.getDesktop().getCytoPanel(SwingConstants.EAST);
        cytoPanel.add(panelTitle, matchResultPanel);
        matchNodesPanelNO++;
        resultIndex = cytoPanel.indexOfComponent(matchResultPanel);
        cytoPanel.setSelectedIndex(resultIndex);
        cytoPanel.setState(CytoPanelState.DOCK);
    }
    
/**
 * 
 */
    public void applyVisualMappingBypass(CyNetwork ln,CyNetwork tn,ArrayList<CyNode> mnAL,ArrayList<CyNode> lnAL,
            ArrayList<CyNode> tnAL,ArrayList<CyEdge> meAL,ArrayList<CyEdge> leAL,ArrayList<CyEdge> teAL)
    {
       setNodeFillColor(tn,tnAL); 
       setNodeFillColor(ln,lnAL);

       setNodeShape(tn,tnAL); 
       setNodeShape(ln,lnAL);
       
       setEdgeColor(tn,teAL);
       setEdgeColor(ln,leAL); 
       
       setEdgeWidth(meAL);
       
      Cytoscape.getNetworkView(tn.getIdentifier()).redrawGraph(false, true);
      Cytoscape.getNetworkView(ln.getIdentifier()).redrawGraph(false, true);
    }
    

    /**
     * Sets the fill color for the provided nodes.
     * default way to show node color 
     * WARNING: this overwrites the vizmapper settings!
     * @param network network where the nodes should be changed.
     * @param nodesAL ids of the nodes.
     * @return True when the operation succeeds.
     */
    public Boolean setNodeFillColor(CyNetwork network, ArrayList<CyNode> nodesAL)
    {
        return BNMatchVizMapperUtils.setNodeColor(network, nodesAL,
                VisualPropertyType.NODE_FILL_COLOR);
    }
    
    public Boolean setNodeShape(CyNetwork network,ArrayList<CyNode> nodesAL)
    {
        return BNMatchVizMapperUtils.setNodeShape(network,nodesAL);
    }
    
    public void setNodeSize()
    {
        
    }
    
    /**
     * Sets the fill color for the provided nodes.
     * WARNING: this overwrites the vizmapper settings!
     * @param network network where the edges should be changed.
     * @param edgesAL 
     * @return True when the operation succeeds.
     */
    public Boolean setEdgeColor(CyNetwork network, ArrayList<CyEdge> edgesAL)
    {
        return BNMatchVizMapperUtils.setEdgeColor(network, edgesAL,
                VisualPropertyType.EDGE_COLOR);
    }
    
    public void setEdgeWidth(ArrayList<CyEdge> matchEdgesAL)
    {
        int count=matchEdgesAL.size();
        for(int i=0;i<count;i=i+2)
        {
           BNMatchVizMapperUtils.setEdgeProperty(matchEdgesAL.get(i).getIdentifier(),"Edge Line Width", "5");
           BNMatchVizMapperUtils.setEdgeProperty(matchEdgesAL.get(i+1).getIdentifier(),"Edge Line Width", "5");            
        }  
    }
    
    
    class BNMatchAnalyzeTask implements Task
    {
        private TaskMonitor taskMonitor;
        private String str;
        private CyNetwork cyNetwork;
        private boolean isCompletedSuccessfully=false;
        public BNMatchAnalyzeTask(CyNetwork cyNetwork, String str)
        {
            this.str = str;
            this.cyNetwork = cyNetwork;
        }

        public void setTaskMonitor(TaskMonitor taskMonitor) throws IllegalThreadStateException
        {
            if (this.taskMonitor != null)
            {
                throw new IllegalStateException("Task Monitor has already been set.");
            }
            this.taskMonitor = taskMonitor;
        }

        public void halt()
        {
        }

        public String getTitle()
        {
            return "Create Networks";
        }

        public void run()
        {   
            final GraphPerspective largeGP = BNMNetworks.largeNetworkGP();
            final GraphPerspective targetGP = BNMNetworks.targetNetworkGP();           
            final ArrayList arrMatchNodes = BNMNetworks.matchNodes;//match nodes
            
            matchNodes = arrMatchNodes;
//            largeNetwork = Cytoscape.createNetwork(largeGP.getNodeIndicesArray(), largeGP.getEdgeIndicesArray(), str + "_Large Network "+matchNodesPanelNO+".sif", cyNetwork);
            BNMatchDataStructure BNMDS=new BNMatchDataStructure(arrMatchNodes);//just pass the match nodes to BNMatchDataStructure class

            targetAL = BNMatchDataStructure.createTargetNetworkMatchNodes();
            largeAL = BNMatchDataStructure.createLargeNetworkMatchNodes();
            targetEAL=BNMatchDataStructure.createTargetNetworkMatchEdges(targetGP);
            largeEAL=BNMatchDataStructure.createLargeNetworkMatchEdges(largeGP);
            matchEdges=BNMatchDataStructure.createMatchEdges(largeGP, targetGP,MainPanel.directedGraph.isSelected());
            matchEdgesCount=matchEdges.size()/2;
            
            taskMonitor.setPercentCompleted(0);
            taskMonitor.setStatus("Generating Networks...");
            MainPanel.runInformation.append("Generating Networks...\n");
            largeNetwork = Cytoscape.createNetwork(largeGP.getNodeIndicesArray(), largeGP.getEdgeIndicesArray(), str + "_Large Network "+matchNodesPanelNO+".sif", cyNetwork);
            targetNetwork = Cytoscape.createNetwork(targetGP.getNodeIndicesArray(), targetGP.getEdgeIndicesArray(), str + "_Target Network "+matchNodesPanelNO+".sif", cyNetwork);
            largeView = (DGraphView) Cytoscape.createNetworkView(largeNetwork);
            targetView = (DGraphView) Cytoscape.createNetworkView(targetNetwork);
            matchNodesCount = arrMatchNodes.size()/2;


            BNMatchLayout largeLayout = new BNMatchLayout(largeView);
            BNMatchLayout targetLayout = new BNMatchLayout(targetView);

            taskMonitor.setPercentCompleted(10);
            taskMonitor.setStatus("Layouting large network...");
            MainPanel.runInformation.append("Layouting large network...\n");
            largeLayout.doLayout(0, 0, 0, null);

            taskMonitor.setPercentCompleted(50);
            taskMonitor.setStatus("Layouting target network...");
            MainPanel.runInformation.append("Layouting target network...\n");
            targetLayout.doLayout(0, 0, 0, null);
            largeView.fitContent();//Fits all Viewable elements onto the Graph

            taskMonitor.setPercentCompleted(80);
            taskMonitor.setStatus("Relayouting target network... ");
            MainPanel.runInformation.append("Relayouting target network...\n");
            for (int j = 0; j < matchNodesCount; j++)
            {
                targetView.getNodeView(targetAL.get(j).getRootGraphIndex()).setXPosition(largeView.getNodeView(largeAL.get(j).getRootGraphIndex()).getXPosition());
                targetView.getNodeView(targetAL.get(j).getRootGraphIndex()).setYPosition(largeView.getNodeView(largeAL.get(j).getRootGraphIndex()).getYPosition());
                taskMonitor.setPercentCompleted(80 + j / matchNodesCount * 15);
            }
            targetView.fitContent();
            taskMonitor.setPercentCompleted(100);
            isCompletedSuccessfully=true;

        }
    }
  
}
