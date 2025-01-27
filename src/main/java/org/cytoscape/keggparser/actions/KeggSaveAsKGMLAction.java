package org.cytoscape.keggparser.actions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.keggparser.KEGGParserPlugin;
import org.cytoscape.keggparser.com.ParsingReportGenerator;
import org.cytoscape.keggparser.dialogs.KeggSaveDialog;
import org.cytoscape.keggparser.parsing.KGMLCreator;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class KeggSaveAsKGMLAction extends AbstractCyAction {

    public KeggSaveAsKGMLAction() {
        super("Save as KGML");
        setMenuGravity(2);
        setPreferredMenu("Apps.KEGGParser.Save network");
    }


    public void actionPerformed(ActionEvent e) {
        JFrame keggSaveFrame = new JFrame("KGML save window");
        keggSaveFrame.setLocation(400, 250);
        keggSaveFrame.setSize(400, 200);
        KeggSaveDialog saveDialog = new KeggSaveDialog(".xml");
        int response = saveDialog.showSaveDialog(KEGGParserPlugin.cytoscapeDesktopService.getJFrame());
        if (response == KeggSaveDialog.CANCEL_OPTION)
            return;
        File outFile = saveDialog.getSelectedFile();
        String kgmlFileName;
        if (outFile != null){

            kgmlFileName = outFile.getAbsolutePath();
            try {
                PrintWriter recentDirWriter = new PrintWriter(saveDialog.getRecentDir());
                recentDirWriter.write(outFile.toString());
                recentDirWriter.close();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }

            LoggerFactory.getLogger(KeggSaveAsKGMLAction.class).info("Saving network to: " + kgmlFileName);
            // Create Task

            final SaveKgmlTask task = new SaveKgmlTask(kgmlFileName);
            KEGGParserPlugin.taskManager.execute(new TaskIterator(task));
        }

    }



    class SaveKgmlTask extends AbstractTask {
        private String fileName;

        public SaveKgmlTask(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void run(TaskMonitor taskMonitor) throws Exception {
            taskMonitor.setTitle("Kegg saving task");
            taskMonitor.setStatusMessage("Saving KGML file.\n\nIt may take a while.\nPlease wait...");
            ParsingReportGenerator.getInstance().appendLine("Saving the network as KGML to " + fileName);
            taskMonitor.setProgress(-1);

            try {
                KGMLCreator kgmlCreator = new KGMLCreator();
                kgmlCreator.createKGML(KEGGParserPlugin.cyApplicationManager.getCurrentNetwork(), new File(fileName));
                taskMonitor.setStatusMessage("KGML file " + fileName + " successfully generated.");
                ParsingReportGenerator.getInstance().appendLine("KGML file saved to " + fileName);
            } catch (Exception e) {
                ParsingReportGenerator.getInstance().appendLine("Error while saving KGML " + e.getMessage());
                throw new Exception("Error while saving KGML " + e.getMessage());
            } finally {
                taskMonitor.setProgress(1);
                System.gc();
            }
        }

    }

}

