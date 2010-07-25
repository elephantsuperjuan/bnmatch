package Cytoscape.plugin.BNMatch.INM;


import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Iterator;

/**
 *
 * @author YULEI
 */
public class INMEdgeList extends HashSet 
{
    String fileName;
    
    INMEdgeList(String fileName)
    {
        this.fileName=fileName;
        RetrieveEdgeFromFile();
    }

    /**
     * 向列表中添加一条新边，若该边已经存在，则忽略
     * @param edge
     */
    public void AddNewEdge(INMEdge edge)
    {
        add(edge);
    }
    
    

    /**
     * 向列表中添加一条新边，若该边已经存在，则忽略
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
     * 测试某条边是否存在
     * @param edge
     * @return
     */
    public boolean IsExistedEdge(INMEdge edge)
    {
        return contains(edge);
    }
 
    /**
     * 删除xNode相关的所有的边
     * @param edge
     */
    public void RemoveEdge(INMEdge edge)
    {
        remove(edge);
    }
    
    /**
     * 返回所有以 xVertex 作为顶点的边
     * @param vertex
     * @return
     */
    public INMEdgeList GetEdgesNodeAsVertex(INMNode vertex)
    {
        INMEdgeList edgeList=new INMEdgeList("");
        
        return edgeList;
    }

    /**
     * 从文件中读取边的信息
     */
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
/**
 * 删除xNode相关的所有的边
 * @param node
 */  
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

    /**
     * 返回以xNode为顶点的边
     * @param node
     * @return
     */
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
