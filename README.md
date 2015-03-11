
WHAT'S DONE
- subset expression parser/ 
- postgres dialect rewriter
- netcdf encoder
- convention/ config strategy
- experimented with a couple of query approaches favor time to start streaming, total time etc. 


---
TODO
- add support to parser/expr for geometry for spatial constraint choice
- factor encoder strategy chooser - so it's not instantiated, or else use property setters instead of constructor for the writer. 
- encode parameters, rather than text
- need time,lat,lon
- try on another timeseries, / implement for trajectory
- compose with spring ?
- make it a tomcat webapp
- zip the file stream
- factor into classes
- command-line driven
