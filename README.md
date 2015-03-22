
Think we should be using the surefire plugin - to do tests. then just mvn exec

mvn clean
mvn install 
mvn exec:java -Dexec.mainClass="au.org.emii.test2"

- this isn't really right because the netcdf artifact should come from maven.


---------
IMportant
	- do we want to encode the name in the attributes rather than pass explicitly...


----
Important - the convention class could be used to populate a gui with the encoding types.
	name (uppercase), type. etc.
	
----

- lookup dimension by name rather than index in the convensions
- date encode since 1950
- sql parametization
- try on other CF type - trajectory 

----
prototype demonstrate end-to-end filter to subset netcdf generation, with streaming 
on prod data.

WHAT'S DONE
- done sequencing the definition, and data writing phases of netcdf generation 
- done basic subset expression parser/ 
- done postgres dialect rewriter instance
- done netcdf encoder
- done localized convention/ config strategy
- done experimented with a couple of query approaches favor time to start streaming, total time etc. 
- done - write required attributes with strategy
- done - write multiple files (and get the writable instantiation working ) (pass at the time). 
- done-  should specify dimension of a var by name, then we can infer size/count. 
- done - think we have to invert it, instead of trying to infer, use an explicit configuration.
	- It's part of the task.
	- meaning use spring.
- done - mvn project
- done layer configuration into xml file

-----
	http://www.unidata.ucar.edu/software/thredds/v4.3/netcdf-java/reference/faq.html

	Perhaps Easier way to create the array?  
		Array.factory(DataType dataType, int[] shape, Object javaArray);
			
---
TODO

- organize project sources
- generalize to trajectory and profile
- surefire to get code-running.
- config to specify the tables.

- compose with spring ?

-  (cant but have abstracted out) do the netcdf generation in memory

- get dates correctly encoded  (to enable verificiation of filter date range)
- pass table as paraemter to encoder strategy (not sure)
- make a tomcat service

- add support to parser/expr for geometry for spatial constraint choice
- factor encoder strategy chooser - so it's not instantiated, or else use property setters instead of constructor for the writer. 
- postgres - parametize query parameters, rather than use text
- need time,lat,lon
- try on another timeseries, / implement for trajectory
- make it a tomcat webapp
- zip the file stream
- factor into classes
- command-line driven


REFS

Marty, says
(but which schemas for profile) 

timeseries example
jfca@10-nsp-mel:~$ ncdump  /mnt/opendap/1/IMOS/opendap/eMII/checker_test/ANMN/timeSeries/IMOS_ANMN-QLD_TZ_20140907T063947Z_ITFTIS_FV01_ITFTIS-1409-SBE39-94_END-20150207T065000Z_C-20150219T005030Z.nc | less

profile example
jfca@10-nsp-mel:~$ ncdump /mnt/opendap/1/IMOS/opendap/eMII/checker_test/ANMN/profile/IMOS_ANMN-NRS_CDEKOSTUZ_20150224T023931Z_NRSROT_FV01_Profile-SBE19plus_C-20150227T052824Z.nc  | less

no trajectory

----

For profile



https://www.unidata.ucar.edu/software/netcdf/docs/netcdf/Variables.html
https://www.unidata.ucar.edu/software/netcdf/docs/netcdf/Dimensions.html


http://www.unidata.ucar.edu/software/thredds/current/netcdf-java/tutorial/NetcdfFileWriteable.html

http://www.unidata.ucar.edu/software/netcdf/docs/BestPractices.html

HTTps://www.unidata.ucar.edu/software/netcdf/docs/netcdf/CDL-Data-Types.html
