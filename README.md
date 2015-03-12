
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
- sequencing the definition, and data writing phases of netcdf generation 
- basic subset expression parser/ 
- postgres dialect rewriter instance
- netcdf encoder
- localized convention/ config strategy
- experimented with a couple of query approaches favor time to start streaming, total time etc. 

- done - write required attributes with strategy

---
TODO

- think we have to invert it, instead of trying to infer, use an explicit configuration.
	- It's part of the task.
	- meaning use spring.

- get dates correctly encoded  (to enable verificiation of filter date range)
- pass table as paraemter to encoder strategy (not sure)
- write multiple files (and get the writable instantiation working ) (pass at the time). 
- make a tomcat service


- add support to parser/expr for geometry for spatial constraint choice
- factor encoder strategy chooser - so it's not instantiated, or else use property setters instead of constructor for the writer. 
- postgres - parametize query parameters, rather than use text
- need time,lat,lon
- try on another timeseries, / implement for trajectory
- compose with spring ?
- make it a tomcat webapp
- zip the file stream
- factor into classes
- command-line driven


REFS
http://www.unidata.ucar.edu/software/thredds/current/netcdf-java/tutorial/NetcdfFileWriteable.html

http://www.unidata.ucar.edu/software/netcdf/docs/BestPractices.html

https://www.unidata.ucar.edu/software/netcdf/docs/netcdf/CDL-Data-Types.html
