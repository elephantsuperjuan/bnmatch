package Cytoscape.plugin.BNMatch.INM;
/**
 *
 * @author YuLei
 */
import java.util.Collections;
import java.util.Iterator;


public class Homogen implements Cloneable
{
    enum enumErrorCode{UNDEFINEDID,UNDEFINEDSCORE};
    String yeastProteinName;// name of yeast protein
    int ID;
    int score;
    INMNodeList homogenList;//list of fruit fly homologous protein
       
    Homogen(String yeastProteinName)
    {
        this.yeastProteinName=yeastProteinName;
        homogenList=new INMNodeList();
        ID=-1;
        score=-2;
    }
    
    @Override
    public int hashCode()
    {
        return GetYeastProteinName().hashCode();
    }
    
    @Override
    public boolean equals(Object o)
    {
        Homogen homogen=(Homogen)o;
        return GetYeastProteinName().equals(homogen.GetYeastProteinName());
    }
    
    @Override
    public String toString()
    {
        return GetYeastProteinName();
    }
    
    @Override
    public Object clone()
    {
        Homogen o=null;
        try
        {
            o=(Homogen)super.clone();
            o.homogenList=new INMNodeList();
            Iterator it=homogenList.iterator();
            while(it.hasNext())
            {
                INMNode node=(INMNode)((INMNode)it.next()).clone();
                o.homogenList.add(node);
            }
        }catch(CloneNotSupportedException e){
            e.printStackTrace();
        }
        return o;
    }
    public void SetYeastProteinName(String yeastProteinName)
    {
        this.yeastProteinName=yeastProteinName;
    }
    
    public String GetYeastProteinName()
    {
        return this.yeastProteinName;
    }
    
    public void SetYeastProteinID(int ID)
    {
        this.ID=ID;
    }
    
    public int GetYeastProteinID()
    {
        return this.ID;
    }
    
    public void SetYeastProteinScore(int score)
    {
        this.score=score;
    }
    
    public int GetYeastProteinScore()
    {
        return this.score;
    }

    /**
     * add a new homologous proteins, if already exists, then the probability of large objects stored
     * @param proteinName
     * @param probability
     */
    public void AddHomogenFlyProtein(String proteinName,double probability)
    {
        homogenList.AddNewProtein(proteinName, probability);
    }
    
    public void AddHomogenFlyProtein(INMNode flyProtein)
    {
        homogenList.AddNewProtein(flyProtein);
    }

    /**
     * remove all known strProteinName homologous protein of fruit fly
     * @param proteinName
     * @param probability
     */
    public void RemoveHomogenFlyProtein(String proteinName,double probability)
    {
        homogenList.RemoveProtein(proteinName, probability);
    }

    /**
     * get all lists of homologous protein of fruit fly
     * @return
     */
    public INMNodeList GetHomogenFlyProteinList()
    {
        return homogenList;
    }
 
/**
 * check for the homologous Drosophila protein called strProteinName, and use binary search
 * @param proteinName
 * @return boolean
 */    
    public boolean IsExistedHomogenFlyProtein(String proteinName)
    {
        INMNode node=new INMNode(proteinName);
        int index=Collections.binarySearch(homogenList, node);
        if(index>=0)
        return true;
        else 
            return false;
    }

/**
 * Before the call, it is to ensure that the homologous Drosophila protein sequence is ordered
 */
    public void SortHomogenFlyProteinList()
    {
        homogenList.SortNodeList();
    }
}
