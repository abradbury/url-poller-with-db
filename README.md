# Service Poller

## Problem statement
> As a part of scaling the number of services running within a modern health tech company we need a 
> way to make sure that all are running smoothly. None of the tools that we have been looking for 
> are quite doing the right thing for us so we decided that we need to build it ourselves. What we 
> want you to do is to build a simple service poller that keeps a list of services (defined by a 
> URL), and periodically does a HTTP GET to each and saves the response ("OK" or "FAIL"). Apart 
> from the polling logic we want to have all the services visualised and easily managed in a basic 
> UI presenting the all services together with their status.

## Usage
```shell
mvn spring-boot:run
```
The UI will now be available at http://localhost:8080. The API is also served on port 8080, for 
example: http://localhost:8080/v1/services. If you wish, the database provides a GUI that can be 
accessed at http://localhost:8080/h2 using the credentials and database path in the 
`application.properties` file.

## Summary
Separation of concerns was important to me when designing this solution. Whilst I could have used 
a templating solution, I wanted to ensure that the front-end was separated from the backend via a 
RESTful API. This would make it relatively easy to swap the front-end solution and allow other 
clients to hook into the API.

I initially started this solution using Vert.X and Gradle, as these were mentioned in the problem 
description as the technologies you use. As it isn't often that you need to build a full web stack 
from scratch in under 4 hours, I thought I would give your technologies a go. However, that did not 
go too well - I spent a lot of time and was not able to meet the minimum requirements of the 
problem. I switched to Spring Boot (after using it for another take-home assignment) and was able 
to get a near-fully running solution (with API, UI and embedded database) ready in a short amount 
of time. I then worked on the polling, tests and general polishing. My earlier decision to separate 
the front-end via a REST API made it very easy to transfer it to the Spring Boot solution.

On the whole, I'm quite pleased with this solution. It avoids a lot of the boilerplate code that 
web and database Java applications are often littered with. There are certainly ways that it can 
be improved, such as better error handling, validation and tests, but I think it does the basics 
ok. And the front-end isn't too bad for static HTML and vanilla JavaScript (thanks Bootstrap CSS!).
Some of the key areas of improvement and points from this solution are discussed below.

## Backend
* The `PollingClient` is very basic. Currently, it polls all stored services at the same time, 
  which I don't like. My ideal design would be when a service is added, it is polled immediately 
  and is stored with a `lastChecked` timestamp. A timer is then set to schedule the next poll for 
  that specific service. The benefit of this is that services will be polled at different times, 
  which spreads the load. Perhaps when adding the service, a user would be able to select the 
  polling interval? Though some validation to prevent denial-of-service attacks from 1 millisecond 
  polling periods would be needed. There may also be a button on the UI that would allow users to 
  poll a specific service when clicked. The polling period is currently set to 60 seconds, defined 
  in `application.properties` in milliseconds.

* When the application starts up, it would also check the database and look at the `lastChecked` 
  timestamp for each service and compare against the current time and the polling interval. If 
  scheduled poll was missed because the application was down, it would poll immediately, else it 
  would work out when the next poll is due and schedule that before resorting to the normal polling 
  period for the service.

* The polling service also doesn't specify any timeouts, which it should really i.e. how long to 
  wait before marking a request as failed.

* The `ServicesController` provides a basic REST API service. The biggest issue with this is with 
  the POST handler: it is very relaxed about what JSON it takes as being valid, which causes many 
  of the fields in the resulting `Service` object to be `null`. This should be fixed at a later 
  date. The API is also missing a PUT endpoint to update the existing services and the GET 
  endpoint is not used in earnest, just in the tests. Another issue is that duplicate services can 
  be added, which should be relatively easy to fix, but it hasn't been done in this initial work.

* The logging of polls for services is set at INFO level so that is shown by default. This is too 
  verbose for production, but sufficient for review and development. Until a central logging 
  configuration is set up, to tone these down you will need to change them from `LOGGER.info` to 
  `LOGGER.debug` in `PollingClient`.
  
* The embedded database is configured to store data into a file in the current working directory 
  (so as to not clutter your computer - it defaulted to the home directory). You will see this as 
  a `database.mv.db` file in the project's root directory. This can be configured using the 
  `spring.datasource.url` fields in `application.json`.

* Some unit tests don't pass, this is because they should, but I haven't got time to fix them in 
  this initial work.

## Frontend
* Plain HTML and JavaScript to keep things simple
* Uses Bootstrap CSS to make it look ok. Normally, I would spend more time on the look, feel and 
  especially user experience (UX) of a web application, as getting the UX right is important to me, 
  but I haven't had time to do this here
* Datetime values would be more useful if they were relative e.g. "5 minutes ago"
* Expand the action column to allow editing of services
* No minification (or testing!)
* It has a delete button, which is useful
* It does not have auto-refreshing, so once you add a service, it will come with status 'UNKNOWN'. 
  The default polling period for the backend is 60 seconds, as described above (can be changed). 
  Once, this time has passed, if you refresh the page you should see the new status.
