package se.kry.poller.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.kry.poller.domain.Service;
import se.kry.poller.domain.ServiceStatus;
import se.kry.poller.persistence.ServicesRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ServicesControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ServicesRepository repository;

    // Note: For debugging add .andDo(print()) after the .perform() call to see the request detail
    // Note: The tests below are basic and may not be as high a quality as they should be

    @Test
    public void getService_whereServiceExists() throws Exception {
        Service service = new Service(ServiceStatus.OK, "Home page", new URL("https://www.kry.se"), Instant.now(), Instant.now());
        long serviceID = service.getId();

        Mockito.when(repository.findById(serviceID)).thenReturn(Optional.of(service));

        mockMvc.perform(get("/v1/services/" + serviceID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(service)));

        Mockito.verify(repository).findById(serviceID);
    }

    @Test
    public void getService_whereServiceDoesNotExist() throws Exception {
        long serviceID = 3L;

        Mockito.when(repository.findById(serviceID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/services/" + serviceID))
                .andExpect(status().isNotFound());

        Mockito.verify(repository).findById(serviceID);
    }

    @Test
    public void getAllServices_whereThereAreServices() throws Exception {
        List<Service> services = List.of(
                new Service(ServiceStatus.OK, "Home page", new URL("https://www.kry.se"), Instant.now(), Instant.now()),
                new Service(ServiceStatus.FAIL, "Patient portal", new URL("https://patients.kry.se"), Instant.now(), Instant.now())
        );

        Mockito.when(repository.findAll()).thenReturn(services);

        mockMvc.perform(get("/v1/services"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(services)));

        Mockito.verify(repository).findAll();
    }

    @Test
    public void getAllServices_whereThereAreNoServices() throws Exception {
        List<Service> services = Collections.emptyList();

        Mockito.when(repository.findAll()).thenReturn(services);

        mockMvc.perform(get("/v1/services"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(services)));

        Mockito.verify(repository).findAll();
    }

    @Test
    public void addNewService_invalidURL() throws Exception {
        String json = "{\"id\":0,\"status\":\"FAIL\",\"name\":\"Patient portal\",\"url\":\"This is not a valid URL!\",\"created\":\"2020-11-25T09:51:54.504087Z\",\"lastUpdated\":\"2020-11-25T09:51:54.504092Z\"}";

        mockMvc.perform(post("/v1/services").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(repository);
    }

    @Test
    public void addNewService_missingFields() throws Exception {
        // FIXME: This test fails, but I would want it to fail. Spring is being far too lenient
        //  when deserialising the JSON to a Services object, leaving many fields null. I have not
        //  yet found a way to toughen it up, though I am sure there is a way to do this.

        String json = "{\"name\":\"Patient portal\"}";

        mockMvc.perform(post("/v1/services").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(repository);
    }

    @Test
    public void addNewService_invalidJSON() throws Exception {
        String json = "This is not JSON!";

        mockMvc.perform(post("/v1/services").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(repository);
    }

    @Test
    public void addNewService_noRequestBody() throws Exception {
        mockMvc.perform(post("/v1/services").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(repository);
    }

    @Test
    public void addNewService_valid() throws Exception {
        Service service = new Service(ServiceStatus.UNKNOWN, "Patient portal", new URL("https://patients.kry.se"), Instant.now(), Instant.now());
        String json = objectMapper.writeValueAsString(service);

        mockMvc.perform(post("/v1/services").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        Mockito.verify(repository).save(service);
    }

    @Test
    public void deleteService() throws Exception {
        // We can't really test much with this one because the method returns the same status code
        // regardless of if the service existed or not (which was an intentional decision as if
        // a deletion request comes in for something that doesn't exist, then it is already gone).
        long serviceID = 4L;

        mockMvc.perform(delete("/v1/services/" + serviceID))
                .andExpect(status().isOk());

        Mockito.verify(repository).deleteById(serviceID);
    }

}