package cn.keking.utils;

import cn.keking.config.ConfigConstants;
import cn.keking.model.FileAttribute;
import cn.keking.model.ReturnResponse;
import cn.keking.service.cache.CacheService;
import io.mola.galimatias.GalimatiasParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static cn.keking.utils.KkFileUtils.*;

/**
 * @author yudian-it
 */
@Component
public class DownloadUtils {

    private final static Logger logger = LoggerFactory.getLogger(DownloadUtils.class);
    private static final String fileDir = ConfigConstants.getFileDir();
    private static final String URL_PARAM_FTP_USERNAME = "ftp.username";
    private static final String URL_PARAM_FTP_PASSWORD = "ftp.password";
    private static final String URL_PARAM_FTP_CONTROL_ENCODING = "ftp.control.encoding";

    private CacheService cacheService;

    @Autowired
    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * 自身的静态常量
     */
    private static final DownloadUtils DOWNLOAD_UTILS = new DownloadUtils();

    private DownloadUtils() {
        //私有构造方法
    }

    @PostConstruct
    private void init() {
        DOWNLOAD_UTILS.setCacheService(this.cacheService);
    }

    /**
     * @param fileAttribute fileAttribute
     * @param fileName      文件名
     * @return 本地文件绝对路径
     */
    public static ReturnResponse<String> downLoad(FileAttribute fileAttribute, String fileName) {
        String urlStr = fileAttribute.getUrl();
        ReturnResponse<String> response = new ReturnResponse<>(0, "下载成功!!!", "");
        String realPath = DownloadUtils.getRelFilePath(fileName, fileAttribute);
        try {
            URL url = WebUtils.normalizedURL(urlStr);
            //获取源文件修改时间
            String sourceModifiedTime = KkFileUtils.getFileModifiedTime(url);
            boolean flag = true;
            if (isHttpUrl(url) || isFileUrl(url)) {
                File realFile = new File(realPath);
                FileUtils.copyURLToFile(url, realFile);
            } else if (isFtpUrl(url)) {
                String ftpUsername = WebUtils.getUrlParameterReg(fileAttribute.getUrl(), URL_PARAM_FTP_USERNAME);
                String ftpPassword = WebUtils.getUrlParameterReg(fileAttribute.getUrl(), URL_PARAM_FTP_PASSWORD);
                String ftpControlEncoding = WebUtils.getUrlParameterReg(fileAttribute.getUrl(), URL_PARAM_FTP_CONTROL_ENCODING);
                FtpUtils.download(fileAttribute.getUrl(), realPath, ftpUsername, ftpPassword, ftpControlEncoding);
            } else {
                flag = false;
                response.setCode(1);
                response.setMsg("url不能识别url" + urlStr);
            }
            //将临时文件名称以及源文件更新日期存入缓存
            if (flag) {
                Map<String, String> map = new HashMap<>(2);
                map.put(CacheService.TEMP_FILE_NAME_KEY, realPath.substring(fileDir.length()));
                map.put(CacheService.SOURCE_FILE_MODIFIED_TIME_KEY, sourceModifiedTime);
                DOWNLOAD_UTILS.cacheService.putTempFileCache(fileAttribute.getName(), map);
                //清理转换后的缓存
                DOWNLOAD_UTILS.cacheService.cleanConvertedCache(fileAttribute.getName());
            }
            response.setContent(realPath);
            response.setMsg(fileName);
            return response;
        } catch (IOException | GalimatiasParseException e) {
            logger.error("文件下载失败，url：{}", urlStr, e);
            response.setCode(1);
            response.setContent(null);
            if (e instanceof FileNotFoundException) {
                response.setMsg("文件不存在!!!");
            } else {
                response.setMsg(e.getMessage());
            }
            return response;
        }
    }


    /**
     * 获取真实文件绝对路径
     *
     * @param fileName 文件名
     * @return 文件路径
     */
    private static String getRelFilePath(String fileName, FileAttribute fileAttribute) {
        String type = fileAttribute.getSuffix();
        if (null == fileName) {
            UUID uuid = UUID.randomUUID();
            fileName = uuid + "." + type;
        } else { // 文件后缀不一致时，以type为准(针对simText【将类txt文件转为txt】)
            fileName = fileName.replace(fileName.substring(fileName.lastIndexOf(".") + 1), type);
        }
        String realPath = fileDir + fileName;
        File dirFile = new File(fileDir);
        if (!dirFile.exists() && !dirFile.mkdirs()) {
            logger.error("创建目录【{}】失败,可能是权限不够，请检查", fileDir);
        }
        return realPath;
    }

    /**
     * 获取可用的临时文件路径，可用代表拥有以及最新。
     *
     * @param fileAttribute 文件属性
     * @return 临时文件路径；null->临时文件不可用
     */
    public static String getAvailableTempFilePath(FileAttribute fileAttribute) {
        String tempFileName = DOWNLOAD_UTILS.cacheService.getTempFileName(fileAttribute.getName());
        if (tempFileName != null) {
            String modifiedTimeCache = DOWNLOAD_UTILS.cacheService.getTempFileSourceUpdateTime(fileAttribute.getName());
            try {
                //缓存不为空并且修改时间一致说明该临时文件可用
                if (StringUtils.isNotEmpty(modifiedTimeCache) && modifiedTimeCache.equals(KkFileUtils.getFileModifiedTime(WebUtils.normalizedURL(fileAttribute.getUrl())))) {
                    String filePath = fileDir + tempFileName;
                    File file = new File(filePath);
                    //判断临时文件是否存在
                    if (file.exists()) {
                        logger.debug("url={}的临时文件存在，{}!", fileAttribute.getUrl(), filePath);
                        return filePath;
                    }
                    logger.debug("临时文件{}不存在!", filePath);
                }
            } catch (IOException | GalimatiasParseException e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
        return null;
    }
}
