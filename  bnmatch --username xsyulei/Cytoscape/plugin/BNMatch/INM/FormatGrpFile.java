package Cytoscape.plugin.BNMatch.INM;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
     * 解析顶点行
     * @param NO
     * @param nodeName
     * @return
     */
    public INMNode ParseNode(String strNode,String nodeName)
    {
        INMNode node=new INMNode("");
        return node;
    }

    /**
     * 解析边数据行
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
  * 输出格式化数据文件
  */  
    public void OutTxtFile()
    {
        
    }
}
