package Cytoscape.plugin.BNMatch.INM;
/**
 *
 * @author YuLei
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;


public class HomogenMap implements Cloneable
{
    static char prefix=':';//protein prefix separator
    static char postfix='-';//fruit fly protein suffix separator
    static char yeast='Y';//the first letter of the yeast protein
    static char fly='C';//the first letter of the fruit fly protein
    
    LinkedList<Homogen> homogenMapList;//Homologous protein map
    
    String homogenFileName;
    
    HomogenMap(String homogenFileName)
    {
        this.homogenFileName=homogenFileName;
        homogenMapList=new LinkedList<Homogen>();
        RetrieveYeastHomogenTable();
    }
    
    @Override
    public Object clone()
    {
        HomogenMap o=null;
        try
        {
            o=(HomogenMap)super.clone();
            o.homogenMapList=new LinkedList<Homogen>();
            Iterator it=homogenMapList.iterator();
            while(it.hasNext())
            {
                Homogen temp=(Homogen)((Homogen)it.next()).clone();
                o.homogenMapList.add(temp);
            }
        }catch(CloneNotSupportedException e)
        {
            e.printStackTrace();
        }
        return o;
    }    

/**
 * join a list of homologous proteins, if that already exists in the table, then the comparison of homologous proteins, and save the probability of large values
 * @param homogen
 */
    public void AddHomogenColumn(Homogen homogen)
    {
        int pos=Collections.binarySearch(homogenMapList, homogen,
                new Comparator<Homogen>()
        {
            public int compare(Homogen h1,Homogen h2)
            {
                return h1.GetYeastProteinName().compareTo(h2.GetYeastProteinName());//
            }
        });
        
        if(pos>=0)
        {
            Iterator it=homogen.GetHomogenFlyProteinList().iterator();
            INMNode node=null;
            while(it.hasNext())
            {
                node=(INMNode)it.next();
                homogenMapList.get(pos).AddHomogenFlyProtein(node);
            }
        }else
        {
            homogenMapList.add(-pos-1, homogen);
        }
        
    }


    public void RetrieveYeastHomogenTable()
    {
        FileStream file=new FileStream(homogenFileName,FileStream.BiologicalFileType.SC_DM);
        if (file.Open(homogenFileName))
          {
            int id = 0, score = 0;
            ArrayList<INMNode> rawProteinSet = new ArrayList<INMNode>();
            while (file.ReadNextSC_DMObject(id, score, rawProteinSet))
              {
                ParseAddYeastHomogen(id, score, rawProteinSet);//Analysis of yeast data, and added to the list
                rawProteinSet.clear();
              }
          }
       // file.Close();
    }
    
    public void ReadHomogenTable(String homogenFileName)
    {
        this.homogenFileName=homogenFileName;
        homogenMapList.clear();
        RetrieveYeastHomogenTable();
    }
    
/**
 * @param id
 * @param score
 */ 
    public void ParseAddYeastHomogen(int id, int score, ArrayList<INMNode> rawProteinSet)
    {
        ArrayList<String> strYeast=new ArrayList<String>();
        ArrayList<Double> fYeast =new ArrayList<Double>();
        ArrayList<String> strFly=new ArrayList<String>();
        ArrayList<Double> fFly=new ArrayList<Double>();
        
        Iterator it=rawProteinSet.iterator();
        INMNode node=null;
        String tmp=null;
        String strName=null;
        while(it.hasNext())//First find out the name of all yeast proteins, and remove one of the pre-symbol (such as ORFP, etc.) and the suffix protein of fruit fly
        {
            node=(INMNode)it.next();
            strName=node.getProteinName();
            int pos=strName.indexOf(prefix);
            if(pos!=-1)//find it
            {
                tmp=strName.substring(pos+1);
                strName=tmp;
                strYeast.add(strName);
                fYeast.add(node.getProteinProbability());//autoboxing                
            }else
            {
             pos=strName.indexOf(postfix);
             if(pos!=-1)
             {
                 tmp=strName.substring(0,pos);
                 strName=tmp;
             }
             strFly.add(strName);
             fFly.add(node.getProteinProbability());
            }
         }     
    	    //add the source data to mapping table        
            for(int i=0;i<strYeast.size();i++)
            {
                Homogen homo=new Homogen("");
                homo.SetYeastProteinID(id);
                homo.SetYeastProteinName(strYeast.get(i));
                homo.SetYeastProteinScore(score);
                for(int j=0;j<strFly.size();j++)
                    homo.AddHomogenFlyProtein(strFly.get(j),fFly.get(j)*fYeast.get(i));
                AddHomogenColumn(homo);
            }                 
    }
    
    public void PrintAllData()
    {
        Iterator it=homogenMapList.iterator();
        Homogen hg=null;
        INMNode node=null;
        while(it.hasNext())
        {
            hg=(Homogen)it.next();
            System.out.println("ID= "+hg.GetYeastProteinID()+"\tscore= "+hg.GetYeastProteinScore()
                    +"\tname: "+hg.GetYeastProteinName());//Print yeast protein ID, Score and Name
            Iterator iter=hg.GetHomogenFlyProteinList().iterator();
            while(iter.hasNext())
            {
                node=(INMNode)iter.next();
                System.out.println("Fly name: "+node.getProteinName()+"\tProbability: "
                        +node.getProteinProbability());
            }
             System.out.print("\n");
        }
        
    }
   
    public void TableToBlast()
    {       
    }
    
    public INMNodeList GetHomogenFlyProteinList(String yeastName)
    {
        Homogen tmp=new Homogen(yeastName);
        int pos=homogenMapList.indexOf(tmp);
        if(pos!=-1)
        {
            return homogenMapList.get(pos).GetHomogenFlyProteinList();
        }
        return null;
    }


    public void RemoveHomogenFlyProtein(String yeastName,String flyName)
    {
        Homogen tmp=new Homogen(yeastName);
        int pos=homogenMapList.indexOf(tmp);
        if(pos!=-1)
        {
            homogenMapList.get(pos).RemoveHomogenFlyProtein(flyName, 0);
        }
    }


    public void AddHomogenFlyPotein(String yeastName,INMNode flyProtein)
    {
        Homogen tmp=new Homogen(yeastName);
        int pos=homogenMapList.indexOf(tmp);
        if(pos!=-1)
        {
            homogenMapList.get(pos).AddHomogenFlyProtein(flyProtein);
        }
    }

}
