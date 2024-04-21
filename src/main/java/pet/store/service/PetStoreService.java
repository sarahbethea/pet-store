package pet.store.service;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pet.store.controller.model.PetStoreData;
import pet.store.controller.model.PetStoreData.PetStoreCustomer;
import pet.store.controller.model.PetStoreData.PetStoreEmployee;
import pet.store.dao.CustomerDao;
import pet.store.dao.EmployeeDao;
import pet.store.dao.PetStoreDao;
import pet.store.entity.Customer;
import pet.store.entity.Employee;
import pet.store.entity.PetStore;

@Service
public class PetStoreService {
	
	@Autowired
	private PetStoreDao petStoreDao;
	
	@Autowired
	private EmployeeDao employeeDao;
	
	@Autowired
	private CustomerDao customerDao;
	
	

	public PetStoreData savePetStore(PetStoreData petStoreData) {
		PetStore petStore = findOrCreatePetStore(petStoreData.getPetStoreId());
		
		copyPetStoreFields(petStore, petStoreData);
		return new PetStoreData(petStoreDao.save(petStore));
		
		
	}

	private void copyPetStoreFields(PetStore petStore, PetStoreData petStoreData) {
		petStore.setPetStoreId(petStoreData.getPetStoreId());
		petStore.setPetStoreName(petStoreData.getPetStoreName());
		petStore.setPetStoreAddress(petStoreData.getPetStoreAddress());
		petStore.setPetStoreCity(petStoreData.getPetStoreCity());
		petStore.setPetStoreState(petStoreData.getPetStoreState());
		petStore.setPetStoreZip(petStoreData.getPetStoreZip());
		petStore.setPetStorePhone(petStoreData.getPetStorePhone());	
		
	}
	

	private PetStore findOrCreatePetStore(Long petStoreId) {
		PetStore petStore;
		
		if(Objects.isNull(petStoreId)) {
			petStore = new PetStore();
			
		} else {
			petStore = findPetStoreById(petStoreId);
			
		}
		return petStore;
		
	}

	private PetStore findPetStoreById(Long petStoreId) {
		return petStoreDao.findById(petStoreId).orElseThrow(() -> 
				new NoSuchElementException("Pet store with ID=" + petStoreId + " was not found."));
		
	}


	@Transactional(readOnly = false)
	public PetStoreEmployee saveEmployee(Long petStoreId, PetStoreEmployee petStoreEmployee) {
		
		PetStore petStore = findPetStoreById(petStoreId);
		Employee employee = findOrCreateEmployee(petStoreId, petStoreEmployee.getEmployeeId());
		
		copyEmployeeFields(employee, petStoreEmployee);
		
		employee.setPetStore(petStore);
		petStore.getEmployees().add(employee);
		
		
		Employee dbEmployee = employeeDao.save(employee);
		
		return new PetStoreEmployee(dbEmployee);
	
		
	}
	
	
	
	public Employee findEmployeeById(Long petStoreId, Long employeeId) {
		
		Employee employee = employeeDao.findById(employeeId).orElseThrow(() -> 
		new NoSuchElementException("Employee with ID=" + employeeId + " was not found."));
		
		if (!employee.getPetStore().getPetStoreId().equals(petStoreId)) {
			throw new IllegalArgumentException("Employee with ID=" + employeeId + 
					" does not belong to pet store with ID=" + petStoreId);
			
		} 
			return employee;
		
	}
	
	public Employee findOrCreateEmployee(Long employeeId, Long petStoreId) {
		
		Employee employee = new Employee();
		
		
		if (Objects.isNull(employeeId)) {
			employee = new Employee();
			
		} else {
			employee = findEmployeeById(employeeId, petStoreId);
			
		}
		return employee;
	}
	
	
	
	
	
	public void copyEmployeeFields(Employee employee, PetStoreEmployee petStoreEmployee) {
		employee.setEmployeeId(petStoreEmployee.getEmployeeId());
		employee.setEmployeeFirstName(petStoreEmployee.getEmployeeFirstName());
		employee.setEmployeeLastName(petStoreEmployee.getEmployeeLastName());
		employee.setEmployeeJobTitle(petStoreEmployee.getEmployeeJobTitle());
		employee.setEmployeePhone(petStoreEmployee.getEmployeePhone());
		employee.setPetStore(petStoreEmployee.getPetStore());

		
	}

	
	@Transactional(readOnly = false)
	public PetStoreCustomer saveCustomer(Long petStoreId, PetStoreCustomer petStoreCustomer) {
		
		PetStore petStore = findPetStoreById(petStoreId);
		Long customerId = petStoreCustomer.getCustomerId();
		Customer customer = findOrCreateCustomer(petStoreId, customerId);
		copyCustomerFields(customer, petStoreCustomer);
		
		if(!customer.getPetStores().contains(petStore)) {
			customer.getPetStores().add(petStore);
			petStore.getCustomers().add(customer);
		}
		
		Customer dbCustomer = customerDao.save(customer);
		return new PetStoreCustomer(dbCustomer);
		
		
	}
	
	
	
	public Customer findCustomerById(Long petStoreId, Long customerId) {
		
		Customer customer = customerDao.findById(customerId).orElseThrow(() -> 
		new NoSuchElementException("Customer with ID=" + customerId + " was not found."));
		
		boolean isAssociated = false;
		for(PetStore petStore : customer.getPetStores()) {
			if(petStore.getPetStoreId().equals(petStoreId)) {
				isAssociated = true;
				break;
			} 
		}
		if(!isAssociated) {
			throw new IllegalArgumentException("Customer is not assocaited with pet store with ID=" + petStoreId);
			
		}
		return customer;
		
	}
	
	
	
	public Customer findOrCreateCustomer(Long customerId, Long petStoreId) {
		
		Customer customer = new Customer();
		
		if(Objects.isNull(customerId)) {
			customer = new Customer();
		} else {
			customer = findCustomerById(customerId, petStoreId);
		}
		
		return customer;
	}
	
	
	
	public void copyCustomerFields(Customer customer, PetStoreCustomer petStoreCustomer) {
		customer.setCustomerId(petStoreCustomer.getCustomerId());
		customer.setCustomerFirstName(petStoreCustomer.getCustomerFirstName());
		customer.setCustomerLastName(petStoreCustomer.getCustomerLastName());
		customer.setCustomerEmail(petStoreCustomer.getCustomerEmail());
	}

	
	
	@Transactional(readOnly = true)
	public List<PetStoreData> retrieveAllPetStores() {
		
		List<PetStore> petStores = petStoreDao.findAll();
		List<PetStoreData> result = new LinkedList<>();
		
		for (PetStore petStore : petStores) {
			PetStoreData psd = new PetStoreData(petStore);
			
			psd.getCustomers().clear();
			psd.getEmployees().clear();
			
			result.add(psd);
		}
		
		return result;
		
	}

	
	@Transactional(readOnly = true)
	public PetStoreData retrievePetStoreById(Long petStoreId) {
		PetStore petStore = findPetStoreById(petStoreId);
		return new PetStoreData(petStore);
		
	}

	@Transactional(readOnly = false)
	public void deletePetStoreById(Long petStoreId) {
		PetStore petStore = findPetStoreById(petStoreId);
		petStoreDao.delete(petStore);
		
		
	}
	

}
