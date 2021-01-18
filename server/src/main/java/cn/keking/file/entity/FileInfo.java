package cn.keking.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 文件信息
 * <br/>
 * 实际上为文件上传日志
 * @author yangbiao
 * @date 2021/1/15 10:50
 */

@Data
@TableName("file_upload_log")
public class FileInfo {

    /**
     * 主键id
     */
    private String id;

    /**
     * 文件实际全路径,包含文件名
     */
    private String fullPath;

    /**
     * 总大小
     */
    private long fileSize;

    /**
     * 检验码，文件唯一性，md5
     */
    private String checkCode;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 扩展名
     */
    private String extName;

    /**
     * 文件状态
     */
    private Integer uploadStatus;

    private String createUserId;

    private String createUserName;

    private Date createTime;

    private String updateUserId;

    private String updateUserName;

    private Date updateTime;
}
