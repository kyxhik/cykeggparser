package org.cytoscape.keggparser.actions;


import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.keggparser.KEGGParserPlugin;
import org.cytoscape.keggparser.com.ParsingReportGenerator;
import org.cytoscape.keggparser.dialogs.KeggLoadFrame;
import org.cytoscape.keggparser.parsing.KeggNetworkCreator;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class KeggLoadAction extends AbstractCyAction {

    private KeggLoadFrame keggLoadFrame;
    private CyNetworkView networkView;

    public KeggLoadAction() {

        super("Load local KGML");
        setMenuGravity(0);
        setPreferredMenu("Apps.KEGGParser.Load KGML");
        if (keggLoadFrame == null) {
            keggLoadFrame = new KeggLoadFrame();
        }
    }

    public void actionPerformed(ActionEvent e) {

        keggLoadFrame.showFrame();
        if (keggLoadFrame.getSelectedFile() == null)
            return;
        
        
        File kgmlFile = keggLoadFrame.getSelectedFile();
        if (kgmlFile != null && kgmlFile.exists()) {
        	
            String kgmlFileName = keggLoadFrame.getSelectedFile().getAbsolutePath();
            LoggerFactory.getLogger(KeggLoadAction.class).info("Opening session file: " + kgmlFileName);
            ParsingReportGenerator.getInstance().appendLine("kgml file name is:" + kgmlFileName);
            
            // Create Task
            final ParseKgmlTask task = new ParseKgmlTask(kgmlFileName);
            KEGGParserPlugin.taskManager.execute(new TaskIterator(task));

        }
    }

    public KeggLoadFrame getKeggLoadFrame() {
        return keggLoadFrame;
    }


    class ParseKgmlTask extends AbstractTask {
        private String fileName;

        public ParseKgmlTask(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void run(TaskMonitor taskMonitor) throws Exception {
            ParsingReportGenerator.getInstance().appendLine("\n" + (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date()) +
                    "\nParsing kgml file " + fileName + "");
            taskMonitor.setTitle("Kegg parsing task");
            taskMonitor.setStatusMessage("Parsing KGML file.\n\nIt may take a while.\nPlease wait...");
            taskMonitor.setProgress(-1);
            networkView = null;

            try {
                KeggNetworkCreator keggNetworkCreator = new KeggNetworkCreator();
                keggNetworkCreator.createNetwork(new File(fileName));
                networkView = keggNetworkCreator.getNetworkView();
                taskMonitor.setStatusMessage("KGML file " + fileName + " successfully parsed.");
                ParsingReportGenerator.getInstance().appendLine("KGML file " + fileName + " successfully parsed.");
            } catch (Exception e) {
                ParsingReportGenerator.getInstance().appendLine("Error while parsing KGML: " + e.getMessage());
                throw new Exception("Error while parsing KGML " + new File(fileName).getName() + ": " + e.getMessage());
            } finally {
                if (networkView != null)
                    KeggNetworkCreator.applyKeggVisualStyle(networkView);
                taskMonitor.setProgress(1);
                System.gc();
            }

        }

        @Override
        public void cancel() {
            super.cancel();
        }
    }


}

