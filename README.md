# peoplesort

A hybrid command-line application/REST service for rudimentary processing of information about people, existing only as an example of one way of building both CLAs and REST services in Clojure.

Here is the CLA help output:

    Usage:
        peoplesort options* files*

     If help is requested, we print this to console and exit.

     If files are provided, we parse, merge, report to console, and exit.

     Files must have, in any order, an initial row with these column headers:

       LastName | FirstName | Gender | FavoriteColor | DateOfBirth

     Columns may be delimited with pipes, as shown, or commas or spaces. The data
     may not include delimiters. Do not use more than one space anywhere.

     If no files are provided, a service will be started on PORT or 3000.

     Options:
      -h, --help 

The service endpoints all return JSON, with POSTs returning ["count" \<count\>]:

* `POST /records?person=<col data>` - Post a single data line in any of the 3 formats supported
* `POST /records/bulk?persons=[<col-data>...]` - Post array of data lines in any of the 3 formats supported
* `POST /records/reset` - Clear all records posted. (No records are stored between server launches.)
* `GET /records/gender` - returns records sorted by gender
* `GET /records/birthdate` - returns records sorted by birthdate
* `GET /records/name` - returns records sorted by name
* `GET /records/orderedby?sortkeys=[sort-spec*]` - returns records sorted as specified. Sample sort-spec:
     [["Gender", "asc"],["LastName","dsc"],["FirstName\","asc"]]
* `GET /records/count` - returns ["count" \<count\>

The service looks for the environment variable PORT, defaulting to 3000. It can be started from source:

`lein ring server` or `lein ring server-headless`

...or using the binary:

`./bin/peoplesort`

Then in a second terminal:

`curl -XGET 'http://localhost:3000/records/count`

Even better, install `jq` to get the output pretty-printed:

   `brew install jq`
   
   ...and:
   
   `curl -XGET 'http://localhost:3000/records/count | jq`

## Assumptions
Some assumptions are explicit in the above CLA help, including that any number of files may be provided.

## Testing
Regression tests:

   `lein test`

Ad hoc tests: he resources directory contains a number of test files, and a subdirectory "bad" of invalid test files. These can be tested using the prebuilt binary:

  `./bin/peoplesort resources/pipes.csv resources/spaces.csv resources/commas.csv`
 
 To start the service, just:
 
  `.bin/peoplesort`
  
 Then add some persons, eg:
 * `curl -XPOST 'http://localhost:3000/records?person=Adams|Bob|male|red|2012-12-31'`
 *`curl -XPOST 'http://localhost:3000/records?person=Turner|Tina|female|gold|1939-11-26'`
   
 And confirm:
 * `curl -XPOST 'http://localhost:3000/records/birthdate'`
 * `curl -XPOST 'http://localhost:3000/records/gender'`
 * `curl -XPOST 'http://localhost:3000/records/name'`
   
 ## Building
 After modifying the app, rebuild the binary thus:
 
   `lein bin`
 
 # Fun Stuff
 Here we offer random notes on the project that might benefit others as daft as I.
 ## Stumpers
 One killer was the dread "Invalid anti-forgery token" error. The fix? Steal code more carefully. Most Ring examples have something like:
*    `(def app (wrap-json-response (wrap-defaults app-routes site-defaults)))`

...but we are developing a service, and `site-defaults` are perfect. For web sites. For a service we want:
*    `(def app (wrap-json-response (wrap-defaults app-routes api-defaults)))`
    
 ## Suggested enhancements
 * Offer a combined execution in which input files are loaded and then served.
 * More detailed parsing errors.
 * Look at the  SQL "order by". How hard would a "where" clause be?
