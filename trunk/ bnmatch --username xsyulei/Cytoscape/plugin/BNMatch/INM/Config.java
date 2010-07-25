package Cytoscape.plugin.BNMatch.INM;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author YULEI
 */
public class Config 
{
   static String targetFileName;
   static String tableFileName;
   static String mipsFileName;
   static String largeFileName;
   static String matchFileName;
   static String path;
   static int randnum=1;
   static double weight;

   static public void setLargeFileName(String largeFileName)
   {
        Config.largeFileName=largeFileName;
   }
   
   static public void setMipsFileName(String mipsFileName)
   {
       Config.mipsFileName=mipsFileName;
   }
   
   static public void setTableFileName(String tableFileName)
   {
       Config.tableFileName=tableFileName;
   }
   
   static public void setTargetFileName(String targetFileName)
   {
       Config.targetFileName=targetFileName;
   }
   
   static public void setMatchFileName(String matchFileName)
   {
       Config.matchFileName=matchFileName;
   }
   
   static public void setRandNum(int randnum)
   {
       Config.randnum=randnum;
   }
   
   static public void setWeight(double weight)
   {
       Config.weight=weight;
   }
   
   static String getLargeFileName()
   {
       return largeFileName;
   }
   
   static String getMipsFileName()
   {
       return mipsFileName;
   }
   
   static String getTableFileName()
   {
       return tableFileName;
   }
   
   static String getTargetFileName()
   {
       return targetFileName;
   }
   
   static String getMatchFileName()
   {
       return matchFileName;
   }
   
   static int getRandNum()
   {
       return randnum;
   }
   
   static double getWeight()
   {
       return weight;
   }
}
