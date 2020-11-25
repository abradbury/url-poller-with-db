package se.kry.poller.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.kry.poller.domain.Service;
import se.kry.poller.persistence.ServicesRepository;

import java.util.List;

/**
 * Serves a simple REST API for managing the services stored in the database.
 */
@RestController
@RequestMapping(path = "/v1/services")
public class ServicesController {
    private final ServicesRepository repository;

    public ServicesController(ServicesRepository repository) {
        this.repository = repository;
    }

    @GetMapping(value = "/{serviceID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getService(@PathVariable Long serviceID) {
        return repository.findById(serviceID)
                .map(service -> new ResponseEntity<Object>(service, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Service> getServices() {
        return repository.findAll();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED) // Code returned on success
    public Service addService(@RequestBody Service service) {
        // FIXME: Need to do some validation here. Spring is creating objects with many null fields
        return repository.save(service);
    }

    @DeleteMapping(value = "/{serviceID}")
    public void deleteService(@PathVariable Long serviceID) {
        repository.deleteById(serviceID);
    }

}
