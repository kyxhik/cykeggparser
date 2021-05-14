package org.cytoscape.keggparser;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.keggparser.actions.*;
import org.cytoscape.keggparser.com.EKeggProps;
import org.cytoscape.keggparser.com.EKeggWebProps;
import org.cytoscape.keggparser.tuning.tse.EKEGGTuningProps;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;


public class KEGGParserPlugin extends AbstractCyActivator {
    public static final int PARSING = 0;
    public static final int TUNING = 1;
    public static final String PARSIN_LOG_NAME = "parsing.log";
    public static final String TUNING_LOG_NAME = "tuning.log";
    public static File KEGGParserDir = null;
    public static final org.slf4j.Logger KEGGParserLogger = LoggerFactory.getLogger(KEGGParserPlugin.class);
    private static Properties keggProps;
    private static File keggPropsFile;
    private static File keggTranslatorJarFile;
    private static File parsingReportFile = null;
    private static File tuningReportFile = null;

    public static CySwingApplication cytoscapeDesktopService;
    public static DialogTaskManager taskManager;
    public static CySessionManager cySessionManager;
    public static CyNetworkFactory networkFactory;
    public static CyNetworkViewFactory networkViewFactory;
    public static CyNetworkManager networkManager;
    public static CyNetworkViewManager networkViewManager;
    public static VisualMappingManager visualMappingManager;
    public static VisualMappingFunctionFactory vmfFactoryC;
    public static VisualMappingFunctionFactory vmfFactoryD;
    public static VisualMappingFunctionFactory vmfFactoryP;
    public static VisualStyleFactory visualStyleFactory;
    public static CyTableFactory tableFactory;
    public static CyApplicationConfiguration cyAppConfig;
    public static CyEventHelper cyEventHelper;
    public static CyApplicationManager cyApplicationManager;
    public static CyTableManager cyTableManager;

    public static KeggLoadAction keggLoadAction;
    public static KEGGPreferenceAction keggPreferenceAction;
    public static KeggSaveAsKGMLAction keggSaveAsKGMLAction;
    public static KeggSaveAsBioPAX2Action keggSaveAsBioPAX2Action;
    public static KeggSaveAsBioPAX3Action keggSaveAsBioPAX3Action;
    public static KeggSaveAsSBML keggSaveAsSBML;
    public static KEGGTuningAction keggTuningAction;
    public static KeggWebLoadAction keggWebLoadAction;
    public static KeggHelpAction keggHelpAction;

    public KEGGParserPlugin() {
        super();
    }


