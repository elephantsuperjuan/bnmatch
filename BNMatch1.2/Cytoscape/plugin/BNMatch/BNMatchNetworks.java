package Cytoscape.plugin.BNMatch;
/**
 *
 * @author YULEI
 */
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import giny.model.Edge;
import giny.model.GraphPerspective;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JOptionPane;


public class BNMatchNetworks 
{
    CyNetwork sifNetwork=null;
    gpClique[] gpClique=new gpClique[2];
    ArrayList<CyNode> matchNodes=new ArrayList<CyNode>();//stores the matched  nodes
    ArrayList<CyNode> targetTotalNodes=new ArrayList<CyNode>();
    ArrayList<CyNode> largeTotalNodes=new ArrayList<CyNode>();
    BNMatchNetworks(CyNetwork sifNetwork)
    {
        this.sifNetwork=sifNetwork;
        CreateGPClique gpc=new CreateGPClique(sifNetwork);
        gpClique=gpc.creategpClique();
    }

    /**
     * get the large Netwokr from the input sif file
     * @return large network Graph Perspective
     */
    public GraphPerspective largeNetworkGP()
    {    
        GraphPerspective largeGP=createGraphPerspective((ArrayList)gpClique[0].getgpNodes(), sifNetwork);     
        return largeGP;
    }
   
    public GraphPerspective totalNetworkGP()
    {
    	ArrayList matchNodesList=new ArrayList();
    	for(int i=0;i<matchNodes.size();i++)
    	{
    		matchNodesList.add(matchNodes.get(i).getRootGraphIndex());
    	}
    	GraphPerspective totalGP=createGraphPerspective(matchNodesList, sifNetwork);     
        return totalGP;
    }
    /**
     * get the target network from the input sif file
     * @return target network Graph Perspective
     */
    public GraphPerspective targetNetworkGP()
    {
    	
        GraphPerspective targetGP=createGraphPerspective((ArrayList)gpClique[1].getgpNodes(), sifNetwork);     
        return targetGP;
    }
    
    /**
     * create graphPerspective for nodes in a cluster
     * @param alNode the nodes
     * @param inputNetwork the original network
     * @return the graph perspective created
     */
    public  GraphPerspective createGraphPerspective(ArrayList alNodes, CyNetwork inputNetwork) 
    {
        int alNodeCount=alNodes.size();
        int[] arrayNode = new int[alNodeCount];
        for (int i = 0; i < alNodeCount; i++) {
            arrayNode[i] = ((Integer) alNodes.get(i)).intValue();
        }
        GraphPerspective gp = inputNetwork.createGraphPerspective(arrayNode);
        return gp;
    }
    
   /**
     * create graphPerspective for nodes in a cluster
     * @param alNode the nodes
     * @param inputNetwork the original network
     * @return the graph perspective created
     */
    public  class CreateGPClique
    {
       private CyNetwork nw;
       public CreateGPClique(CyNetwork network)
      {
           nw=network;
       }
       
       public gpClique[] creategpClique()
       {
         gpClique[] gps= new gpClique[2];
         gps[0]=new gpClique();
         gps[1]=new gpClique();
         
         Iterator<Edge> edgeIt=nw.edgesIterator();
         CyAttributes edgeAtt = Cytoscape.getEdgeAttributes();
                  
         ArrayList largeALNodes=new ArrayList();
         ArrayList targetALNodes=new ArrayList();
         ArrayList matchALNodes=new ArrayList();
          while(edgeIt.hasNext())
          {   

             Edge edge=edgeIt.next();
             if(edgeAtt.getStringAttribute(edge.getIdentifier(),"interaction").equals("p2"))
             {
               largeALNodes.add(new Integer(nw.getEdgeSourceIndex(edge.getRootGraphIndex())));
               largeALNodes.add(new Integer(nw.getEdgeTargetIndex(edge.getRootGraphIndex())));
               continue;//go directly next step
             }
             if(edgeAtt.getStringAttribute(edge.getIdentifier(), "interaction").equals("p1"))
             {
               targetALNodes.add(new Integer(nw.getEdgeSourceIndex(edge.getRootGraphIndex())));
               targetALNodes.add(new Integer(nw.getEdgeTargetIndex(edge.getRootGraphIndex())));
               continue; 
             }
             if(edgeAtt.getStringAttribute(edge.getIdentifier(), "interaction").equals("pd"))
             { 
               matchALNodes.add(new Integer(nw.getEdgeSourceIndex(edge.getRootGraphIndex())));//the matched node(containing "-") of target network
               matchALNodes.add(new Integer(nw.getEdgeTargetIndex(edge.getRootGraphIndex())));//the matched node(containing "-") of large network
               continue;
             }
             JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "File Format error!");
             return null;
          }
         
         Iterator matchALNIt=matchALNodes.iterator();
         CyNode node;
         Integer it;
         
         Iterator largeALNIt=largeALNodes.iterator();
         while(largeALNIt.hasNext())
         {
        	 it=(Integer)largeALNIt.next();
        	 node=Cytoscape.getCyNode(nw.getNode(it.intValue()).getIdentifier().substring(1),true);
        	 largeTotalNodes.add(node);
         }
         
         Iterator targetALNIt=targetALNodes.iterator();
         while(targetALNIt.hasNext())
         {
        	 it=(Integer)targetALNIt.next();
        	 node=Cytoscape.getCyNode(nw.getNode(it.intValue()).getIdentifier().substring(1),true);
        	 targetTotalNodes.add(node);
         }
         
         while(matchALNIt.hasNext())
         {
             it=(Integer)matchALNIt.next();
             node=Cytoscape.getCyNode(nw.getNode(it.intValue()).getIdentifier().substring(1),true);//get node according to its identifier
              matchNodes.add(node);
              if(!targetALNodes.contains(node.getRootGraphIndex()))//matched node,but not a node of network
              {
                  nw.addNode(node);
                  targetALNodes.add(node.getRootGraphIndex());//add to network
              }
                         
             it=(Integer) matchALNIt.next();
             node=Cytoscape.getCyNode(nw.getNode(it.intValue()).getIdentifier().substring(1),true);//get node according to its identifier
             matchNodes.add(node);
             if(!largeALNodes.contains(node.getRootGraphIndex()))
             {
                 nw.addNode(node);
                 largeALNodes.add(node.getRootGraphIndex());
             }
         }         
       
         gps[0].setgpNodes(largeALNodes);
         gps[1].setgpNodes(targetALNodes);
         return gps;
       }
    }
    
   public void showNodeID()
   {
   }
}
