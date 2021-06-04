package org.cytoscape.keggparser.actions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.keggparser.KEGGParserPlugin;
import org.cytoscape.keggparser.com.ParsingReportGenerator;
import org.cytoscape.keggparser.dialogs.KeggSaveDialog;
import org.cytoscape.keggparser.parsing.KGMLConverter;
import org.cytoscape.keggparser.parsing.KGMLCreator;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class KeggSaveAsBioPAXAction extends AbstractCyAction {
    private int bioPaxLevel;
    private KeggSaveDialog saveDialog;
    private File outFile;
    private String menuName;
    private String suffix = "";

    String getSuffix() {
        return suffix;
    }

    public KeggSaveAsBioPAXAction(int bioPaxLevel, String menuName) {
        super(menuName == null ? "" : menuName);
        setPreferredMenu("Apps.KEGGParser.Save network");

        if (menuName == null)
            menuName = "";
        this.menuName = menuName;
        this.bioPaxLevel = bioPaxLevel;

        if (bioPaxLevel == KGMLConverter.BioPAX2)
            suffix = ".bp2";
        else if (bioPaxLevel == KGMLConverter.BioPAX3)
            suffix = ".bp3";
    }


    int getBioPaxLevel() {
        return bioPaxLevel;
    }

    File getOutFile() {
        return outFile;
    }

    KeggSaveDialog getSaveDialog() {
        return saveDialog;
    }

    public void actionPerformed(ActionEvent e) {
        outFile = getSelectedFileFromSaveDialog();
        if (outFile != null) {
            writeOutFileDirectory();
            // Create Task
            final SaveBioPAXTask task = new SaveBioPAXTask(outFile.getAbsolutePath());
            KEGGParserPlugin.taskManager.execute(new TaskIterator(task));
        }
    }

    private void writeOutFileDirectory() {
        if (outFile != null) {
            try {
                PrintWriter recentDirWriter = new PrintWriter(saveDialog.getRecentDir());
                recentDirWriter.write(outFile.getParent());
                recentDirWriter.close();
            } catch (FileNotFoundException e1) {
                LoggerFactory.getLogger(KeggSaveAsBioPAXAction.class).error(e1.getMessage());
            }
        }
    }

    private File getSelectedFileFromSaveDialog() {
        if (bioPaxLevel == KGMLConverter.BioPAX2)
            saveDialog = new KeggSaveDialog(".bp2");
        else if (bioPaxLevel == KGMLConverter.BioPAX3)
            saveDialog = new KeggSaveDialog(".bp3");
        else {
            try {
                throw new Exception("Ivalid bioPax level has been specified.");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return null;
        }
        int response = saveDialog.showSaveDialog(KEGGParserPlugin.cytoscapeDesktopService.getJFrame());
        if (response == KeggSaveDialog.CANCEL_OPTION)
            return null;
        return saveDialog.getSelectedFile();
    }

    String getMenuName() {
        return menuName;
    }


    public class SaveBioPAXTask extends AbstractTask {

        private String outFilePath;
        private TaskMonitor taskMonitor;

        public SaveBioPAXTask(String outFilePath) {
            this.outFilePath= outFilePath;
            super.cancelled = false;
        }

        @Override
        public void run(TaskMonitor taskMonitor) throws Exception {
            this.taskMonitor = taskMonitor;
            taskMonitor.setTitle("Kegg saving task");
            taskMonitor.setStatusMessage("Saving KGML file.\n\nIt may take a while.\nPlease wait...");
            ParsingReportGenerator.getInstance().appendLine("Saving the network as BioPAX_level2 to " +
                    outFilePath);
            taskMonitor.setProgress(-1);

            try {
                File existingFile = new File(outFilePath);
                if ((existingFile).exists())
                    existingFile.delete();
                File kgmlFile = createKGMLFile(new File(outFilePath));
                taskMonitor.setStatusMessage("Converting KGML file " + kgmlFile.getAbsolutePath()
                        + " to " + suffix + " file");
                KGMLConverter kgmlConverter = new KGMLConverter();
               
                kgmlConverter.translateFromCmd(kgmlFile, outFilePath,
                        bioPaxLevel, taskMonitor, this);
                ParsingReportGenerator.getInstance().appendLine("outfile is:" + outFile);
                ParsingReportGenerator.getInstance().appendLine("outfile length is: " + outFile.length());
               
                if (outFile.exists() && outFile.length() >0) {
                    String successMessage = "BioPAX file " + outFilePath + " successfully generated.";
                    taskMonitor.setStatusMessage(successMessage);
                    ParsingReportGenerator.getInstance().appendLine(successMessage);
                    taskMonitor.setProgress(1);
                } else {
                    ParsingReportGenerator.getInstance().appendLine("Problem saving network as " + suffix);
                    throw new Exception("Problem saving network as " + suffix);
                }
            } catch (Exception e) {
                throw new Exception("Error while saving KGML " + e.getMessage());
            } finally {
                System.gc();
            }
        }

        private File createKGMLFile(File outFile) throws Exception {
            File kgmlFile = getKGMLFile(outFile);
            KGMLCreator kgmlCreator = new KGMLCreator();
            kgmlCreator.setFilterForConversion(bioPaxLevel);
            kgmlCreator.createKGML(KEGGParserPlugin.cyApplicationManager.getCurrentNetwork(), kgmlFile);
            taskMonitor.setStatusMessage("KGML file " + kgmlFile.getAbsolutePath() + " successfully generated.");
            ParsingReportGenerator.getInstance().appendLine("KGML file saved to " + kgmlFile.getAbsolutePath());
            return kgmlFile;
        }


        @Override
        public void cancel() {

            LoggerFactory.getLogger(KeggSaveAsBioPAXAction.class).info("Cancel called!!!");
            taskMonitor.setProgress(1);
            super.cancelled = true;
            System.gc();

        }


        public boolean isCancelled() {
            return cancelled;
        }
    }

    private File getKGMLFile(File outFile) {
        String kgmlFileName;
        if (outFile.getName().contains(suffix))
            kgmlFileName = outFile.getName().replace(suffix, ".xml");
        else
            kgmlFileName = outFile.getName() + ".xml";
        
        if (outFile.getName().contains(" "))
        	kgmlFileName = outFile.getName().replace(" ", "_");
        
        File kgmlDir = new File(KEGGParserPlugin.getKEGGParserDir(), "/kgml");
        if (!kgmlDir.exists())
            if (!kgmlDir.mkdir()) {
                LoggerFactory.getLogger(KeggSaveAsBioPAXAction.class).
                        error("Error creating directory " + kgmlDir.getAbsolutePath());
                kgmlDir = KEGGParserPlugin.getKEGGParserDir();
            }
        return new File(kgmlDir, kgmlFileName);
    }

}

