package com.smarthome.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthome.common.exception.BusinessException;
import com.smarthome.common.result.PageResult;
import com.smarthome.model.entity.Product;
import com.smarthome.model.entity.ThingsModel;
import com.smarthome.model.mapper.ProductMapper;
import com.smarthome.model.mapper.ThingsModelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 产品管理服务
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final ThingsModelMapper thingsModelMapper;

    /**
     * 添加产品
     */
    public Product addProduct(Product product) {
        product.setStatus(1);
        productMapper.insert(product);
        return product;
    }

    /**
     * 更新产品
     */
    public void updateProduct(Product product) {
        productMapper.updateById(product);
    }

    /**
     * 删除产品
     */
    public void deleteProduct(Long productId) {
        // 检查是否有关联设备
        // TODO: 检查设备关联
        productMapper.deleteById(productId);
    }

    /**
     * 获取产品详情
     */
    public Product getById(Long productId) {
        return productMapper.selectById(productId);
    }

    /**
     * 分页查询产品
     */
    public PageResult<Product> listProducts(String productName, int pageNum, int pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (productName != null && !productName.isEmpty()) {
            wrapper.like(Product::getProductName, productName);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        Page<Product> page = productMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    /**
     * 获取产品列表（下拉选择用）
     */
    public List<Product> listAll() {
        return productMapper.selectList(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1));
    }

    /**
     * 添加物模型
     */
    public void addThingsModel(ThingsModel thingsModel) {
        thingsModelMapper.insert(thingsModel);
    }

    /**
     * 更新物模型
     */
    public void updateThingsModel(ThingsModel thingsModel) {
        thingsModelMapper.updateById(thingsModel);
    }

    /**
     * 删除物模型
     */
    public void deleteThingsModel(Long modelId) {
        thingsModelMapper.deleteById(modelId);
    }

    /**
     * 获取产品的物模型列表
     */
    public List<ThingsModel> listThingsModels(Long productId) {
        return thingsModelMapper.selectList(
                new LambdaQueryWrapper<ThingsModel>()
                        .eq(ThingsModel::getProductId, productId)
                        .orderByAsc(ThingsModel::getSortOrder)
        );
    }

    /**
     * 获取产品的属性物模型
     */
    public List<ThingsModel> listPropertyModels(Long productId) {
        return thingsModelMapper.selectList(
                new LambdaQueryWrapper<ThingsModel>()
                        .eq(ThingsModel::getProductId, productId)
                        .eq(ThingsModel::getType, 1)
                        .orderByAsc(ThingsModel::getSortOrder)
        );
    }

    /**
     * 获取产品的功能物模型
     */
    public List<ThingsModel> listFunctionModels(Long productId) {
        return thingsModelMapper.selectList(
                new LambdaQueryWrapper<ThingsModel>()
                        .eq(ThingsModel::getProductId, productId)
                        .eq(ThingsModel::getType, 2)
                        .orderByAsc(ThingsModel::getSortOrder)
        );
    }
}
