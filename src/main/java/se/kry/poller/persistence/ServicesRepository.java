package se.kry.poller.persistence;

import se.kry.poller.domain.Service;
import org.springframework.data.jpa.repository.JpaRepository;

// Amazingly, this empty interface is all that is needed to setup basic database access.
// Though it does use the annotations on Service (labelling it as an Entity for example),
// and the configuration in application.properties.
public interface ServicesRepository extends JpaRepository<Service, Long> {

}