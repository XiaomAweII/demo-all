package com.maxiaowei.demo001.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxiaowei.demo001.common.ServiceExceptionUtils;
import com.maxiaowei.demo001.dto.ShardUploadCompleteRequest;
import com.maxiaowei.demo001.dto.ShardUploadDetailResponse;
import com.maxiaowei.demo001.dto.ShardUploadInitRequest;
import com.maxiaowei.demo001.dto.ShardUploadPartRequest;
import com.maxiaowei.demo001.mapper.ShardUploadMapper;
import com.maxiaowei.demo001.mapper.ShardUploadPartMapper;
import com.maxiaowei.demo001.po.ShardUploadPO;
import com.maxiaowei.demo001.po.ShardUploadPartPO;
import com.maxiaowei.demo001.service.ShardUploadService;
import com.maxiaowei.demo001.utils.IdUtils;
import com.maxiaowei.demo001.utils.ShardUploadUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ShardUploadServiceImpl extends ServiceImpl<ShardUploadMapper, ShardUploadPO> implements ShardUploadService {
    private final String SHARD_FILE_PATH = "E:/temp/";

    @Autowired
    private ShardUploadPartMapper shardUploadPartMapper;

    @Override
    public String init(ShardUploadInitRequest request) {
        ShardUploadPO po = new ShardUploadPO();
        po.setId(IdUtils.generateId());
        po.setFileName(request.getFileName());
        po.setPartNum(request.getPartNum());
        po.setMd5(request.getMd5());

        this.save(po);
        return po.getId();
    }

    @Override
    public void uploadPart(ShardUploadPartRequest request) throws IOException {
        //如果分片已上传，则直接返回
        if (this.getUploadPartPO(request.getShardUploadId(), request.getPartOrder()) != null) {
            return;
        }
        //1、分片文件完整路径
        String partFileFullPath = this.getPartFileFullPath(request.getShardUploadId(), request.getPartOrder());
        File file = new File(partFileFullPath);
        ShardUploadUtils.createFileNotExists(file);

        //2、将分片文件落入磁盘
        request.getFile().transferTo(file);

        //3、将分片文件信息写入db中
        this.saveShardUploadPart(request, partFileFullPath);
    }

    private ShardUploadPartPO getUploadPartPO(String shardUploadId, Integer partOrder) {
        LambdaQueryWrapper<ShardUploadPartPO> wq = Wrappers.lambdaQuery(ShardUploadPartPO.class)
                .eq(ShardUploadPartPO::getShardUploadId, shardUploadId)
                .eq(ShardUploadPartPO::getPartOrder, partOrder);
        return this.shardUploadPartMapper.selectOne(wq);
    }


    @Override
    public void complete(ShardUploadCompleteRequest request) throws IOException {
        //1、获取分片任务 && 分片文件列表
        ShardUploadPO shardUploadPO = this.getById(request.getShardUploadId());
        if (shardUploadPO == null) {
            throw ServiceExceptionUtils.exception("分片任务不存在");
        }
        List<ShardUploadPartPO> shardUploadPartList = this.getShardUploadPartList(request.getShardUploadId());
        if (shardUploadPartList.size() != shardUploadPO.getPartNum()) {
            throw ServiceExceptionUtils.exception("分片还未上传完毕");
        }

        //2、合并分片文件
        File file = this.mergeFile(shardUploadPO, shardUploadPartList);

        //3、将最终的文件信息写到db中
        shardUploadPO.setFileFullPath(file.getAbsolutePath());
        this.updateById(shardUploadPO);
    }

    @Override
    public ShardUploadDetailResponse detail(String shardUploadId) {
        ShardUploadPO shardUploadPO = this.getById(shardUploadId);
        if (shardUploadPO == null) {
            return null;
        }
        List<ShardUploadPartPO> shardUploadPartList = this.getShardUploadPartList(shardUploadId);

        ShardUploadDetailResponse response = new ShardUploadDetailResponse();
        response.setShardUploadId(shardUploadId);
        response.setPartNum(shardUploadPO.getPartNum());
        response.setSuccess(Objects.equals(shardUploadPO.getPartNum(), shardUploadPartList.size()));
        response.setPartOrderList(shardUploadPartList.stream().map(ShardUploadPartPO::getPartOrder).collect(Collectors.toList()));

        return response;
    }

    /**
     * 合并文件，返回最终文件
     *
     * @param shardUploadPO
     * @param shardUploadPartList
     * @return
     * @throws IOException
     */
    private File mergeFile(ShardUploadPO shardUploadPO, List<ShardUploadPartPO> shardUploadPartList) throws IOException {
        File file = ShardUploadUtils.createFileNotExists(new File(this.getFileFullName(shardUploadPO)));

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = FileUtils.openOutputStream(file, true);
            for (ShardUploadPartPO part : shardUploadPartList) {
                File partFile = new File(part.getFileFullPath());
                FileInputStream partFileInputStream = null;
                try {
                    partFileInputStream = FileUtils.openInputStream(partFile);
                    IOUtils.copyLarge(partFileInputStream, fileOutputStream);
                } finally {
                    IOUtils.closeQuietly(partFileInputStream);
                }
                partFile.delete();
            }
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
        }

        //校验合并后的文件和目标文件的md5字是否一致
        if (StringUtils.isNotBlank(shardUploadPO.getMd5()) && !shardUploadPO.getMd5().equals(SecureUtil.md5(file))) {
            throw ServiceExceptionUtils.exception("文件md5不匹配");
        }
        return file;
    }

    /**
     * 获取分片文件列表（并按顺序排序号）
     *
     * @param shardUploadId
     * @return
     */
    private List<ShardUploadPartPO> getShardUploadPartList(String shardUploadId) {
        return this.shardUploadPartMapper.selectList(Wrappers.lambdaQuery(ShardUploadPartPO.class).eq(ShardUploadPartPO::getShardUploadId, shardUploadId).orderByAsc(ShardUploadPartPO::getPartOrder));
    }

    private ShardUploadPartPO saveShardUploadPart(ShardUploadPartRequest request, String partFileFullPath) {
        ShardUploadPartPO partPO = new ShardUploadPartPO();
        partPO.setId(IdUtils.generateId());
        partPO.setShardUploadId(request.getShardUploadId());
        partPO.setPartOrder(request.getPartOrder());
        partPO.setFileFullPath(partFileFullPath);
        this.shardUploadPartMapper.insert(partPO);
        return partPO;
    }

    private String getPartFileFullPath(String shardUploadId, Integer partOrder) {
        return String.format(SHARD_FILE_PATH + "%s/%s", shardUploadId, partOrder);
    }

    private String getFileFullName(ShardUploadPO shardUploadPO) {
        return String.format(SHARD_FILE_PATH + "%s/%s", shardUploadPO.getId(), shardUploadPO.getFileName());
    }
}
