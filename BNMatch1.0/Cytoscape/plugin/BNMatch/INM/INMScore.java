package Cytoscape.plugin.BNMatch.INM;
/**
 *
 * @author YULEI
 */
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

public class INMScore 
{
    String matchedFix;
    String yeast;
    String fly;
    INMEdgeList flyNetwork;
    String code;
    String MIPSFileName;
    String SC_DMFileName;
    String yeastFileName;
    HomogenMap homogenMap;
    INMEdgeList yeastNet;
    INMEdgeList flyNet;
    INMEdgeList mateList;
    String scoreFile;
    
    INMScore()
    {
        matchedFix="PD";
        yeast="P1";
        fly="P2";
    }

/**
 * 
 * @param flyNetwork
 * @return
 */
    public boolean Initialize(INMEdgeList flyNetwork)
    {
        this.code="110";
//        Config cfg=new Config();
        this.MIPSFileName=Config.getMipsFileName();
        this.SC_DMFileName=Config.getTableFileName();
        this.yeastFileName=Config.getTargetFileName();
        this.homogenMap=new HomogenMap(this.SC_DMFileName);
        this.flyNetwork=flyNetwork;
        return true;
    }
 
    /**
     * mark for the matching result
     * @param nodeScore
     * @param edgeScore
     * @return
     */
   public double Mark(double nodeScore, double edgeScore)
   {
       INMNodeList tmp;
       INMEdgeList homoMateList=new INMEdgeList("");
       INMEdgeList tmpMateList=new INMEdgeList("");
       double score=0;
       nodeScore=0;
       edgeScore=0;
       
       System.out.println("Matched Nodes:"+this.mateList.size()); 
       Iterator it=this.mateList.iterator();
       INMEdge edge=null;
       while(it.hasNext())
       {
           edge=(INMEdge)it.next();
           tmp=this.homogenMap.GetHomogenFlyProteinList(edge.GetFirstVertex().getProteinName());
           if(tmp.isEmpty())
               continue;
           INMNode node=tmp.FindProteinByName(edge.GetSecondVertex().getProteinName());
           if(node!=null)
           {
               nodeScore=node.getProteinProbability();
               homoMateList.AddNewEdge(edge.GetFirstVertex().getProteinName(), 
                       edge.GetSecondVertex().getProteinName(),nodeScore, INMEdge.enumEdgeType.DIRECTION);
           }        
       }
       
       edgeScore=0;
       scoreFile=scoreFile+".mark";
       INMEdge[] homoMateArray=(INMEdge[])homoMateList.toArray();
       int count=homoMateArray.length;
       for(int i=0;i<count;i++)
       {
           for(int j=i+1;j<count;j++)
           {
               INMEdge yeastEdge=new INMEdge(homoMateArray[i].GetFirstVertex(),homoMateArray[j].GetFirstVertex());
               INMEdge flyEdge=new INMEdge(homoMateArray[i].GetSecondVertex(),homoMateArray[j].GetSecondVertex());
               if(yeastNet.IsExistedEdge(yeastEdge)&&flyNet.IsExistedEdge(flyEdge))//这里有问题
               {
                   edgeScore++;
                   tmpMateList.AddNewEdge(homoMateArray[i]);
                   tmpMateList.AddNewEdge(homoMateArray[j]);
               }
           }
       }
       
       nodeScore=0;
       it=tmpMateList.iterator();
       while(it.hasNext())
       {
           edge=(INMEdge)it.next();
           nodeScore+=edge.GetEdgeWeight();
       }
       
       score=nodeScore+edgeScore;
       return score;
   }

   /**
    * read nbm result
    * @param fileName
    * @return
    */
   public boolean ReadNBMResult(String fileName)
   {
       scoreFile=fileName;
       FileStream file=new FileStream(fileName,FileStream.BiologicalFileType.NBM_OUT);
       mateList.clear();
       flyNet.clear();
       yeastNet.clear();
       String firstNode=null,
               secondNode=null,
               edgeType=null;
       if (file.Open(fileName))
         {
           try
             {
               String s = file.getBufferedReader().readLine();
               while (s != null)
                 {
                   s=s.toUpperCase();
                   String[] textLine = s.split("\\s");                  
                   firstNode=textLine[0];
                   edgeType=textLine[1];
                   secondNode=textLine[2];
                   if(edgeType==matchedFix)
                   {
                       mateList.AddNewEdge(firstNode.substring(1), secondNode.substring(1),INMEdge.enumEdgeType.DIRECTION);
                   }else if(edgeType==fly)
                   {
                      flyNet.AddNewEdge(firstNode, secondNode); 
                   }else// 
                   {
                       yeastNet.AddNewEdge(firstNode, secondNode);
                   }
                   s=file.getBufferedReader().readLine();
                 }
               file.Close();
               System.out.println(fileName+" has been read successfully!");
               return true;
             } catch (FileNotFoundException e)
             {
               System.out.println("No such file!");
               return false;
             } catch (IOException e)
             {
               System.out.println("IO exception!");
               return false;
             }
         }
       return false;
   }

}
