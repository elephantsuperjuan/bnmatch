package Cytoscape.plugin.BNMatch.INM;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author e467941
 */
public class HomogenMap implements Cloneable
{
    static char prefix=':';// 蛋白质的前缀分隔符
    static char postfix='-';// 果蝇蛋白质后缀分隔符
    static char yeast='Y';// 酵母蛋白质首字母
    static char fly='C';// 果蝇蛋白质首字母
    
    LinkedList<Homogen> homogenMapList;// 同源蛋白质映射表 java中linkedlist==c++中list
    
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
            o=(HomogenMap)super.clone();//影子克隆
            o.homogenMapList=new LinkedList<Homogen>();//将clone进行到底
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
 * 加入一列同源蛋白质列表，若该项在表中已经存在，则比较各同源蛋白质，并保存概率大的数值
 * @param homogen
 */
    public void AddHomogenColumn(Homogen homogen)
    {
        int pos=Collections.binarySearch(homogenMapList, homogen,
                new Comparator<Homogen>()
        {
            public int compare(Homogen h1,Homogen h2)
            {
                return h1.GetYeastProteinName().compareTo(h2.GetYeastProteinName());//?
            }
        });
        
        if(pos>=0)//有相等的值
        {
            Iterator it=homogen.GetHomogenFlyProteinList().iterator();// 取 objHomogen 中果蝇蛋白质数据
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

/**
 * 调用 XJFileStream，读取文件中所有的酵母同源蛋白质数据，并创建同源蛋白质结构
 * ReadSC_DMObject()只在这里被调用
 */
    public void RetrieveYeastHomogenTable()
    {
        FileStream file=new FileStream(homogenFileName,FileStream.BiologicalFileType.SC_DM);
        if (file.Open(homogenFileName))
          {
            int id = 0, score = 0;
            ArrayList<INMNode> rawProteinSet = new ArrayList<INMNode>();
            while (file.ReadNextSC_DMObject(id, score, rawProteinSet))
              {
                ParseAddYeastHomogen(id, score, rawProteinSet);// 解析酵母数据，并将其加入到列表中
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
 * 解析酵母数据，并将其加入到列表中。如果数据文件中数据不正确，该解析将抛出异常
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
        while(it.hasNext())// 首先找出所有酵母蛋白质名称，并去除其中的前置符号（如 ORFP 等）以及果蝇蛋白质的后缀
        {
            node=(INMNode)it.next();
            strName=node.getProteinName();
            int pos=strName.indexOf(prefix);
            if(pos!=-1)//找到了
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
    	    // 将同源数据加入到映射表中        
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
                    +"\tname: "+hg.GetYeastProteinName());// 打印酵母蛋白质 ID、 Score 及名称
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
/**
 * 输出Blast格式文件 省略
 */   
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

/**
 * 移除酵母 strYeastName 蛋白质的同源果蝇蛋白质 strFlyName
 * @param yeastName
 * @param flyName
 */
    public void RemoveHomogenFlyProtein(String yeastName,String flyName)
    {
        Homogen tmp=new Homogen(yeastName);
        int pos=homogenMapList.indexOf(tmp);
        if(pos!=-1)
        {
            homogenMapList.get(pos).RemoveHomogenFlyProtein(flyName, 0);
        }
    }

/**
 * 
 * @param yeastName
 * @param flyProtein
 */
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
