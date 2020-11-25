package se.kry.poller.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import se.kry.poller.domain.Service;
import se.kry.poller.domain.ServiceStatus;
import se.kry.poller.persistence.ServicesRepository;

import java.time.Instant;

/**
 * A basic polling client that periodically checks the services to see if they are up or not.
 */
@org.springframework.stereotype.Service
public class PollingClient {
    private final static Logger LOGGER = LoggerFactory.getLogger(PollingClient.class);

    private final RestTemplate restTemplate;
    private final ServicesRepository repository;

    public PollingClient(RestTemplate restTemplate, ServicesRepository repository) {
        this.restTemplate = restTemplate;
        this.repository = repository;
    }

    /**
     * Periodically polls all the services to see if they are up or not, updating their status
     * accordingly. The polling interval period is defined in the application.properties file.
     * <p>
     * Polling all services at the same time like this is not ideal, and not scalable. Ideally,
     */
    @Scheduled(fixedDelayString = "${poller.fixedDelay.in.milliseconds}")
    public void checkStatusOfServices() {
        repository.findAll().forEach(service ->
                updateServiceWithStatus(service, checkServiceStatus(service))
        );
    }

    /**
     * @param service The service to check to see if it is up or not
     * @return The status of the checked service, OK if it is up, FAIL if not
     */
    public ServiceStatus checkServiceStatus(Service service) {
        String serviceURL = service.getUrl().toExternalForm();
        LOGGER.info("Checking status of service at " + serviceURL);

        try {
            restTemplate.getForEntity(serviceURL, String.class);
            LOGGER.info("Success!");
            return ServiceStatus.OK;

        } catch (RestClientException e) {
            LOGGER.info("Failed... " + e.getMessage());
            return ServiceStatus.FAIL;
        }
    }

    /**
     * Updates the given service in the database with a new status, updating the
     * last updated time at the same time.
     */
    public void updateServiceWithStatus(Service service, ServiceStatus newStatus) {
        service.setStatus(newStatus);
        service.setLastUpdated(Instant.now());

        // Interestingly, there is no 'update' function exposed on the repository. It seems that
        // it knows that the services are keyed by their ID, so if it finds an existing service
        // with that ID, it just updates it. Otherwise, it inserts the new service. Perhaps we
        // should add some more guards here to explicitly check that the service exists and throw
        // an exception/error if it does not?
        repository.save(service);
    }

}
