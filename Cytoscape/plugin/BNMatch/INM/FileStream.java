package Cytoscape.plugin.BNMatch.INM;


import Cytoscape.plugin.BNMatch.MainPanel;
import java.io.BufferedReader;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author YULEI
 */
public class FileStream 
{

    enum BiologicalFileType{INTERRELATION,SC_DM,MIPS_PC,NBM_OUT,GRP_FILE,KN_D,CFG};
    enum GrpDataType{NODE,EDGE};
    String fileName;
    BiologicalFileType fileType;
    GrpDataType dataType;
    BufferedReader in=null;

/**
 * 四个构造器对应C++中一个有缺省值的构造函数
 */
    FileStream()
    {
        this.fileName="";
        this.fileType=BiologicalFileType.INTERRELATION;
        if(BiologicalFileType.GRP_FILE==this.fileType)
            this.dataType=GrpDataType.NODE;    
    }
    
    FileStream(String fileName,BiologicalFileType fileType)
    {
        this.fileName=fileName;
        this.fileType=fileType;
        if(BiologicalFileType.GRP_FILE==this.fileType)
            this.dataType=GrpDataType.NODE;
    }
    
    FileStream(String fileName)
    {
        this.fileName=fileName;
        this.fileType=BiologicalFileType.INTERRELATION;
        if(BiologicalFileType.GRP_FILE==this.fileType)
        this.dataType=GrpDataType.NODE;
    }
    
    FileStream(BiologicalFileType fileType)
    {
        this.fileName="";
        this.fileType=fileType;
        if(BiologicalFileType.GRP_FILE==this.fileType)
        this.dataType=GrpDataType.NODE;        
    }
/**
 * 打开文件。若打开失败，返回 false， 否则返回 true
 * @param fileName
 * @return
 */    
    public boolean Open(String fileName)
    {
        if(fileName.isEmpty())
        {
            if(this.fileName.isEmpty())
                return false;
        }
        else
        {
            this.fileName=fileName;
        }
       try
       { 
        in=new BufferedReader(new FileReader(this.fileName));
        return true;
       }
       catch(FileNotFoundException e)
       {
           MainPanel.runInformation.append("no such file!\n");
           return false;
       }
       catch(IOException e)
       {
	   MainPanel.runInformation.append("IO exception!\n");
           return false;
        }        
    }
    
    public void Close()
    {
        try
        {
           in.close();
        } catch (IOException ex)
        {
            Logger.getLogger(FileStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public BufferedReader getBufferedReader()
    {
        return in;
    }
    
    public boolean ReadNextSC_DMObject(int id,int score,ArrayList<INMNode> proteinSet)
    {
        try
          {
            String s = getBufferedReader().readLine();
            if(s==null)//到文件结尾
                return false;
            
            String[] textLine = s.split("\\s");//空格分开
            String a = null;
            double probability = 0.0f;

            a = "ORFP:" + textLine[0];
            probability = 1.0;
            INMNode node = new INMNode(a, probability);
            proteinSet.add(node);
            for (int i = 1; i < textLine.length; i = i + 2)//读入一行数据
              {
                a = textLine[i];
                probability = Double.valueOf(textLine[i + 1]);
                node = new INMNode(a, probability);
                proteinSet.add(node);
              }
            return true;
          } catch (FileNotFoundException e)
          {
            MainPanel.runInformation.append("no such file!\n");
            return false;
          } catch (IOException e)
          {
            MainPanel.runInformation.append("IO exception!\n");
            return false;
          }
    }

    
/**
 * 
 * @param firstNode
 * @param secondNode
 * @param probability
 * @return
 */
    public INMEdge ReadNextInterrelationObject()
    {
        String firstNode="";
        String secondNode="";
        Double probability=0.0d;
        INMEdge edge=null;
        try
        {
            String s=getBufferedReader().readLine();
            if(s==null)
                return null;
            
            String[] textLine=s.split("\\s");
            
            firstNode=textLine[0].toUpperCase();
            secondNode=textLine[1].toUpperCase();
            probability=Double.valueOf(textLine[2]);
            edge=new INMEdge(firstNode,secondNode,probability);
            return edge;
        }catch(FileNotFoundException e)
        {
            MainPanel.runInformation.append("no such file!\n");
            return null;
          } catch (IOException e)
          {
            MainPanel.runInformation.append("IO exception!\n");
            return null;
          }
    }    
}
