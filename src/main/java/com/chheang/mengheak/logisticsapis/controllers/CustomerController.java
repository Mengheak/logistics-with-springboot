package com.chheang.mengheak.logisticsapis.controllers;

import com.chheang.mengheak.logisticsapis.dto.base.Response;
import com.chheang.mengheak.logisticsapis.dto.customer.request.CreateCustomerRequest;
import com.chheang.mengheak.logisticsapis.dto.customer.request.UpdateCustomerRequest;
import com.chheang.mengheak.logisticsapis.services.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody CreateCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.created("created customer successfully", customerService.create(request)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @GetMapping
    public ResponseEntity<Response> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "active", required = false) Boolean active,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(Response.ok("retrieved customers successfully",
                customerService.list(keyword, active, pageable)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @GetMapping("/{id}")
    public ResponseEntity<Response> getOne(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(Response.ok("retrieved customer successfully", customerService.getOne(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @GetMapping("/by-code/{customerCode}")
    public ResponseEntity<Response> getByCode(@PathVariable("customerCode") String customerCode) {
        return ResponseEntity.ok(Response.ok("retrieved customer successfully",
                customerService.getByCode(customerCode)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @PatchMapping("/{id}")
    public ResponseEntity<Response> update(@PathVariable("id") UUID id,
                                           @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(Response.ok("updated customer successfully",
                customerService.update(id, request)));
    }

    /** Soft delete - the customer's shipment history has to stay intact. */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deactivate(@PathVariable("id") UUID id) {
        customerService.deactivate(id);
        return ResponseEntity.ok(Response.success("200", "success", "deactivated customer successfully"));
    }
}
