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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;

public final class DirectoryWatcherManager implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(DirectoryWatcherManager.class);
	
	private final static Set<DirectoryWatcher> directoryWatchers = new LinkedHashSet<>();	
	private final static Map<String, Kind<Path>> eventKinds = new  LinkedHashMap<>();
	
	private static final AtomicBoolean running;	
	private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private static final long POLLING_RATE = 120; 
	
	
	private static ScheduledFuture<?> watcherSchedule = null;
	
	static {
		eventKinds.put("create", StandardWatchEventKinds.ENTRY_CREATE);
		eventKinds.put("delete", StandardWatchEventKinds.ENTRY_DELETE);
		eventKinds.put("modify", StandardWatchEventKinds.ENTRY_MODIFY);
		
		running = new AtomicBoolean(false);
		
	}
	
	static void setUp() {		
		
		if (running.compareAndSet(false, true) ) {
			LOG.debug("Inizialiting DirectoryWatcherManager....");
			
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
				
				if (directoryWatchers.isEmpty()) {					
					LOG.debug("No DirectoryWatcherManager founds");					
				} else {
					LOG.debug("Scheduling DirectoryWatcherManager events-loop");
					watcherSchedule = executorService.scheduleAtFixedRate(new DirectoryWatcherManager(), POLLING_RATE, POLLING_RATE, TimeUnit.MILLISECONDS);
				}
			
			} catch (XMLConfigException e) {
				LOG.error("Error reading configuration", e);
			}			
		
		} else {
			LOG.debug("DirectoryWatcherManager already running");
		}
	}
	
	static void shutDown() {
		LOG.debug("Finalizing FileAdapter....");
		
		if (running.compareAndSet(true, false) ) {
			
			try {				
				Optional.ofNullable(watcherSchedule).ifPresent(s -> s.cancel(false));				
				
			} catch (Exception e) {
				LOG.error("Error stopping executor service", e);
				
			}
			
			directoryWatchers.stream().forEach(DirectoryWatcher::dismiss);			
			directoryWatchers.clear();
			
			LOG.debug("DirectoryWatcherManager stopped");
		} else {
			LOG.debug("DirectoryWatcherManager already stopped");
		}
		
		
		
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
			LOG.error("Error configuring DirectoryWatcherManager", e);
		}
	}
	
	private DirectoryWatcherManager() {		
	}

	@Override
	public void run() {
		for (DirectoryWatcher watcher: directoryWatchers) {
			if (running.get()) {
				watcher.processEvents();
			} else {
				LOG.debug("Stopping DirectoryWatcherManager events-loop");
				break;
			}
		}
		
	}

}
