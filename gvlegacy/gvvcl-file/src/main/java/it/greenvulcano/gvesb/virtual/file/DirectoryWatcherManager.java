package it.greenvulcano.gvesb.virtual.file;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;

public final class DirectoryWatcherManager {
	private static final Logger LOG = LoggerFactory.getLogger(DirectoryWatcherManager.class);
	
	private final static Set<DirectoryWatcher> directoryWatchers = new LinkedHashSet<>();	
	private final static Map<String, Kind<Path>> eventKinds = new  LinkedHashMap<>();
	
	static {
		eventKinds.put("create", StandardWatchEventKinds.ENTRY_CREATE);
		eventKinds.put("delete", StandardWatchEventKinds.ENTRY_DELETE);
		eventKinds.put("modify", StandardWatchEventKinds.ENTRY_MODIFY);
		
	}
	
	static void setUp() {
		LOG.debug("Inizialiting FileAdapter");
		
		try {
			NodeList fileChannelList = XMLConfig.getNodeList(GreenVulcanoConfig.getSystemsConfigFileName(),"//Channel[@type='FileAdapter' and @enabled='true']");
		
			LOG.debug("Enabled FileAdapter channels found: "+fileChannelList.getLength());
			
			for (int i = 0; i<fileChannelList.getLength(); i++) {
				Node fileAdapter = fileChannelList.item(i);
				
				NodeList directoryWatcherList = XMLConfig.getNodeList(fileAdapter,"./directory-watcher");
				IntStream.range(0, directoryWatcherList.getLength())
		                 .mapToObj(directoryWatcherList::item)		         
		                 .forEach(DirectoryWatcherManager::configure);
			}
		
		} catch (XMLConfigException e) {
			LOG.error("Error reading configuration", e);
		}
		
		directoryWatchers.stream().forEach(DirectoryWatcher::start);
	}
	
	static void shutDown() {
		LOG.debug("Finalizing FileAdapter");
		directoryWatchers.stream().forEach(DirectoryWatcher::stop);
		directoryWatchers.clear();
	}
	
	private static void configure(Node directoryWatcherNode) {
		try {
			String name = XMLConfig.get(directoryWatcherNode, "@name");
			String directory = XMLConfig.get(directoryWatcherNode, "@target");
			
			NodeList configurationList =  XMLConfig.getNodeList(directoryWatcherNode,"./forward");
			for (int i = 0; i<configurationList.getLength(); i++) {
				
				Node configNode = configurationList.item(0);
				String[] events = XMLConfig.get(configNode, "@events").split(",");
				
				Set<Kind<Path>> kinds = new LinkedHashSet<>();
				for (String event:events) {
					Kind<Path> kind = Optional.ofNullable(eventKinds.get(event)).orElseThrow(()->new XMLConfigException("Invalid entry in events: "+event));
					kinds.add(kind);
				}
				
				String service = XMLConfig.get(configNode, "@service");
				String operation = XMLConfig.get(configNode, "@operation");
				boolean processContent = Boolean.valueOf(XMLConfig.get(configNode, "@processContent","false"));
				
				DirectoryWatcher directoryWatcher = new DirectoryWatcher(name, Paths.get(directory), service, operation, kinds, processContent);
				directoryWatchers.add(directoryWatcher);
			}
			
			
		} catch (XMLConfigException e) {
			LOG.error("Error reading configuration", e);
		} catch (IOException e) {
			LOG.error("Error configurind DirectoryWatcher", e);
		}
	}
	
	

}
