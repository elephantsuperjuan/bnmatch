package Cytoscape.plugin.BNMatch.INM;

/**
 *
 * @author YULEI
 */

public class INMEdge 
{
    protected     INMNode source;
    protected     INMNode destination;
    protected     double weight;
    protected     enum enumEdgeType{UNDIRECTION,DIRECTION};
    protected     enumEdgeType enumEType;
     
     INMEdge(String strSource,String strDestination,double weight,enumEdgeType enumEType)
     {
         source=new INMNode(strSource);
         destination=new INMNode(strDestination);
         this.weight=weight;
         this.enumEType=enumEType;
         
         if((this.enumEType.equals(enumEdgeType.UNDIRECTION)) && source.getProteinName().compareTo(destination.getProteinName())>0)// 为比较方便，在无向边中，起点与终点有序
         {
             INMNode tmpNode=source;
             source=destination;
             destination=tmpNode;
         }
     }
     
     INMEdge(INMNode source,INMNode destination)
     {
         this.source=source;
         this.destination=destination;
         this.weight=0;
         this.enumEType=enumEdgeType.UNDIRECTION;
         if((this.enumEType.equals(enumEdgeType.UNDIRECTION)) && source.getProteinName().compareTo(destination.getProteinName())>0)// 为比较方便，在无向边中，起点与终点有序
         {
             this.source=destination;
             this.destination=source;
         }
     }
     
     INMEdge(String strSource,String strDestination)
     {
         source=new INMNode(strSource);
         destination=new INMNode(strDestination);
         this.weight=0;
         this.enumEType=enumEdgeType.UNDIRECTION;
         if((this.enumEType.equals(enumEdgeType.UNDIRECTION)) && source.getProteinName().compareTo(destination.getProteinName())>0)// 为比较方便，在无向边中，起点与终点有序
         {
             INMNode tmpNode=source;
             source=destination;
             destination=tmpNode;
         }
     }


    @Override
     public int hashCode()
     {
         return source.proteinName.hashCode()*destination.proteinName.hashCode();
     }
    
    @Override
    public boolean equals(Object o)
    {
        INMEdge edge=(INMEdge)o;
        return source.getProteinName().equals(edge.source.getProteinName()) &&
                destination.getProteinName().equals(edge.destination.getProteinName());
    }
     
    @Override
    public String toString()
    {
        return source.getProteinName()+destination.getProteinName();
    }

     INMEdge(String strSource,String strDestination,double weight)
     {
         source=new INMNode(strSource);
         destination=new INMNode(strDestination);
         this.weight=weight;
         this.enumEType=enumEdgeType.UNDIRECTION;
         if((this.enumEType.equals(enumEdgeType.UNDIRECTION)) && source.getProteinName().equals(destination.getProteinName()))// 为比较方便，在无向边中，起点与终点有序
         {
             INMNode tmpNode=source;
             source=destination;
             destination=tmpNode;
         }         
     }
    
/**
 * get vertexs
 */
     public void GetVertexs(String vertex1,String vertex2)
     {
         vertex1=source.getProteinName();
         vertex2=destination.getProteinName();
     }
     
     public double GetEdgeWeight()
     {
         return weight;
     }
     
     public enumEdgeType GetEdgeType()
     {
         return enumEType;
     }
  

     public INMNode GetFirstVertex()
     {
         return source;
     }
     
     public INMNode GetSecondVertex()
     {
         return destination;
     }
} 

