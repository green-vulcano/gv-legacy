package it.greenvulcano.util.file.monitor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

public class FileSystemMonitorFactory {
    
    private final static Map<String, Supplier<FileSystemMonitor>> suppliers = new LinkedHashMap<String, Supplier<FileSystemMonitor>>();
    
    static {
        suppliers.put(LocalFileSystemMonitor.class.getName(), LocalFileSystemMonitor::new);
        suppliers.put(RemoteFileSystemMonitor.class.getName(), RemoteFileSystemMonitor::new);
    }
        
    public static FileSystemMonitor buildInstance(String type) {        
        return Optional.ofNullable(suppliers.get(type)).orElseThrow(()-> new NoSuchElementException("Implementation not found: "+type)).get();
        
    }
}
