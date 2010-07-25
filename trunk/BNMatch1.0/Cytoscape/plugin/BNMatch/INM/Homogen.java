package Cytoscape.plugin.BNMatch.INM;


import java.util.Collections;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Iterator;

/**
 *
 * @author e467941
 */
public class Homogen implements Cloneable
{
    enum enumErrorCode{UNDEFINEDID,UNDEFINEDSCORE};
    String yeastProteinName;// 酵母蛋白质名称
    int ID;
    int score;
    INMNodeList homogenList;// 果蝇的同源蛋白质列表
       
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
     * 添加一个新的同源蛋白质，若已经存在，则保存概率大的对象
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
     * 移除名为 strProteinName 的所有同源果蝇蛋白质
     * @param proteinName
     * @param probability
     */
    public void RemoveHomogenFlyProtein(String proteinName,double probability)
    {
        homogenList.RemoveProtein(proteinName, probability);
    }

    /**
     * 获取所有同源果蝇蛋白质列表
     * @return
     */
    public INMNodeList GetHomogenFlyProteinList()
    {
        return homogenList;
    }
 
/**
 * 检查是否存在名为 strProteinName 的同源果蝇蛋白质,采用二分法搜索
 * @param proteinName
 * @return
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
 * 在调用前，确保果蝇的同源蛋白质序列是有序的
 */
    public void SortHomogenFlyProteinList()
    {
        homogenList.SortNodeList();
    }
}
