package com.chheang.mengheak.logisticsapis.services.impl;

import com.chheang.mengheak.logisticsapis.common.enums.DriverStatus;
import com.chheang.mengheak.logisticsapis.config.ApplicationConfig;
import com.chheang.mengheak.logisticsapis.dto.base.PaginatedResponse;
import com.chheang.mengheak.logisticsapis.dto.driver.request.CreateDriverRequest;
import com.chheang.mengheak.logisticsapis.dto.driver.request.UpdateDriverRequest;
import com.chheang.mengheak.logisticsapis.dto.driver.response.DriverResponse;
import com.chheang.mengheak.logisticsapis.entities.Driver;
import com.chheang.mengheak.logisticsapis.entities.Warehouse;
import com.chheang.mengheak.logisticsapis.entities.auth.UserAccount;
import com.chheang.mengheak.logisticsapis.exception.BusinessRuleException;
import com.chheang.mengheak.logisticsapis.exception.ConflictException;
import com.chheang.mengheak.logisticsapis.exception.NotFoundException;
import com.chheang.mengheak.logisticsapis.mapper.DriverMapper;
import com.chheang.mengheak.logisticsapis.repositories.DriverRepository;
import com.chheang.mengheak.logisticsapis.repositories.UserAccountRepository;
import com.chheang.mengheak.logisticsapis.repositories.WarehouseRepository;
import com.chheang.mengheak.logisticsapis.services.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserAccountRepository userAccountRepository;
    private final DriverMapper driverMapper;
    private final ApplicationConfig applicationConfig;

    @Override
    public DriverResponse create(CreateDriverRequest request) {
        String employeeCode = request.getEmployeeCode().trim().toUpperCase();
        if (driverRepository.existsByEmployeeCode(employeeCode)) {
            throw ConflictException.duplicate("Driver", "employee code", employeeCode);
        }
        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw ConflictException.duplicate("Driver", "license number", request.getLicenseNumber());
        }

        Driver driver = driverMapper.toEntity(request);
        driver.setEmployeeCode(employeeCode);
        if (request.getBaseWarehouseId() != null) {
            driver.setBaseWarehouse(resolveWarehouse(request.getBaseWarehouseId()));
        }
        if (request.getUserId() != null) {
            driver.setUserAccount(resolveUser(request.getUserId(), null));
        }

        return driverMapper.toResponse(driverRepository.save(driver));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<DriverResponse> list(DriverStatus status,
                                                  UUID warehouseId,
                                                  String keyword,
                                                  Pageable pageable) {
        Page<DriverResponse> page = driverRepository
                .search(status, warehouseId, keyword == null || keyword.isBlank() ? null : keyword.trim(), pageable)
                .map(driverMapper::toResponse);
        return PaginatedResponse.from(page, applicationConfig.getPagination().getUrlByResource("driver"));
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getOne(UUID id) {
        return driverMapper.toResponse(findOrThrow(id));
    }

    @Override
    public DriverResponse update(UUID id, UpdateDriverRequest request) {
        Driver driver = findOrThrow(id);

        if (request.getLicenseNumber() != null
                && !request.getLicenseNumber().equals(driver.getLicenseNumber())
                && driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw ConflictException.duplicate("Driver", "license number", request.getLicenseNumber());
        }

        // pulling a driver off duty mid-trip would leave the trip without a driver
        if (request.getStatus() != null
                && request.getStatus() != DriverStatus.ON_TRIP
                && driver.getStatus() == DriverStatus.ON_TRIP) {
            throw new BusinessRuleException(
                    "Driver %s is on a trip, complete or cancel the trip first".formatted(driver.getEmployeeCode()));
        }

        if (request.getLicenseExpiry() != null && request.getLicenseExpiry().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("license expiry cannot be in the past");
        }

        driverMapper.update(driver, request);
        if (request.getBaseWarehouseId() != null) {
            driver.setBaseWarehouse(resolveWarehouse(request.getBaseWarehouseId()));
        }
        if (request.getUserId() != null) {
            driver.setUserAccount(resolveUser(request.getUserId(), driver.getId()));
        }

        return driverMapper.toResponse(driverRepository.save(driver));
    }

    private Driver findOrThrow(UUID id) {
        return driverRepository.findById(id).orElseThrow(() -> NotFoundException.of("Driver", id));
    }

    private Warehouse resolveWarehouse(UUID warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> NotFoundException.of("Warehouse", warehouseId));
    }

    private UserAccount resolveUser(UUID userId, UUID currentDriverId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.of("User account", userId));

        driverRepository.findByUserAccountId(userId)
                .filter(existing -> !existing.getId().equals(currentDriverId))
                .ifPresent(existing -> {
                    throw new ConflictException("User account %s is already linked to driver %s"
                            .formatted(userId, existing.getEmployeeCode()));
                });

        return user;
    }
}
