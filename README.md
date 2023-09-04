# Keyword-Counter

## Introduction

This project is used to count keywords, which are defined in the application.properties, from text files and directories and sites. The emphasis is on creating a concurrent system.
It has file and web crawlers that go trought the files or sites in dept in search for the keywords. In any time the user can request the results via a query or a get command, which works with a query and futures respectivly.

## Menu

After running the program you can run the commands:
```
  ad file_name ( or directory ) - add a file name to be searched
  aw web_url - add a web url to be searched
  get web_url/file_name - waits and returns the result of the search 
  query web_url/file_name - returns the result if the search is finished
  cws - to clear web results
  cfs - to clear file results
  stop - to end the program
```
