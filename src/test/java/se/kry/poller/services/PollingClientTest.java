package se.kry.poller.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import se.kry.poller.domain.Service;
import se.kry.poller.domain.ServiceStatus;
import se.kry.poller.persistence.ServicesRepository;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.List;

@SpringBootTest
class PollingClientTest {

    @MockBean
    private ServicesRepository repository;
    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private PollingClient pollingClient;

    @Test
    public void poller_whenNoServices() {
        Mockito.when(repository.findAll()).thenReturn(List.of());

        pollingClient.checkStatusOfServices();

        Mockito.verify(repository).findAll();
        Mockito.verifyNoInteractions(restTemplate);
    }

    @Test
    public void poller_whenServiceIsDown() throws MalformedURLException {
        String url = "https://www.kry.se";
        Instant creationTime = Instant.now();
        Service originalService = new Service(ServiceStatus.UNKNOWN, "Home page", new URL(url), creationTime, Instant.now());
        Service updatedService = new Service(ServiceStatus.FAIL, "Home page", new URL(url), creationTime, Instant.now());

        Mockito.when(repository.findAll()).thenReturn(List.of(originalService));
        Mockito.when(restTemplate.getForEntity(url, String.class)).thenThrow(RestClientException.class);

        pollingClient.checkStatusOfServices();

        Mockito.verify(repository).findAll(); // FIXME: This is called twice for some unknown reason.
        Mockito.verify(restTemplate).getForEntity(url, String.class);
        Mockito.verify(repository).save(updatedService); // FIXME: This fails because of the use of Instant.now(). Fix by injecting a time provider, that can be mocked in tests.
    }

    @Test
    public void poller_whenServiceIsUp() throws MalformedURLException {
        String url = "https://www.kry.se";
        Instant creationTime = Instant.now();
        Service originalService = new Service(ServiceStatus.UNKNOWN, "Home page", new URL(url), creationTime, Instant.now());
        Service updatedService = new Service(ServiceStatus.OK, "Home page", new URL(url), creationTime, Instant.now());

        Mockito.when(repository.findAll()).thenReturn(List.of(originalService));
        Mockito.when(restTemplate.getForEntity(url, String.class)).thenReturn(ResponseEntity.ok().build());

        pollingClient.checkStatusOfServices();

        Mockito.verify(repository).findAll();
        Mockito.verify(restTemplate).getForEntity(url, String.class);
        Mockito.verify(repository).save(updatedService); // FIXME: This fails because of the use of Instant.now(). Fix by injecting a time provider, that can be mocked in tests.
    }

}