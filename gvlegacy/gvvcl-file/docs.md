`directory-watcher` example:

```
 <Channel enabled="true"
          type="FileAdapter"
          id-channel="FileSystem">
                    
         <directory-watcher name="logProcessor" target="/var/log"   type="listener">
             <forward events="create" service="LogManager" operation="notify" processContent="true"/>
         </directory-watcher>
 </Channel>```
