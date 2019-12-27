# peoplesort

A hybrid command-line application/REST service for rudimentary processing of information about people, existing only as an example of one way of building both CLAs and REST services in Clojure.

Here is the CLA help output:

    Usage:
        peoplesort options* files*

     If help is requested, we print this to console and exit.

     If files are provided, we parse, merge, report to console, and exit.

     Files must have, in any order, an initial row with these colun headers:

       LastName | FirstName | Gender | FavoriteColor | DateOfBirth

     Columns may be delimited with pipes, as shown, or commas or spaces. The data
     may not include delimiters.

     If no files are provided, a service will be started on PORT or 3000.

     Options:
      -h, --help 

The service endpoints all return JSON, with POSTs returning ["count" <count]:

POST /records?person=<col data> - Post a single data line in any of the 3 formats supported
POST /records/bulk?persons=[<col-data>...] - Post array of data lines in any of the 3 formats supported
POST /records/reset - Clear all records posted. (No records are stored between server launches.)
GET /records/gender - returns records sorted by gender
GET /records/birthdate - returns records sorted by birthdate
GET /records/name - returns records sorted by name
GET /records/orderedby?sortkeys=[sort-spec*] - returns records sorted as specified. Sample sort-spec:
     [["Gender", "asc"],["LastName","dsc"],["FirstName\","asc"]]
GET /records/count - returns {:count <count>}

# Assumptions
Some assumptions are explicit in the above CLA help, including that any number of files may be provided.

# Testing
Regression tests:

   `lein test`

Ad hoc tests: he resources directory contains a number of test files, and a subdirectory "bad" of invalid test files. These can be tested using the prebuilt binary:

  `./bin/peoplesort resources/pipes.csv resources/spaces.csv resources/commas.csv`
  
 After modifying the app, rebuild the binary thus:
 
   `lein bin`
   
 # Suggested enhancements
 * Offer a combined execution in which input files are loaded and then served.
 * More detailed parsing errors.
