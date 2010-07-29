package Cytoscape.plugin.BNMatch.INM;
/**
 *
 * @author YULEI
 */
public class INMNode implements Cloneable
{
    protected String proteinName;
    protected double probability;
    protected String layerCode;
    protected int degree;
    protected int index;
    public enum enumProteinProperty {NAME,PROBABILITY}


    INMNode(String proteinName)
    {
        double probability=0.0d;
        init(proteinName,probability);       
    } 
    
    INMNode(double probability)
    {
        String proteinName="";
        init(proteinName,probability);
    }

     INMNode(String proteinName,double probability)
    {
        init(proteinName,probability); 
    }

    @Override
    public int hashCode()
    {
        return this.proteinName.hashCode();
    }
    
    @Override
    public boolean equals(Object o)
    {
        INMNode node=(INMNode)o;
        return this.proteinName.equals(node.proteinName);
    }
     
    @Override
    public String toString()
    {
        return this.getProteinName();
    }
    
    @Override
    public Object clone()
    {
        INMNode o=null;
        try
        {
            o=(INMNode)super.clone();//影子克隆
        }catch(CloneNotSupportedException e)
        {
            e.printStackTrace();
        }
        return o;
    }
    public void init(String proteinName,double probability)
    {
        this.proteinName=proteinName;
        this.probability=probability;  
    }
    
    public void setProteinProperty(enumProteinProperty propertyType,String value)
    {
        switch(propertyType)
        {
            case PROBABILITY:
                probability=Double.valueOf(value);break;
            case NAME:
                proteinName=value;break;
            default:
                break;
        }
    }
    
    public String getProteinProperty(enumProteinProperty propertyType)
    {
        switch(propertyType)
        {
            case PROBABILITY:
            return String.valueOf(probability);
            case NAME:
            return proteinName;
            default:
                return null;
        }
    }
    
    public void setProteinName(String proteinName)
    {
        this.proteinName=proteinName;
    }
    
    public String getProteinName()
    {
        return proteinName;
    }
    
    public void setProteinProbability(double probability)
    {
        this.probability=probability;
    }
    
    public double getProteinProbability()
    {
        return probability;
    }
    public void setLayerCode(String layerCode)
    {
     this.layerCode=layerCode;   
    }
    
    public String getLayerCode()
    {
        return layerCode;
    }
    
    public void setDegree(int degree)
    {
        this.degree=degree;
    }
   
    public int getDegree()
    {
        return degree;
    }
    
    public void setIndex(int index)
    {
        this.index=index;
    }
    
    public int getIndex()
    {
        return index;
    }
}
