package sk.upjs.miesici.admin;

import java.util.List;

public interface CustomerDao {
    List<Customer> getAll();

    Customer save(Customer customer);

    void edit(Customer customer);
}