    public void start(BundleContext bc) {

        cytoscapeDesktopService = getService(bc, CySwingApplication.class);
        taskManager = getService(bc, DialogTaskManager.class);
        cySessionManager = getService(bc, CySessionManager.class);
        networkFactory = getService(bc, CyNetworkFactory.class);
        networkViewFactory = getService(bc, CyNetworkViewFactory.class);
        networkManager = getService(bc, CyNetworkManager.class);
        networkViewManager = getService(bc, CyNetworkViewManager.class);
        visualMappingManager = getService(bc, VisualMappingManager.class);
        vmfFactoryC = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
        vmfFactoryD = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
        vmfFactoryP = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
        visualStyleFactory = getService(bc, VisualStyleFactory.class);
        tableFactory = getService(bc, CyTableFactory.class);
        cyAppConfig = getService(bc, CyApplicationConfiguration.class);
        cyEventHelper = getService(bc, CyEventHelper.class);
        cyApplicationManager = getService(bc, CyApplicationManager.class);
        cyTableManager = getService(bc, CyTableManager.class);
        cySessionManager = getService(bc, CySessionManager.class);

        keggLoadAction = new KeggLoadAction();
        keggPreferenceAction = new KEGGPreferenceAction();
        keggSaveAsKGMLAction = new KeggSaveAsKGMLAction();
        keggSaveAsBioPAX2Action = new KeggSaveAsBioPAX2Action();
        keggSaveAsBioPAX3Action = new KeggSaveAsBioPAX3Action();
        keggSaveAsSBML = new KeggSaveAsSBML();
        keggTuningAction = new KEGGTuningAction();
        keggWebLoadAction = new KeggWebLoadAction();
        keggHelpAction = new KeggHelpAction();


        registerService(bc, cytoscapeDesktopService, CySwingApplication.class, new Properties());
        registerService(bc, taskManager, DialogTaskManager.class, new Properties());
        registerService(bc, cySessionManager, CySessionManager.class, new Properties());
        registerService(bc, networkFactory, CyNetworkFactory.class, new Properties());
        registerService(bc, networkViewFactory, CyNetworkViewFactory.class, new Properties());
        registerService(bc, networkViewManager, CyNetworkViewManager.class, new Properties());
        registerService(bc, networkManager, CyNetworkManager.class, new Properties());
        registerService(bc, visualMappingManager, VisualMappingManager.class, new Properties());
        registerService(bc, vmfFactoryC, VisualMappingFunctionFactory.class, new Properties());
        registerService(bc, vmfFactoryD, VisualMappingFunctionFactory.class, new Properties());
        registerService(bc, vmfFactoryP, VisualMappingFunctionFactory.class, new Properties());
        registerService(bc, visualStyleFactory, VisualStyleFactory.class, new Properties());
        registerService(bc, tableFactory, CyTableFactory.class, new Properties());
        registerService(bc, cyAppConfig, CyApplicationConfiguration.class, new Properties());
        registerService(bc, cyEventHelper, CyEventHelper.class, new Properties());
        registerService(bc, cyApplicationManager, CyApplicationManager.class, new Properties());
        registerService(bc, cyTableManager, CyTableManager.class, new Properties());


        registerService(bc, keggLoadAction, CyAction.class, new Properties());
        registerService(bc, keggPreferenceAction, CyAction.class, new Properties());
        registerService(bc, keggSaveAsKGMLAction, CyAction.class, new Properties());
        registerService(bc, keggSaveAsBioPAX2Action, CyAction.class, new Properties());
        registerService(bc, keggSaveAsBioPAX3Action, CyAction.class, new Properties());
        registerService(bc, keggSaveAsSBML, CyAction.class, new Properties());
        registerService(bc, keggTuningAction, CyAction.class, new Properties());
        registerService(bc, keggWebLoadAction, CyAction.class, new Properties());
        registerService(bc, keggHelpAction, CyAction.class, new Properties());

    }


    public static File getReportFile(int type) {
        if (type == PARSING)
            return getReportFile(parsingReportFile, PARSIN_LOG_NAME);
        if (type == TUNING)
            return getReportFile(tuningReportFile, TUNING_LOG_NAME);
        throw new IllegalArgumentException(String.format("The report type %d is not valid", type));
    }

    private static File getReportFile(File reportFile, String reportFileName) {
        File loggingDir = null;
        if (reportFile == null)
            loggingDir = setLoggingDirectory();
        if (loggingDir != null && loggingDir.exists()) {
            reportFile = new File(loggingDir, reportFileName);
            if (!reportFile.exists())
                try {
                    reportFile.createNewFile();
                } catch (IOException e) {
                    LoggerFactory.getLogger(KEGGParserPlugin.class).error(e.getMessage());
                }
            else {
                if (reportFile.length() > (1024 * 1024))
                    try {
                        reportFile.createNewFile();
                    } catch (IOException e) {
                        LoggerFactory.getLogger(KEGGParserPlugin.class).error(e.getMessage());
                    }
            }
        }

        return reportFile;
    }


    private static File setLoggingDirectory() {
        File loggingDir = new File(getKEGGParserDir(), "logs");
        boolean dirValid = true;
        if (!loggingDir.exists())
            dirValid = loggingDir.mkdir();
        if (dirValid)
            return loggingDir;
        return null;
    }

