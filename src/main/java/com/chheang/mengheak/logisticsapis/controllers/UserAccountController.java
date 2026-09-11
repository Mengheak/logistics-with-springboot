package com.chheang.mengheak.logisticsapis.controllers;

import com.chheang.mengheak.logisticsapis.dto.auth.response.UserAccountResponse;
import com.chheang.mengheak.logisticsapis.dto.base.PaginatedResponse;
import com.chheang.mengheak.logisticsapis.dto.base.Response;
import com.chheang.mengheak.logisticsapis.dto.user.request.CreateUserAccountRequest;
import com.chheang.mengheak.logisticsapis.dto.user.request.UpdateUserAccountRequest;
import com.chheang.mengheak.logisticsapis.services.UserAccountService;
import com.chheang.mengheak.logisticsapis.services.auth.AuthService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Staff account administration, plus the "who am I" lookup every signed-in client needs. */
@RestController
@RequestMapping("/api/v1/users")
public class UserAccountController {

    private final UserAccountService userAccountService;
    private final AuthService authService;

    public UserAccountController(UserAccountService userAccountService, AuthService authService) {
        this.userAccountService = userAccountService;
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<Response> me() {
        UserAccountResponse user = authService.currentUser();
        return ResponseEntity.ok(Response.ok("retrieved current user successfully", user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody CreateUserAccountRequest request) {
        UserAccountResponse created = userAccountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.created("created user account successfully", created));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Response> list(
            @PageableDefault(size = 10, sort = "email", direction = Sort.Direction.ASC) Pageable pageable) {
        PaginatedResponse<UserAccountResponse> page = userAccountService.list(pageable);
        return ResponseEntity.ok(Response.ok("retrieved user accounts successfully", page));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Response> getOne(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(Response.ok("retrieved user account successfully",
                userAccountService.getOne(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<Response> update(@PathVariable("id") UUID id,
                                           @Valid @RequestBody UpdateUserAccountRequest request) {
        return ResponseEntity.ok(Response.ok("updated user account successfully",
                userAccountService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> delete(@PathVariable("id") UUID id) {
        userAccountService.delete(id);
        return ResponseEntity.ok(Response.success("200", "success", "deleted user account successfully"));
    }
}
