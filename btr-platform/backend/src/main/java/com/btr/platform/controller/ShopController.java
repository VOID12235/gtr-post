package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.Product;
import com.btr.platform.entity.ShopOrder;
import com.btr.platform.security.SecurityUtils;
import com.btr.platform.service.ShopService;
import com.btr.platform.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ShopController {

    private final ShopService shopService;
    private final TenantService tenantService;

    public ShopController(ShopService shopService, TenantService tenantService) {
        this.shopService = shopService;
        this.tenantService = tenantService;
    }

    @GetMapping("/api/v1/public/shop/products")
    public ApiResponse<List<Product>> publicProducts(@RequestParam(defaultValue = "demo") String tenantCode) {
        return ApiResponse.ok(tenantService.withTenant(tenantCode, shopService::publicProducts));
    }

    @GetMapping("/api/v1/public/shop/products/{id}")
    public ApiResponse<Product> publicProduct(@PathVariable Long id,
                                              @RequestParam(defaultValue = "demo") String tenantCode) {
        return ApiResponse.ok(tenantService.withTenant(tenantCode, () -> shopService.product(id)));
    }

    @PostMapping("/api/v1/shop/orders")
    public ApiResponse<ShopOrder> createOrder(@Valid @RequestBody P1Dtos.ShopOrderRequest request) {
        return ApiResponse.ok(shopService.createOrder(request, SecurityUtils.current()));
    }

    @GetMapping("/api/v1/shop/orders/mine")
    public ApiResponse<List<ShopOrder>> myOrders() {
        return ApiResponse.ok(shopService.mine(SecurityUtils.current().userId()));
    }

    @GetMapping("/api/v1/admin/shop/products")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<List<Product>> adminProducts() {
        return ApiResponse.ok(shopService.adminProducts());
    }

    @PostMapping("/api/v1/admin/shop/products")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<Product> createProduct(@Valid @RequestBody P1Dtos.ProductRequest request) {
        return ApiResponse.ok(shopService.createProduct(request));
    }

    @PutMapping("/api/v1/admin/shop/products/{id}")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<Product> updateProduct(@PathVariable Long id,
                                              @Valid @RequestBody P1Dtos.ProductRequest request) {
        return ApiResponse.ok(shopService.updateProduct(id, request));
    }

    @PutMapping("/api/v1/admin/shop/products/{id}/status")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<Product> updateProductStatus(@PathVariable Long id,
                                                    @Valid @RequestBody P1Dtos.ProductStatusRequest request) {
        return ApiResponse.ok(shopService.updateStatus(id, request.status()));
    }

    @GetMapping("/api/v1/admin/shop/orders")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<List<ShopOrder>> adminOrders(@RequestParam(required = false) String status) {
        return ApiResponse.ok(shopService.adminOrders(status));
    }

    @PostMapping("/api/v1/admin/shop/orders/{id}/paid")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<ShopOrder> markPaid(@PathVariable Long id) {
        return ApiResponse.ok(shopService.markPaid(id));
    }

    @PostMapping("/api/v1/admin/shop/orders/{id}/ship")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<ShopOrder> ship(@PathVariable Long id,
                                       @Valid @RequestBody P1Dtos.ShipRequest request) {
        return ApiResponse.ok(shopService.ship(id, request.trackingNo()));
    }

    @PostMapping("/api/v1/admin/shop/orders/{id}/complete")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<ShopOrder> complete(@PathVariable Long id) {
        return ApiResponse.ok(shopService.complete(id));
    }

    @PostMapping("/api/v1/admin/shop/orders/{id}/refund")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<ShopOrder> refund(@PathVariable Long id) {
        return ApiResponse.ok(shopService.refund(id));
    }
}