    private static void initProperties() {
        keggPropsFile = new File(KEGGParserPlugin.getKEGGParserDir(), "kegg.props");
        FileInputStream stream = null;
        if (keggPropsFile.exists())
            try {
                stream = new FileInputStream(KEGGParserPlugin.getKEGGParserDir().getAbsolutePath() + "/kegg.props");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        boolean isPropsFileValid = true;

        if (stream != null) {
            if (keggProps == null) {
                keggProps = new Properties();
                try {
                    keggProps.load(stream);
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    KEGGParserLogger.error(e.getMessage());
                }
            }

            for (EKeggProps eKeggProps : EKeggProps.values()) {
                if (keggProps.getProperty(eKeggProps.getName()) == null) {
                    isPropsFileValid = false;
                    break;
                }
            }

            if (isPropsFileValid) {
                for (EKEGGTuningProps ekeggTuningProps : EKEGGTuningProps.values()) {
                    if (keggProps.getProperty(ekeggTuningProps.getName()) == null) {
                        isPropsFileValid = false;
                        break;
                    }
                }
            }
            if (isPropsFileValid) {
                for (EKeggWebProps ekeggWebProps : EKeggWebProps.values()) {
                    if (keggProps.getProperty(ekeggWebProps.getName()) == null) {
                        isPropsFileValid = false;
                        break;
                    }
                }
            }


        } else
            isPropsFileValid = false;

        if (!isPropsFileValid) {
            try {
                if (keggPropsFile.exists())
                    keggPropsFile.delete();
                keggPropsFile.createNewFile();
                ClassLoader cl = KEGGParserPlugin.class.getClassLoader();
                InputStream in = cl.getResourceAsStream("kegg.props");
                keggProps = new Properties();
                keggProps.load(in);
                keggProps.store(new PrintWriter(getKeggPropsFile()), "");
            } catch (IOException e) {
                KEGGParserLogger.error(e.getMessage());
                e.printStackTrace();
            }
        }

        for (EKeggProps property : EKeggProps.values()) {
            property.setOldValue(Boolean.parseBoolean((String) KEGGParserPlugin.getKeggProps().get(property.getName())));
            property.setNewValue(Boolean.parseBoolean((String) KEGGParserPlugin.getKeggProps().get(property.getName())));
        }

    }

    private static void createPluginDirectory() {
        File appConfigDir = cyAppConfig.getConfigurationDirectoryLocation();
        File appData = new File(appConfigDir, "app-data");
        if (!appData.exists())
            appData.mkdir();

        KEGGParserDir = new File(appData, "CyKEGGParser");
        if (!KEGGParserDir.exists())
            if (!KEGGParserDir.mkdir())
                LoggerFactory.getLogger(KEGGParserPlugin.class).
                        error("Failed to create directory " + KEGGParserDir.getAbsolutePath());

    }

    public static Properties getKeggProps() {
        if (keggProps == null)
            initProperties();
        return keggProps;
    }

    public static File getKEGGParserDir() {
        if (KEGGParserDir == null) {
            createPluginDirectory();
        }
        return KEGGParserDir;
    }

    public static File getKeggPropsFile() {
        if (keggPropsFile == null)
            initProperties();
        return keggPropsFile;
    }

    public static File getKeggTranslatorJar() throws FileNotFoundException {
        File libDir = new File(KEGGParserPlugin.getKEGGParserDir(), "lib");
        boolean success = false;
        if (!libDir.exists()) {
            success = libDir.mkdir();
        } else
            success = true;
        if (success) {
            keggTranslatorJarFile = new File(libDir, "keggtranslator-api-2.3.0.jar");
            if (!keggTranslatorJarFile.exists()
                    || keggTranslatorJarFile.length() == 0) {
                ClassLoader cl = KEGGParserPlugin.class.getClassLoader();
                InputStream in = cl.getResourceAsStream("keggtranslator-api-2.3.0.jar");
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(keggTranslatorJarFile);
                    byte[] bytes = new byte[1024];
                    int read;
                    while ((read = in.read(bytes)) != -1) {
                        out.write(bytes, 0, read);
                    }
                    in.close();
                    out.close();
                } catch (IOException e) {
                    LoggerFactory.getLogger(KeggSaveAsBioPAX2Action.class).error(e.getMessage());
                }
            }
        }
        if (!keggTranslatorJarFile.exists())
            throw new FileNotFoundException();
        return keggTranslatorJarFile;
    }

    public static void main(String[] args) {

    }



}

