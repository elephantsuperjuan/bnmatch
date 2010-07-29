package Cytoscape.plugin.BNMatch;

/**
 *
 * @author YULEI
 */

import cytoscape.plugin.CytoscapePlugin;
import cytoscape.Cytoscape;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;
import cytoscape.visual.VisualMappingManager;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

public class BNMatchPlugin extends CytoscapePlugin
{
    public BNMatchPlugin()
    {
        JMenu menu=Cytoscape.getDesktop().getCyMenus().getOperationsMenu();
        JMenuItem item;
        JMenu submenu = new JMenu("BNMatch");
        submenu.setToolTipText("Plugin for clustering");

        //BNMatch panel
        item = new JMenuItem("Start");
        item.setToolTipText("Start the plugin");
        item.addActionListener(new MainPanelAction());
        submenu.add(item);
       
        item = new JMenuItem("Stop");
        item.setToolTipText("Stop the plugin");
        item.addActionListener(new StopAction());
        submenu.add(item);
        submenu.addSeparator();
        
        item = new JMenuItem("About...");
        item.setToolTipText("About the plugin");
        item.addActionListener(new AboutAction());
        submenu.add(item);
       
        menu.add(submenu);
    }
    
public class MainPanelAction implements ActionListener
    {
        boolean opened = false;
        MainPanel mainPanel;


        public void actionPerformed(ActionEvent event) 
        {
            CytoscapeDesktop desktop = Cytoscape.getDesktop();
            CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);

            //plugin hasn't been started
            if (!opened) 
            {
                mainPanel = new MainPanel(this);
                cytoPanel.add("BNMatch", mainPanel);
            }
            else 
            {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "The plugin has started already!");
            }    
            int index = cytoPanel.indexOfComponent(mainPanel);
            cytoPanel.setSelectedIndex(index);
            cytoPanel.setState(CytoPanelState.DOCK);
            setOpened(true);
        }


        public void setOpened(boolean opened) 
        {
            this.opened = opened;
        }
        public boolean isOpened() 
        {
            return opened;
        }
    }
private class StopAction implements ActionListener
    {
        MainPanel mainPanel;
        BNMatchMatchResultPanel matchResultPanel;
        StopAction () 
        {
        }
        /**
         * get the MainPanel if the Plugin has started, null otherwise.
         */
        public MainPanel getMainPanel()
        {
                MainPanel mainPanel=null;
                CytoPanel cytoPanel=Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST);
                for(int i=cytoPanel.getCytoPanelComponentCount()-1; i>=0; i--)
                {
                        Component comp=cytoPanel.getComponentAt(i);
                        if(comp instanceof MainPanel)
                                mainPanel=(MainPanel)comp;
                }
                return mainPanel;
        }

        public void actionPerformed(ActionEvent e) 
        {
                mainPanel=getMainPanel();
            if (mainPanel==null) 
            {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Plugin has not started yet!");
                return;
            }
            CytoscapeDesktop desktop = Cytoscape.getDesktop();
            CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.EAST);//get the east panel
            //close all open result panels
            for (int c = cytoPanel.getCytoPanelComponentCount() - 1; c >= 0; c--) 
            {
                cytoPanel.setSelectedIndex(c);
                Component component = cytoPanel.getSelectedComponent();
                String componentTitle;
                if (component instanceof BNMatchMatchResultPanel) 
                {
                    this.matchResultPanel = (BNMatchMatchResultPanel) component;
                    componentTitle = this.matchResultPanel.getResultTitle();
                    cytoPanel.remove(component);
//                    ParameterSet.removeResultParams(componentTitle);
                }
            }
            //hide the result panel
            if (cytoPanel.getCytoPanelComponentCount() == 0) {
                cytoPanel.setState(CytoPanelState.HIDE);
            }
            cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);
            cytoPanel.remove(mainPanel);
            mainPanel.getTrigger().setOpened(false);
        }
    
}

private class AboutAction implements ActionListener 
{
        public void actionPerformed(ActionEvent e)
        {
            AboutDialog aboutDialog = new AboutDialog();
            aboutDialog.pack();//the dialog must be packed???
            aboutDialog.setVisible(true);
        }
    }

private class AboutDialog extends JDialog 
{
        static final long serialVersionUID=-954321L;
        public AboutDialog() 
        {
            super(Cytoscape.getDesktop(), "About BNMatch Plugin", false);
            setResizable(false);

            JEditorPane editorPane = new JEditorPane();
            editorPane.setMargin(new Insets(10,10,10,10));
            editorPane.setEditable(false);
            editorPane.setEditorKit(new HTMLEditorKit());
            editorPane.addHyperlinkListener(new HyperlinkAction(editorPane));

            URL logoURL = BNMatchPlugin.class.getResource("resources/logo.gif");
            String logoCode = "";
            if (logoURL != null)
            {
                logoCode = "<center><img src='"+logoURL+"'></center>";
            }

            editorPane.setText(        
                    "<html><body>"+logoCode+"<P align=center><b>BNMatch 0.1(December 2009) </b><BR>" +
                    "<i>The visualizing of biological molecules network</i><BR>" +
                    "BNMatch finds the most similar target network in the large <BR>" +
                    "biological molecules network in some search algorithm,<BR>" +
                    "then visualizing the result network.<BR><BR>"+
                    "</P></body></html>");
            setContentPane(editorPane);
        }
        private class HyperlinkAction implements HyperlinkListener
        {
            JEditorPane pane;
            public HyperlinkAction(JEditorPane pane) 
            {
                this.pane = pane;
            }
            public void hyperlinkUpdate(HyperlinkEvent event)
            {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
                {
                    cytoscape.util.OpenBrowser.openURL(event.getURL().toString());
                }
            }
        }
    }
}


