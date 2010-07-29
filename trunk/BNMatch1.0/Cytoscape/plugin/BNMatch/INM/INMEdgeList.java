package Cytoscape.plugin.BNMatch.INM;
/**
 *
 * @author YULEI
 */
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import java.util.Iterator;
public class INMEdgeList extends HashSet 
{
    String fileName;
    
    INMEdgeList(String fileName)
    {
        this.fileName=fileName;
        RetrieveEdgeFromFile();
    }


    public void AddNewEdge(INMEdge edge)
    {
        add(edge);
    }
    
    

    /**
     * add a new edge
     * @param source
     * @param destination
     * @param weight
     * @param edgeType
     */
    public void AddNewEdge(String source,String destination,double weight, INMEdge.enumEdgeType edgeType)
    {
        add(new INMEdge(source,destination,weight,edgeType));
    }
   
     public void AddNewEdge(String source,String destination,double weight)
     {
        INMEdge.enumEdgeType edgeType=INMEdge.enumEdgeType.UNDIRECTION;
        add(new INMEdge(source,destination,weight,edgeType));
     }
     
     public void AddNewEdge(String source,String destination,INMEdge.enumEdgeType edgeType)
     {
        double weight=0.0d;
        add(new INMEdge(source,destination,weight,edgeType));
     }
     
     public void AddNewEdge(String source,String destination)
     {
        double weight=0.0d;
        INMEdge.enumEdgeType edgeType=INMEdge.enumEdgeType.UNDIRECTION;
        add(new INMEdge(source,destination,weight,edgeType));
     }     
    /**
     * Test whether there is a certain edge
     * @param edge
     * @return
     */
    public boolean IsExistedEdge(INMEdge edge)
    {
        return contains(edge);
    }
 

    public void RemoveEdge(INMEdge edge)
    {
        remove(edge);
    }
    

    public INMEdgeList GetEdgesNodeAsVertex(INMNode vertex)
    {
        INMEdgeList edgeList=new INMEdgeList("");
        
        return edgeList;
    }


    public void RetrieveEdgeFromFile()
    {
        FileStream fs=new FileStream(fileName);
        if(fs.Open(fileName))
        {
            INMEdge edge=null;           
            while((edge=fs.ReadNextInterrelationObject())!=null)
            {
                AddNewEdge(edge);
            }
            fs.Close();
        }
    }
    
    public void RetrieveEdgeFromFile(String fileName)
    {
        this.fileName=fileName;
        RetrieveEdgeFromFile();
    }
    
    
    public void SortEdgeList()
    {
        
    }
    
    public void PrintAllData()
    {
        
    }
  
    public void RemoveEdge(INMNode node)
    {
        Iterator it=this.iterator();
        while(it.hasNext())
        {
            INMEdge edge=(INMEdge)it.next();
            if(edge.GetFirstVertex().equals(node))
            {
                RemoveEdge(edge);
                return;
            }
        }
    }


    public INMEdge GetFirstEdge(INMNode node)
    {
        Iterator it=this.iterator();
        while(it.hasNext())
        {
           INMEdge edge=(INMEdge)it.next();
           if(edge.GetFirstVertex().equals(node))
               return edge;
        }
        return new INMEdge("0","0");
    }
}
