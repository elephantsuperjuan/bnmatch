package Cytoscape.plugin.BNMatch.INM;

/**
 *
 * @author YULEI
 */
public class FormatGrpFile 
{
    enum EdgeType{Y,C};
    String goalFileName;
    String graphFileName;
    String flyNodes;
    String yeastNodes;
    String flyEdges;
    String yeastEdges;
    
    FormatGrpFile()
    {
        
    }
    
    FormatGrpFile(String goalFileName,String graphFileName)
    {
        this.goalFileName=goalFileName;
        this.graphFileName=graphFileName;
    }

    /**
     * 读入目标子图文件和查询图文件
     */
    public void ReadFileData()
    {
        
    }
 
    /**
     * parse peak line
     * @param NO
     * @param nodeName
     * @return INMNode
     */
    public INMNode ParseNode(String strNode,String nodeName)
    {
        INMNode node=new INMNode("");
        return node;
    }

    /**
     * parse edge of data line
     * @param strEdge
     * @param edgeType
     * @return
     */
    public INMEdge ParseEdge(String strEdge,String edgeType)
    {
        INMNode firstNode,secondNode;
        firstNode=new INMNode("");
        secondNode=new INMNode("");
        
        return new INMEdge(firstNode,secondNode);
    }
    
 /**
  * ouput format data to file
  */  
    public void OutTxtFile()
    {
        
    }
}
