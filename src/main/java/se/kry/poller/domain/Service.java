package se.kry.poller.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.net.URL;
import java.time.Instant;
import java.util.Objects;

@Entity
public class Service {
    // Coming from a Scala background my preference is for immutable objects, so I don't
    // particularly like how the fields are not final here, nor the setters. However, I believe
    // at least some of this is needed for the persistence libraries to work, including the empty
    // constructor.

    private @Id @GeneratedValue long id;
    private ServiceStatus status;
    private String name;
    private URL url;
    private Instant created;
    private Instant lastUpdated;

    public Service() {
    }

    @JsonCreator // Force JSON deserialisation to use this constructor
    public Service(String name, URL url) {
        this.status = ServiceStatus.UNKNOWN;
        this.name = name;
        this.url = url;
        this.created = Instant.now();
        this.lastUpdated = this.created;
    }

    @PersistenceConstructor // Force the persistence to use this constructor
    public Service(ServiceStatus status, String name, URL url, Instant created, Instant lastUpdated) {
        this.status = status;
        this.name = name;
        this.url = url;
        this.created = created;
        this.lastUpdated = lastUpdated;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return id == service.id &&
                status == service.status &&
                Objects.equals(name, service.name) &&
                Objects.equals(url, service.url) &&
                Objects.equals(created, service.created) &&
                Objects.equals(lastUpdated, service.lastUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, name, url, created, lastUpdated);
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", status=" + status +
                ", name='" + name + '\'' +
                ", url=" + url +
                ", created=" + created +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
