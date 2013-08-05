package Cytoscape.plugin.BNMatch;
/**
 *
 * @author YULEI
 */
import cytoscape.CyEdge;
import cytoscape.CyNode;
import giny.model.GraphPerspective;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class BNMatchDataStructure 
{
   static ArrayList<CyNode> matchNodes;
   static HashMap<String,String> matchNodesMap=new HashMap<String,String>();
  /**
    * get matched nodes of input network
    *@param array list
    *@return null  
    */ 
    BNMatchDataStructure(ArrayList<CyNode> nodesAL)
   {        
       matchNodes=nodesAL;
   }
    

/**
 * create the match nodes of large network
 * @return match nodes of large network
 */  
   public static ArrayList<CyNode> createLargeNetworkMatchNodes()
   {
       ArrayList<CyNode> largeMatchNodes=new ArrayList<CyNode>();
       int matchNodesCount=matchNodes.size();
       for(int j = 0; j < matchNodesCount; j=j+2)
       {
           largeMatchNodes.add( matchNodes.get(j + 1));
       }
       return largeMatchNodes;
   }

/**
 * create the match nodes of target network
 * @return match nodes of target network
 */
   public static ArrayList<CyNode> createTargetNetworkMatchNodes()
   {
       ArrayList<CyNode> targetMatchNodes=new ArrayList<CyNode>();
       int matchNodesCount=matchNodes.size();
       for(int j = 0; j < matchNodesCount; j=j+2)
       {
           targetMatchNodes.add( matchNodes.get(j));
       }
       return targetMatchNodes;      
   }
/**
 * create the edges according to large network match nodes
 * @param largeGP
 * @return large network's edges
 */   
   public static ArrayList<CyEdge> createLargeNetworkMatchEdges(GraphPerspective largeGP)
   {
       ArrayList<CyNode> largeNetworkMatchNodes=createLargeNetworkMatchNodes();
       ArrayList<CyEdge> largeNetworkMatchEdges=(ArrayList<CyEdge>) largeGP.getConnectingEdges(largeNetworkMatchNodes);
       return largeNetworkMatchEdges;
   }
   
/**
 * create the edges according to target network match nodes
 * @param targetGP
 * @return target network's edges
 */
      public static ArrayList<CyEdge> createTargetNetworkMatchEdges(GraphPerspective targetGP)
   {
       ArrayList<CyNode> targetNetworkMatchNodes=createTargetNetworkMatchNodes();
       ArrayList<CyEdge> targetNetworkMatchEdges=(ArrayList<CyEdge>) targetGP.getConnectingEdges(targetNetworkMatchNodes);        
       return targetNetworkMatchEdges;
   }
      
/**
 * according match nodes create match directed edges
 * @param largeGP
 * @param targetGP
 * @return match edges
 */      
      public static ArrayList<CyEdge> createMatchEdges(GraphPerspective largeGP,GraphPerspective targetGP,boolean isDirected)
      {
         ArrayList<CyNode> largeNetworkMatchNodes=createLargeNetworkMatchNodes();
         ArrayList<CyNode> targetNetworkMatchNodes=createTargetNetworkMatchNodes();
         ArrayList<CyEdge> largeNetworkMatchEdges=createLargeNetworkMatchEdges(largeGP);
         ArrayList<CyEdge> targetNetworkMatchEdges=createTargetNetworkMatchEdges(targetGP);
        
         ArrayList<CyEdge> matchEdgesAL=new ArrayList<CyEdge>();
         
         HashMap<String,CyEdge> lnmeMap=new HashMap<String,CyEdge>();
         HashMap<String,CyEdge> tnmeMap=new HashMap<String,CyEdge>();

         String tempStr;         
         int count=matchNodes.size()/2;
         
         for(int i=0;i<count;i++)
         {
             //key:match nodes of large network  value:match nodes of target network
             matchNodesMap.put(largeNetworkMatchNodes.get(i).getIdentifier(),targetNetworkMatchNodes.get(i).getIdentifier());
         }         
         
         for(CyEdge e:largeNetworkMatchEdges)
         {    
             tempStr=matchNodesMap.get(e.getSource().getIdentifier())+matchNodesMap.get(e.getTarget().getIdentifier());//这里没有考滤CACF与CFCA这种情况 整个便是有向         
             lnmeMap.put(tempStr, e);
             if(!isDirected)
             {
             tempStr=matchNodesMap.get(e.getTarget().getIdentifier())+matchNodesMap.get(e.getSource().getIdentifier());//这里考滤CACF与CFCA这种情况 整个便是无向
             lnmeMap.put(tempStr, e);
             }
         }
         
         for(CyEdge e:targetNetworkMatchEdges)
         {
             tempStr=e.getSource().getIdentifier()+e.getTarget().getIdentifier();
             tnmeMap.put(tempStr, e);
         }
         
         Set<String> commonSet=new HashSet<String>();
         commonSet=lnmeMap.keySet();
         commonSet.retainAll(tnmeMap.keySet());//the common element
         
         Iterator<String> it=commonSet.iterator();
         while(it.hasNext())
         {
             String str=it.next();
             matchEdgesAL.add(tnmeMap.get(str));
             matchEdgesAL.add(lnmeMap.get(str));
         } 

         return matchEdgesAL;
      }
}
