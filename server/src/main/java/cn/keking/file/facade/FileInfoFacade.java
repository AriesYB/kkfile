package cn.keking.file.facade;

import cn.keking.file.entity.FileInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 文件信息业务接口
 *
 * @author yangbiao
 * @date 2021/1/18 14:18
 */
public interface FileInfoFacade extends IService<FileInfo> {

    /**
     * 根据文件上传日志中的id获取文件路径生成url
     *
     * @param id 文件的id
     * @return {@link String}文件的Url;未查询到文件时返回null。
     */
    String getFileUrlByIdAndStatus(String id);

}
