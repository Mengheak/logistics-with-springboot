package com.chheang.mengheak.logisticsapis.services.impl;

import com.chheang.mengheak.logisticsapis.config.ApplicationConfig;
import com.chheang.mengheak.logisticsapis.dto.base.PaginatedResponse;
import com.chheang.mengheak.logisticsapis.dto.customer.request.CreateCustomerRequest;
import com.chheang.mengheak.logisticsapis.dto.customer.request.UpdateCustomerRequest;
import com.chheang.mengheak.logisticsapis.dto.customer.response.CustomerResponse;
import com.chheang.mengheak.logisticsapis.entities.Customer;
import com.chheang.mengheak.logisticsapis.entities.auth.UserAccount;
import com.chheang.mengheak.logisticsapis.exception.ConflictException;
import com.chheang.mengheak.logisticsapis.exception.NotFoundException;
import com.chheang.mengheak.logisticsapis.mapper.CustomerMapper;
import com.chheang.mengheak.logisticsapis.repositories.CustomerRepository;
import com.chheang.mengheak.logisticsapis.repositories.UserAccountRepository;
import com.chheang.mengheak.logisticsapis.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserAccountRepository userAccountRepository;
    private final CustomerMapper customerMapper;
    private final ApplicationConfig applicationConfig;

    @Override
    public CustomerResponse create(CreateCustomerRequest request) {
        String code = request.getCustomerCode().trim().toUpperCase();
        if (customerRepository.existsByCustomerCode(code)) {
            throw ConflictException.duplicate("Customer", "customer code", code);
        }

        Customer customer = customerMapper.toEntity(request);
        customer.setCustomerCode(code);
        if (request.getUserId() != null) {
            customer.setUserAccount(resolveUser(request.getUserId(), null));
        }

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CustomerResponse> list(String keyword, Boolean active, Pageable pageable) {
        Page<CustomerResponse> page = customerRepository
                .search(blankToNull(keyword), active, pageable)
                .map(customerMapper::toResponse);
        return PaginatedResponse.from(page, applicationConfig.getPagination().getUrlByResource("customer"));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getOne(UUID id) {
        return customerMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getByCode(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode.trim().toUpperCase())
                .orElseThrow(() -> NotFoundException.of("Customer", customerCode));
        return customerMapper.toResponse(customer);
    }

    @Override
    public CustomerResponse update(UUID id, UpdateCustomerRequest request) {
        Customer customer = findOrThrow(id);
        customerMapper.update(customer, request);

        if (request.getUserId() != null) {
            customer.setUserAccount(resolveUser(request.getUserId(), customer.getId()));
        }

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    /**
     * Customers are deactivated rather than deleted: their shipments and invoices must keep
     * pointing at a real row.
     */
    @Override
    public void deactivate(UUID id) {
        Customer customer = findOrThrow(id);
        customer.setActive(false);
        customerRepository.save(customer);
    }

    private Customer findOrThrow(UUID id) {
        return customerRepository.findById(id).orElseThrow(() -> NotFoundException.of("Customer", id));
    }

    /** One login maps to at most one customer, so reject a link that another customer already owns. */
    private UserAccount resolveUser(UUID userId, UUID currentCustomerId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.of("User account", userId));

        customerRepository.findByUserAccountId(userId)
                .filter(existing -> !existing.getId().equals(currentCustomerId))
                .ifPresent(existing -> {
                    throw new ConflictException("User account %s is already linked to customer %s"
                            .formatted(userId, existing.getCustomerCode()));
                });

        return user;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
