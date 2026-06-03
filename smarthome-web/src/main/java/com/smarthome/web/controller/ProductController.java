package com.smarthome.web.controller;

import com.smarthome.common.result.PageResult;
import com.smarthome.common.result.R;
import com.smarthome.device.service.ProductService;
import com.smarthome.model.entity.Product;
import com.smarthome.model.entity.ThingsModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品管理 API
 */
@Tag(name = "产品管理")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "添加产品")
    @PostMapping
    public R<Product> add(@RequestBody Product product) {
        return R.ok(productService.addProduct(product));
    }

    @Operation(summary = "更新产品")
    @PutMapping
    public R<Void> update(@RequestBody Product product) {
        productService.updateProduct(product);
        return R.ok();
    }

    @Operation(summary = "删除产品")
    @DeleteMapping("/{productId}")
    public R<Void> delete(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return R.ok();
    }

    @Operation(summary = "获取产品详情")
    @GetMapping("/{productId}")
    public R<Product> getById(@PathVariable Long productId) {
        return R.ok(productService.getById(productId));
    }

    @Operation(summary = "分页查询产品")
    @GetMapping("/list")
    public R<PageResult<Product>> list(
            @RequestParam(required = false) String productName,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(productService.listProducts(productName, pageNum, pageSize));
    }

    @Operation(summary = "获取产品列表（下拉选择）")
    @GetMapping("/all")
    public R<List<Product>> listAll() {
        return R.ok(productService.listAll());
    }

    // ===== 物模型管理 =====

    @Operation(summary = "添加物模型")
    @PostMapping("/model")
    public R<Void> addModel(@RequestBody ThingsModel thingsModel) {
        productService.addThingsModel(thingsModel);
        return R.ok();
    }

    @Operation(summary = "更新物模型")
    @PutMapping("/model")
    public R<Void> updateModel(@RequestBody ThingsModel thingsModel) {
        productService.updateThingsModel(thingsModel);
        return R.ok();
    }

    @Operation(summary = "删除物模型")
    @DeleteMapping("/model/{modelId}")
    public R<Void> deleteModel(@PathVariable Long modelId) {
        productService.deleteThingsModel(modelId);
        return R.ok();
    }

    @Operation(summary = "获取产品的物模型列表")
    @GetMapping("/{productId}/models")
    public R<List<ThingsModel>> listModels(@PathVariable Long productId) {
        return R.ok(productService.listThingsModels(productId));
    }

    @Operation(summary = "获取产品的属性模型")
    @GetMapping("/{productId}/properties")
    public R<List<ThingsModel>> listPropertyModels(@PathVariable Long productId) {
        return R.ok(productService.listPropertyModels(productId));
    }

    @Operation(summary = "获取产品的功能模型")
    @GetMapping("/{productId}/functions")
    public R<List<ThingsModel>> listFunctionModels(@PathVariable Long productId) {
        return R.ok(productService.listFunctionModels(productId));
    }
}
