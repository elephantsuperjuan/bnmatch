package Cytoscape.plugin.BNMatch.INM;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author YULEI
 */
public class INMNodeList extends LinkedList
{
  
/**
 * 该算法采用顺序查找，代价比较高昂， 不建议采用
 * 若有必要，建议对该列表排序，采用二分查找，可能效率更高
 * @param proteinName
 * @return
 */    
public INMNode FindProteinByName(String proteinName)
{
    INMNode tmp = new INMNode(proteinName);
    int pos = this.indexOf(tmp);
    if (pos != -1)
    {
        return (INMNode) this.get(pos);
    } else
    {
        return null;
    }
}



/**
 * 添加一个名为 protein 的蛋白质，若该蛋白质已经存在，则保存概率大的蛋白质对象
 * @param objProtein
 */
public void AddNewProtein(INMNode protein)
{
       int index=indexOf(protein);
       INMNode node=null;
       if(-1==index)
       {
           node=new INMNode("");
           node=(INMNode)protein.clone();
           add(node);
       }
       else
       {
         node=(INMNode)get(index);
        if(node.getProteinProbability()<protein.getProteinProbability())
            node.setProteinProbability(protein.getProteinProbability());
       }
}

public void AddNewProtein(String proteinName,double probability)
{
    INMNode node=new INMNode(proteinName,probability);
    AddNewProtein(node);
}
/**
 * 移除所有名称与 protein 相同的蛋白质
 * @param protein
 */
public  void RemoveProtein(INMNode protein)
{
    Collection<INMNode> c=new LinkedList<INMNode>();
    c.add(protein);
    this.removeAll(c);
}

public void RemoveProtein(String proteinName,double probability)
{
    INMNode node=new INMNode(proteinName,probability); 
    this.RemoveProtein(node);
}

/**
 * 节点序列排序
 */
public void SortNodeList()
{
    Collections.sort(this,new Comparator<INMNode>()
    {
        public int compare(INMNode node0,INMNode node1)
        {
            return node0.getProteinName().compareTo(node1.getProteinName());//sort the node according to its name
        }
    });
}


public void SortedAddProtein(String proteinName,double probability)
{
    INMNode node=new INMNode(proteinName,probability);
    SortedAddProtein(node);
}

public void SortedAddProtein(INMNode protein)
{
   int pos= Collections.binarySearch(this, protein, new Comparator<INMNode>()
        {

            public int compare(INMNode node0, INMNode node1)
            {
                return node0.getProteinName().compareTo(node1.getProteinName());
            }
        });//pos,the position to insert the element
  
    if(pos>=0)
        this.add(pos, protein);
    else
        this.add(-pos-1,protein);       
}

public boolean isExistProtein(INMNode node)
{
    return contains(node);
}

public void AddProteinByP(INMNode protein)
{
    double p = protein.getProteinProbability();
    int index = 0;
    INMNode node=null;
    if (this.isEmpty())
      {
        add(index, protein);
      } else
      {
        Iterator it = iterator();
        while (it.hasNext())
          {
            node = (INMNode) it.next();
            if (node.getProteinProbability() > p)
            {
                index++;
               continue;
            }
            else
            {   
                break;
            }
          }      
         add(index, protein);
      }
}

public INMNode GetProtein(int index)
{
    return (INMNode)get(index);
}
}
