package Cytoscape.plugin.BNMatch.INM;


import Cytoscape.plugin.BNMatch.MainPanel;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
public class INM 
{    
    public static void runINM()
    {
      HashSet<String> hs=new HashSet<String>();
      int n=0;
      int rn=1;

      
      BufferedReader in=null;
        try
          {
            in = new BufferedReader(new FileReader(Config.getMipsFileName()));//config.ini
            String s=in.readLine().toUpperCase();
            String temp=null;
            while(s!=null)
            {
                String[] strArray=s.split("\\s+");
                if(strArray.length!=0)
                {
                temp=strArray[0];
                }
                else
                {
                System.out.println("File error!");
                return;                    
                }
                hs.add(temp);
                while(temp.contains("."))
                {
                    temp=temp.substring(0,temp.lastIndexOf("."));
                    hs.add(temp);
                }
                s=in.readLine().toUpperCase();
            }
            
            
          } catch (FileNotFoundException e)
          {
              MainPanel.runInformation.append("no such file!\n");//log
          } catch (IOException e)
          {
            MainPanel.runInformation.append("IO exception!\n");
          }     
      
      INMAlgorithm match=new INMAlgorithm();
      INMEdgeList matchNodes=null;
    
      int connectivity=0,num=0,resultNum=0;
      int randNum=Config.getRandNum();//config.ini
      
      for(int randNo=0;randNo<randNum;randNo++)
      {
          if(randNo>0)//参数为0则对果蝇原始图进行匹配
          {
             // match.SetTransformSeed();
              match.FlyNetworkTransform();
          }
          
         Iterator it=hs.iterator();
         String str=null;
         while(it.hasNext())
         {
             str=(String)it.next();
             n++;
             MainPanel.runInformation.append("Network Code:"+str+"\n");
             MainPanel.runInformation.append("---------------------------\n");
             match.InitializeAlgorithm(str);
             connectivity=match.FindYeastConnectivitySubnet(str);//*
             
            while(connectivity!=0)
            {
                if(connectivity>0)
                {
                    num++;
                    MainPanel.runInformation.append("Begin MatchAlgorithm.....\n");
                    resultNum=match.ComputeSim(randNo, num);//计算同源系数矩阵
                    rn=1;
                    matchNodes=match.MatchAlgorithm(rn);
                    match.OutPut(matchNodes, randNo, num, rn);
/*                    while(rn<=resultNum)
                    {
                        matchNodes=match.MatchAlgorithm(rn);
                        match.OutPut(matchNodes, randNo, num, rn);
                        rn++;
                    }*/
                }
                connectivity = match. FindYeastConnectivitySubnet(str);
            }
         }
         MainPanel.runInformation.append("NO."+randNo+" FlyNetwork.Total:"+n+"\n");
         MainPanel.runInformation.append("---------------------------\n");
      }
    } 
}
