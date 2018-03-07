package it.greenvulcano.gvesb.virtual.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolException;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;

public class DirectoryWatcher  {
	private static final Logger LOG = LoggerFactory.getLogger(DirectoryWatcher.class);
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newWorkStealingPool();

	private final String name;
	private final Path directory;
		
	private final String service, operation;
	
	private final WatchService watcher;
	private final WatchKey watchKey;
		
	private final boolean processFileContent;
	 
	
	public String getName() {
		return name;
	}

	public Path getDirectory() {
		return directory;
	}
	
	public String getService() {
		return service;
	}
	
	public String getOperation() {
		return operation;
	}

	public DirectoryWatcher(String name, Path directory, String service, String operation, Set<Kind<Path>> events, boolean processFileContent) throws IOException {

		if (directory != null && Files.exists(directory) && Files.isDirectory(directory)) {

			this.name = name;
			this.directory = directory;

			this.watcher = FileSystems.getDefault().newWatchService();
			this.watchKey = directory.register(watcher, events.toArray(new Kind<?>[events.size()]));

			this.service = service;
			this.operation = operation;
						
			this.processFileContent = processFileContent;

		} else {
			throw new FileNotFoundException();
		}

	}
	
	void processEvents() {
		
		Optional.ofNullable(watcher.poll())
		        .ifPresent(eventKey -> {
		        	
		        	eventKey.pollEvents()
		        	        .stream()
		        	        .map(this::buildGVBuffer)
		        	        .filter(Optional::isPresent)
		        	        .map(Optional::get)		        	        
		        	        .map(GreenVulcanoExecutor::new)
		        	        .forEach(EXECUTOR_SERVICE::submit);
		        	
		        	eventKey.reset();
		        	
		        });
		      
	}
	
	void dismiss() {
		LOG.debug("DirectoryWatcher["+name+"] stopping on "+directory);
		if(watchKey.isValid()) {
			
			watchKey.cancel();	
			
		}
		
		try {
			watcher.close();
			
			EXECUTOR_SERVICE.shutdown();
		} catch (IOException e) {
			LOG.error("DirectoryWatcher["+name+"] interruped on "+directory, e);
		} 	
		
	}

	@SuppressWarnings("unchecked")
	private Optional<GVBuffer> buildGVBuffer(WatchEvent<?> event) {
		WatchEvent.Kind<?> kind = event.kind();

		if (StandardWatchEventKinds.OVERFLOW.equals(kind)) {
			LOG.error("DirectoryWatcher["+name+"] on "+directory+" got OVERFLOW processing events: "+ event.context());
		} else {
		
		
			WatchEvent<Path> ev = (WatchEvent<Path>)event;
			Path filename = ev.context();
	
			try {
				GVBuffer gvbuffer = new GVBuffer();
	
				gvbuffer.setService(service);
	
				gvbuffer.setProperty("DIRECTORY_WATCHER_NAME", getName());
				gvbuffer.setProperty("DIRECTORY_WATCHER_TARGET", getDirectory().toString());
				gvbuffer.setProperty("DIRECTORY_WATCHER_FILE", filename.toString());
				gvbuffer.setProperty("DIRECTORY_WATCHER_EVENT", kind.name());
				
				if (processFileContent && !StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
					LOG.debug("DirectoryWatcher ["+name+"] process file content ");
					gvbuffer.setObject(Files.readAllBytes(getDirectory().resolve(filename)));
				}		
	
				return Optional.of(gvbuffer);
			} catch (GVException | IOException e) {
				LOG.error("DirectoryWatcher["+name+"] on "+directory+" fails building GVBuffer", e);
			}

		}
		return Optional.empty();
	}

	
	private class GreenVulcanoExecutor implements Runnable {

		private final GreenVulcanoPool greenVulcano;
		private final GVBuffer inputBuffer;

		GreenVulcanoExecutor(GVBuffer inputBuffer) {
			this.inputBuffer = inputBuffer;
			this.greenVulcano = GreenVulcanoPoolManager.instance()
					.getGreenVulcanoPool("DirectoryWatcher")
					.orElseGet(GreenVulcanoPoolManager::getDefaultGreenVulcanoPool);

		}


		@Override
		public void run() {
			try {
				LOG.debug("DirectoryWatcher ["+name+"] forwarding event to "+inputBuffer.getService()+"/"+getOperation());
				greenVulcano.forward(inputBuffer, getOperation());

			} catch (GVPublicException e) {
				LOG.error("DirectoryWatcher ["+name+"] on "+directory+" got error in forward ", e);
			} catch (GreenVulcanoPoolException e) {
				LOG.error("DirectoryWatcher ["+name+"] on "+directory+" got error getting pool instance", e);
			}

		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((directory == null) ? 0 : directory.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((operation == null) ? 0 : operation.hashCode());
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DirectoryWatcher other = (DirectoryWatcher) obj;
		if (directory == null) {
			if (other.directory != null)
				return false;
		} else if (!directory.equals(other.directory))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (operation == null) {
			if (other.operation != null)
				return false;
		} else if (!operation.equals(other.operation))
			return false;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!service.equals(other.service))
			return false;
		return true;
	}
	

}
