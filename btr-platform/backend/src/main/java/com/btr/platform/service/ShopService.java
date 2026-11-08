package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.Product;
import com.btr.platform.entity.ShopOrder;
import com.btr.platform.mapper.ProductMapper;
import com.btr.platform.mapper.ShopOrderMapper;
import com.btr.platform.security.UserPrincipal;
import com.btr.platform.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShopService {

    private final ProductMapper productMapper;
    private final ShopOrderMapper orderMapper;

    public ShopService(ProductMapper productMapper, ShopOrderMapper orderMapper) {
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
    }

    public List<Product> adminProducts() {
        return productMapper.selectList(new QueryWrapper<Product>().orderByDesc("id"));
    }

    public List<Product> publicProducts() {
        return productMapper.selectList(new QueryWrapper<Product>()
                .eq("status", "ACTIVE")
                .orderByDesc("id"));
    }

    public Product product(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || !"ACTIVE".equals(product.status)) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在或已下架");
        }
        return product;
    }

    @Transactional
    public Product createProduct(P1Dtos.ProductRequest request) {
        Product product = new Product();
        product.tenantId = TenantContext.require();
        product.name = request.name();
        product.description = request.description();
        product.cover = request.cover();
        product.price = request.price();
        product.stock = request.stock();
        product.status = "ACTIVE";
        product.createdAt = LocalDateTime.now();
        product.updatedAt = LocalDateTime.now();
        productMapper.insert(product);
        return product;
    }

    @Transactional
    public Product updateProduct(Long id, P1Dtos.ProductRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        product.name = request.name();
        product.description = request.description();
        product.cover = request.cover();
        product.price = request.price();
        product.stock = request.stock();
        product.updatedAt = LocalDateTime.now();
        productMapper.updateById(product);
        return product;
    }

    @Transactional
    public Product updateStatus(Long id, String status) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        product.status = status;
        product.updatedAt = LocalDateTime.now();
        productMapper.updateById(product);
        return product;
    }

    @Transactional
    public ShopOrder createOrder(P1Dtos.ShopOrderRequest request, UserPrincipal principal) {
        if (request.quantity() <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "数量必须大于 0");
        }
        Product product = product(request.productId());
        Long tenantId = TenantContext.require();
        int updated = productMapper.deductStock(product.id, tenantId, request.quantity());
        if (updated == 0) {
            throw new BizException(ErrorCode.CONFLICT, "库存不足");
        }
        ShopOrder order = new ShopOrder();
        order.tenantId = tenantId;
        order.productId = product.id;
        order.userId = principal.userId();
        order.quantity = request.quantity();
        order.amount = product.price.multiply(BigDecimal.valueOf(request.quantity()));
        order.status = "PENDING";
        order.contactName = request.contactName();
        order.contactPhone = request.contactPhone();
        order.address = request.address();
        order.note = request.note();
        order.createdAt = LocalDateTime.now();
        orderMapper.insert(order);
        return order;
    }

    public List<ShopOrder> mine(Long userId) {
        return orderMapper.selectList(new QueryWrapper<ShopOrder>()
                .eq("user_id", userId)
                .orderByDesc("id"));
    }

    public List<ShopOrder> adminOrders(String status) {
        QueryWrapper<ShopOrder> query = new QueryWrapper<ShopOrder>().orderByDesc("id");
        if (status != null && !status.isBlank()) {
            query.eq("status", status);
        }
        return orderMapper.selectList(query);
    }

    @Transactional
    public ShopOrder markPaid(Long id) {
        ShopOrder order = order(id);
        order.status = "PAID";
        order.paidAt = LocalDateTime.now();
        orderMapper.updateById(order);
        return order;
    }

    @Transactional
    public ShopOrder ship(Long id, String trackingNo) {
        ShopOrder order = order(id);
        order.status = "SHIPPED";
        order.shippedAt = LocalDateTime.now();
        order.trackingNo = trackingNo;
        orderMapper.updateById(order);
        return order;
    }

    @Transactional
    public ShopOrder complete(Long id) {
        ShopOrder order = order(id);
        order.status = "COMPLETED";
        orderMapper.updateById(order);
        return order;
    }

    @Transactional
    public ShopOrder refund(Long id) {
        ShopOrder order = order(id);
        if (!"REFUNDED".equals(order.status)) {
            order.status = "REFUNDED";
            orderMapper.updateById(order);
            productMapper.restoreStock(order.productId, TenantContext.require(), order.quantity);
        }
        return order;
    }

    private ShopOrder order(Long id) {
        ShopOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }
}
