package Cytoscape.plugin.BNMatch.INM;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author YULEI
 */
public class INMScore 
{
    String matchedFix;// NBM算法的结果，匹配的蛋白质类型
    String yeast;// 酵母蛋白质类型
    String fly;// 果蝇蛋白质类型
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
     * 为匹配结果打分
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
       
       //////对同源顶点进行打分//////
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
       //////对具有同源点的边进行打分//////
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
                   //这里存文件省略
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
           //这里存文件省略
       }
       
       score=nodeScore+edgeScore;
       return score;
   }

   /**
    * 读取NBM算法的匹配结果
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
               String s = file.getBufferedReader().readLine().toUpperCase();
               while (s != null)
                 {
                   String[] textLine = s.split("\\s");
                   
                   firstNode=textLine[0];
                   edgeType=textLine[1];
                   secondNode=textLine[2];
                   if(edgeType==matchedFix)//该行是匹配的蛋白质
                   {
                       mateList.AddNewEdge(firstNode.substring(1), secondNode.substring(1),INMEdge.enumEdgeType.DIRECTION);
                   }else if(edgeType==fly)// 该行是果蝇蛋白质的边
                   {
                      flyNet.AddNewEdge(firstNode, secondNode); 
                   }else// 该行是酵母蛋白质的边
                   {
                       yeastNet.AddNewEdge(firstNode, secondNode);
                   }
                   s=file.getBufferedReader().readLine().toUpperCase();
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
