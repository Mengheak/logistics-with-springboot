package com.chheang.mengheak.logisticsapis.services.impl;

import com.chheang.mengheak.logisticsapis.config.ApplicationConfig;
import com.chheang.mengheak.logisticsapis.dto.base.PaginatedResponse;
import com.chheang.mengheak.logisticsapis.dto.warehouse.request.CreateWarehouseRequest;
import com.chheang.mengheak.logisticsapis.dto.warehouse.request.UpdateWarehouseRequest;
import com.chheang.mengheak.logisticsapis.dto.warehouse.response.WarehouseResponse;
import com.chheang.mengheak.logisticsapis.entities.Warehouse;
import com.chheang.mengheak.logisticsapis.exception.ConflictException;
import com.chheang.mengheak.logisticsapis.exception.NotFoundException;
import com.chheang.mengheak.logisticsapis.mapper.WarehouseMapper;
import com.chheang.mengheak.logisticsapis.repositories.WarehouseRepository;
import com.chheang.mengheak.logisticsapis.services.WarehouseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;
    private final ApplicationConfig applicationConfig;

    public WarehouseServiceImpl(WarehouseRepository warehouseRepository,
                                WarehouseMapper warehouseMapper,
                                ApplicationConfig applicationConfig) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseMapper = warehouseMapper;
        this.applicationConfig = applicationConfig;
    }

    @Override
    public WarehouseResponse create(CreateWarehouseRequest request) {
        String code = request.getWarehouseCode().trim().toUpperCase();
        if (warehouseRepository.existsByWarehouseCode(code)) {
            throw ConflictException.duplicate("Warehouse", "warehouse code", code);
        }

        Warehouse warehouse = warehouseMapper.toEntity(request);
        warehouse.setWarehouseCode(code);

        return warehouseMapper.toResponse(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<WarehouseResponse> list(String keyword, Boolean active, Pageable pageable) {
        Page<WarehouseResponse> page = warehouseRepository
                .search(keyword == null || keyword.isBlank() ? null : keyword.trim(), active, pageable)
                .map(warehouseMapper::toResponse);
        return PaginatedResponse.from(page, applicationConfig.getPagination().getUrlByResource("warehouse"));
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getOne(UUID id) {
        return warehouseMapper.toResponse(findOrThrow(id));
    }

    @Override
    public WarehouseResponse update(UUID id, UpdateWarehouseRequest request) {
        Warehouse warehouse = findOrThrow(id);
        warehouseMapper.update(warehouse, request);
        return warehouseMapper.toResponse(warehouseRepository.save(warehouse));
    }

    /** Hubs are deactivated, never deleted: historical shipments still reference them. */
    @Override
    public void deactivate(UUID id) {
        Warehouse warehouse = findOrThrow(id);
        warehouse.setActive(false);
        warehouseRepository.save(warehouse);
    }

    private Warehouse findOrThrow(UUID id) {
        return warehouseRepository.findById(id).orElseThrow(() -> NotFoundException.of("Warehouse", id));
    }
}
