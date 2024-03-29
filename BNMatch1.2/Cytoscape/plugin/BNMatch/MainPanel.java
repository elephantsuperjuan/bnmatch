package Cytoscape.plugin.BNMatch;
/**
 *
 * @author YULEI
 */
import Cytoscape.plugin.BNMatch.INM.Config;
import cytoscape.Cytoscape;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


public class MainPanel extends JPanel 
{
    BNMatchPlugin.MainPanelAction trigger;
    JPanel setParametersPanel;
    JTextField 
            dirFileName1=new JTextField(20),
            dirFileName2=new JTextField(20),
            dirFileName3=new JTextField(20),
            dirFileName4=new JTextField(20),
            dirFileName5=new JTextField(20);
    static  JTextField weightField=new JTextField(4);
    AnalyzeAction analyzeAction;
    static JButton analyzeButton = new JButton("Analyze");
    static JCheckBox colorWeight=new JCheckBox("Color Weight");
    static JCheckBox ShowCommonOnly=new JCheckBox("Show Common Nodes Only");
    static JCheckBox directedGraph=new JCheckBox("Directed");
    static JButton resetButton=new JButton("Reset");
    static JCheckBox refreshImmediately=new JCheckBox("Refresh Immediately");
    static JButton refreshButton=new JButton("Refresh");
    
    static JRadioButton extenalSIFFile;
    static JRadioButton byBNMatch;
    public static JRadioButton INMAlgorithm;
    public static JRadioButton NBMAlgorithm;
    JPanel selectSIFSourcePanel=null;
    JPanel selectAlgorithmPanel =null;
    JPanel algorithmPanel=new JPanel();
    JPanel siffilePanel=new JPanel();
    private JSplitPane splitPane;
    static public JTextArea runInformation;
    static String str1,str2,str3,str4,str5;
    static AnalyzeAction mAnalyzeAction=new AnalyzeAction();

    MainPanel(BNMatchPlugin.MainPanelAction trigger) 
    {
        this.trigger = trigger;
        runInformation=new JTextArea();
        runInformation.setEditable(false);
        JScrollPane infoScrollPane=new JScrollPane(runInformation);
        infoScrollPane.getVerticalScrollBar().setUnitIncrement(20);//mouse move unit
        infoScrollPane.setBorder(BorderFactory.createTitledBorder("Log"));
        JPanel p=new JPanel();
        p.setLayout(new BorderLayout());
        
        selectSIFSourcePanel=selectSIFSourcePanel();
        

        selectAlgorithmPanel = selectAlgorithmPanel();
        setParametersPanel=setParametersPanel(); 
 
        algorithmPanel.setLayout(new BorderLayout());
        algorithmPanel.add(selectAlgorithmPanel,BorderLayout.NORTH);
        algorithmPanel.add(setParametersPanel,BorderLayout.CENTER);
        algorithmPanel.setVisible(false); 
        
        p.add(selectSIFSourcePanel,BorderLayout.NORTH);
        p.add(algorithmPanel,BorderLayout.CENTER);
        p.add(operationPanel(),BorderLayout.SOUTH);
        
        JScrollPane operScrollPane=new JScrollPane(p);
        operScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                   operScrollPane, infoScrollPane);
        operScrollPane.setMinimumSize(new Dimension(310,260));
        operScrollPane.setMaximumSize(new Dimension(310,650));
        infoScrollPane.setMinimumSize(new Dimension(310, 60));
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(260);
        splitPane.setPreferredSize(new Dimension(310, 580));
        add(splitPane);       

