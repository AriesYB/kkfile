package cn.keking.file.service;

import cn.keking.file.entity.FileInfo;
import cn.keking.file.mapper.FileInfoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用于查询文件信息
 *
 * @author yangbiao
 * @date 2021/1/15 11:11
 */
@Service
public class FileInfoService extends ServiceImpl <FileInfoMapper,FileInfo>{

    public FileInfo getByIdAndStatus(String id){
        QueryWrapper<FileInfo> qw = new QueryWrapper();
        //上传成功的文件
        qw.eq("id",id).eq("upload_status",1);
        return this.baseMapper.selectOne(qw);
    }

}
