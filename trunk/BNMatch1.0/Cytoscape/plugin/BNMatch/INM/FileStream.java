package Cytoscape.plugin.BNMatch.INM;
/**
 *
 * @author YULEI
 */
import Cytoscape.plugin.BNMatch.MainPanel;
import java.io.BufferedReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FileStream 
{

    enum BiologicalFileType{INTERRELATION,SC_DM,MIPS_PC,NBM_OUT,GRP_FILE,KN_D,CFG};
    enum GrpDataType{NODE,EDGE};
    String fileName;
    BiologicalFileType fileType;
    GrpDataType dataType;
    BufferedReader in=null;


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
 * 
 * open file
 * @param fileName
 * @return true or  false
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
            if(s==null)//end of file
                return false;
            s=s.toUpperCase();
            String[] textLine = s.split("\\s");
            String a = null;
            double probability = 0.0f;

            a = "ORFP:" + textLine[0];
            probability = 1.0;
            INMNode node = new INMNode(a, probability);
            proteinSet.add(node);
            for (int i = 1; i < textLine.length; i = i + 2)//read a line
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
 * read data from sif file
 * @param firstNode
 * @param secondNode
 * @param probability
 * @return INMEdge
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
            s=s.toUpperCase();
            String[] textLine=s.split("\\s");
            
            firstNode=textLine[0];
            secondNode=textLine[1];
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