        /*disabled before analyze execution*/
        colorWeight.setEnabled(false);
        directedGraph.setSelected(true);
        resetButton.setEnabled(false);
        refreshImmediately.setEnabled(false);
        refreshButton.setEnabled(false);
     }

   
    private JPanel selectSIFSourcePanel() 
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("SIF"));

        extenalSIFFile = new JRadioButton("external sif file",true);
        byBNMatch = new JRadioButton("generated by BNMatch",false);
        extenalSIFFile.addActionListener(new AlgorithmPanel());
        byBNMatch.addActionListener(new AlgorithmPanel());
        ButtonGroup scopeOptions = new ButtonGroup();
        scopeOptions.add(extenalSIFFile);
        scopeOptions.add(byBNMatch);
        
        panel.add(extenalSIFFile);
        panel.add(byBNMatch);
        panel.setToolTipText("Please select the source of sif file");
        return panel;
    }

    private JPanel selectAlgorithmPanel() 
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Algorithm"));

        INMAlgorithm = new JRadioButton("INM",true);
        NBMAlgorithm = new JRadioButton("NBM",false);

        ButtonGroup scopeOptions = new ButtonGroup();
        scopeOptions.add(INMAlgorithm);
        scopeOptions.add(NBMAlgorithm);
        panel.add(INMAlgorithm);
        panel.add(NBMAlgorithm);
        //panel.setPreferredSize(new java.awt.Dimension(185, 164));
        panel.setToolTipText("Please select a search algorithm");
        return panel;
    }

   private JPanel setParametersPanel() 
   {
        JPanel filesPanel = new JPanel();
        File directory = new File(""); 
        JFileChooser chooser=null;
        try
        {
        chooser = new JFileChooser(directory.getCanonicalPath());
        }catch(Exception e)
        {
            
        }
        
        filesPanel.setLayout(new BoxLayout(filesPanel, BoxLayout.Y_AXIS));
        filesPanel.setBorder(BorderFactory.createTitledBorder("Set Parameters"));
        JLabel jLabel1=new JLabel("Proteins:");
        JLabel jLabel2=new JLabel("Interactions of one species:");
        JLabel jLabel3=new JLabel("Interactions of another species:");
        JLabel jLabel4=new JLabel("Homologous file:");
        JLabel jLabel5=new JLabel("Proteins assigned to match (file):");
        JLabel jLabel6=new JLabel("weight:");   
        
        JPanel weightPanel=new JPanel();
        weightPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        weightPanel.add(jLabel6);
        weightField.setText("0.1");
        weightPanel.add(weightField);
        
        JButton openFileButton1,openFileButton2,openFileButton3,openFileButton4,openFileButton5;
        openFileButton1=new JButton("open");
        openFileButton2=new JButton("open");
        openFileButton3=new JButton("open");
        openFileButton4=new JButton("open");
        openFileButton5=new JButton("open");
        
        
        openFileButton1.setToolTipText("open the proteins file of target PIN (Protein-Protein Interaction Network) of one species");
        openFileButton2.setToolTipText("open the interactions file of target PIN (Protein-Protein Interaction Network) of one species");
        openFileButton3.setToolTipText("open file of genome PIN of another species");
        openFileButton4.setToolTipText("open homologous file");
        openFileButton5.setToolTipText("open proteins assigned to match file");
       
        openFileButton1.setActionCommand("1");
        openFileButton2.setActionCommand("2");
        openFileButton3.setActionCommand("3");
        openFileButton4.setActionCommand("4");
        openFileButton5.setActionCommand("5");
       
        dirFileName1.setAlignmentX(Component.LEFT_ALIGNMENT);
        openFileButton1.setAlignmentX(Component.LEFT_ALIGNMENT);
        dirFileName2.setAlignmentX(Component.LEFT_ALIGNMENT);
        openFileButton2.setAlignmentX(Component.LEFT_ALIGNMENT);
        dirFileName3.setAlignmentX(Component.LEFT_ALIGNMENT);
        openFileButton3.setAlignmentX(Component.LEFT_ALIGNMENT);
        dirFileName4.setAlignmentX(Component.LEFT_ALIGNMENT);
        openFileButton4.setAlignmentX(Component.LEFT_ALIGNMENT);
        dirFileName5.setAlignmentX(Component.LEFT_ALIGNMENT);
        openFileButton5.setAlignmentX(Component.LEFT_ALIGNMENT);
        weightPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        openFileButton1.addActionListener(new FilesAction(chooser));
        openFileButton2.addActionListener(new FilesAction(chooser));
        openFileButton3.addActionListener(new FilesAction(chooser));
        openFileButton4.addActionListener(new FilesAction(chooser));
        openFileButton5.addActionListener(new FilesAction(chooser));

        filesPanel.add(jLabel1);
        filesPanel.add(dirFileName1);
        filesPanel.add(openFileButton1);
        filesPanel.add(jLabel2);
        filesPanel.add(dirFileName2);
        filesPanel.add(openFileButton2);
        filesPanel.add(jLabel3);
        filesPanel.add(dirFileName3);
        filesPanel.add(openFileButton3);
        filesPanel.add(jLabel4);
        filesPanel.add(dirFileName4);
        filesPanel.add(openFileButton4);
        filesPanel.add(jLabel5);
        filesPanel.add(dirFileName5);
        filesPanel.add(openFileButton5);
        filesPanel.add(weightPanel);
        
        dirFileName1.setEditable(false);
        dirFileName2.setEditable(false);
        dirFileName3.setEditable(false);
        dirFileName4.setEditable(false); 
        dirFileName5.setEditable(false);  
        
        return filesPanel;
    } 


   private JPanel operationPanel() 
   {
        JPanel operationPanel=new JPanel();
        operationPanel.setLayout(new BoxLayout(operationPanel,BoxLayout.Y_AXIS));
        operationPanel.setBorder(BorderFactory.createTitledBorder("Operations"));
        
        JPanel analyzePanel = new JPanel();
        analyzePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        
        JPanel refreshPanel=new JPanel();
        refreshPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JPanel refreshWrapper=new JPanel();
        refreshWrapper.setLayout(new BoxLayout(refreshWrapper,BoxLayout.Y_AXIS));

        JPanel panelClose=new JPanel();
        panelClose.setLayout(new FlowLayout(FlowLayout.LEFT));


        analyzeButton.setToolTipText("start the process of analyze");

        analyzeButton.addActionListener(mAnalyzeAction);        
        analyzePanel.add(analyzeButton);
               
        colorWeight.setToolTipText("If checked,color the match nodes according to the digitals");
        colorWeight.addActionListener(new ColorWeightAction());
        analyzePanel.add(colorWeight);        
        
        ShowCommonOnly.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				mAnalyzeAction.changeVisualState();
			}
        	
        });
        analyzePanel.add(ShowCommonOnly);     
        
        directedGraph.setToolTipText("If not checked,all edges are supposed undirected");
        analyzePanel.add(directedGraph);
        
        resetButton.setToolTipText("reset the layouts of the network views");
        resetButton.addActionListener(new ResetAction());

        refreshImmediately.setToolTipText("refresh the match nodes in target network view immediately");
        refreshImmediately.addActionListener(new RefreshAction(false));
        refreshImmediately.setLayout(new FlowLayout(FlowLayout.LEFT));
        

        refreshButton.setToolTipText("refresh the match nodes in target network view");
        refreshButton.addActionListener(new RefreshAction(true));
        refreshButton.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        refreshWrapper.add(refreshButton);
        refreshWrapper.add(resetButton);
        refreshWrapper.add(refreshImmediately);
      

        refreshPanel.add(refreshWrapper);
        
        JButton closeButton = new JButton("Close");
        closeButton.setToolTipText("terminate the plugin");
        panelClose.add(closeButton);
        closeButton.addActionListener(new MainPanel.CloseAction(this));
        
        operationPanel.add(analyzePanel);
        operationPanel.add(refreshPanel);
        operationPanel.add(panelClose);
        return operationPanel;
 
    }

    class AlgorithmPanel extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if(extenalSIFFile.isSelected())
            {
                 algorithmPanel.setVisible(false);
            }else
            {
                 algorithmPanel.setVisible(true);
            }
        }      
    }
    
    private class FilesAction implements ActionListener
    {

        JFileChooser chooser;
        
        FilesAction(JFileChooser chooser)
        {
          this.chooser=chooser;
        }
        
        public void actionPerformed(ActionEvent e) 
        {
    int returnVal = chooser.showOpenDialog(MainPanel.this);
    if(returnVal == JFileChooser.APPROVE_OPTION) 
    {
        if("1" ==e.getActionCommand())
      {
       str1=chooser.getCurrentDirectory().toString()+"\\"+chooser.getSelectedFile().getName();

       Config.setMipsFileName(str1);
       dirFileName1.setText(str1);
      }
        if("2"==e.getActionCommand())
      {
       str2=chooser.getCurrentDirectory().toString()+"\\"+chooser.getSelectedFile().getName();

       Config.setTargetFileName(str2); 
       dirFileName2.setText(str2);
      }
        if("3"==e.getActionCommand())
      {
       str3=chooser.getCurrentDirectory().toString()+"\\"+chooser.getSelectedFile().getName();

       Config.setLargeFileName(str3);
       dirFileName3.setText(str3);
      }
        if("4"==e.getActionCommand())
      {
       str4=chooser.getCurrentDirectory().toString()+"\\"+chooser.getSelectedFile().getName();

       Config.setTableFileName(str4);
       dirFileName4.setText(str4);
      }
        if("5"==e.getActionCommand())
      {
       str5=chooser.getCurrentDirectory().toString()+"\\"+chooser.getSelectedFile().getName();

       Config.setMatchFileName(str5);
       dirFileName5.setText(str5);
      }
    }
    if(returnVal == JFileChooser.CANCEL_OPTION)
    {
        
    }
        }
        
    }
    
    /**
     * Handles the press of the Close button
     */
    private class CloseAction extends AbstractAction 
    {
        MainPanel mainPanel;
        BNMatchMatchResultPanel component;
        CloseAction (MainPanel mainPanel) 
        {
            this.mainPanel = mainPanel;
        }
        public void actionPerformed(ActionEvent e) 
        {
            //close all open panels
            CytoscapeDesktop desktop = Cytoscape.getDesktop();
            boolean resultsClosed = true;
            CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.EAST);
            for (int c = cytoPanel.getCytoPanelComponentCount() - 1; c >= 0; c--) 
            {
                cytoPanel.setSelectedIndex(c);
                Component component = cytoPanel.getSelectedComponent();
                String componentTitle;
                if (component instanceof BNMatchMatchResultPanel) 
                {
                    this.component = (BNMatchMatchResultPanel) component;
                    componentTitle = this.component.getPanelTitle();
                    String message = "Close " + componentTitle + ".\nContiune?";
                    int result = JOptionPane.showOptionDialog(Cytoscape.getDesktop(), new Object[] {message}, "Comfirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                    if (result == JOptionPane.YES_OPTION)
                    {
                        cytoPanel.remove(component);
                    } 
                    else 
                    {
                        resultsClosed = false;
                    }
                }
            }
            if (cytoPanel.getCytoPanelComponentCount() == 0) 
            {
                cytoPanel.setState(CytoPanelState.HIDE);
            }
            if (resultsClosed) {
                cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);
                cytoPanel.remove(mainPanel);
                trigger.setOpened(false);
            }
        }
    }
    
  
        public BNMatchPlugin.MainPanelAction getTrigger() 
        {
                return trigger;
        }

        public void setTrigger(BNMatchPlugin.MainPanelAction trigger) 
        {
                this.trigger = trigger;
        }
}