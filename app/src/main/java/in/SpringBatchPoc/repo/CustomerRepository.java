package in.SpringBatchPoc.repo;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;

import in.SpringBatchPoc.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Serializable> {

}
