package cn.keking.file.service;

import cn.keking.file.entity.FileInfo;
import cn.keking.file.facade.FileInfoFacade;
import cn.keking.file.mapper.FileInfoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 用于查询文件信息
 *
 * @author yangbiao
 * @date 2021/1/15 11:11
 */
@Service
public class FileInfoService extends ServiceImpl<FileInfoMapper, FileInfo> implements FileInfoFacade {

    @Value("${nas.dir}")
    private String nasDir;

    @Override
    public String getFileUrlByIdAndStatus(String id) {
        QueryWrapper<FileInfo> qw = new QueryWrapper<>();
        //上传成功的文件
        qw.eq("id", id).eq("upload_status", 1);
        FileInfo fileInfo = this.getOne(qw);
        if (fileInfo == null) {
            return null;
        }
        //通过nas访问相当于本地访问需要加file:///
        return "file:///" + nasDir + fileInfo.getFullPath();
    }
}
