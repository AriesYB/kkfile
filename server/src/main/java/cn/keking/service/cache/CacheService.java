package cn.keking.service.cache;

import java.util.List;
import java.util.Map;

/**
 * @author: chenjh
 * @since: 2019/4/2 16:45
 */
public interface CacheService {
    /**
     * 已经转换的文件
     */
    String FILE_PREVIEW_FILE_KEY = "converted-preview-file";
    /**
     * 压缩包内图片文件集合
     */
    String FILE_PREVIEW_IMGS_KEY = "converted-preview-imgs-file";
    String FILE_PREVIEW_PDF_IMGS_KEY = "converted-preview-pdfimgs-file";
    String TASK_QUEUE_NAME = "convert-task";
    String FILE_PREVIEW_TEMP_FILE_KEY = "converted-preview-temp-file";
    String TEMP_FILE_NAME_KEY = "tempFile";
    String SOURCE_FILE_MODIFIED_TIME_KEY = "sourceFileModifiedTime";

    Integer DEFAULT_PDF_CAPACITY = 500000;
    Integer DEFAULT_IMG_CAPACITY = 500000;
    Integer DEFAULT_PDFIMG_CAPACITY = 500000;
    Integer DEFAULT_TEMP_FILE_CAPACITY = 500000;

    void initPDFCachePool(Integer capacity);

    void initIMGCachePool(Integer capacity);

    void initPdfImagesCachePool(Integer capacity);

    void putConvertedCache(String key, String value);

    void putImgCache(String key, List<String> value);

    Map<String, String> getConvertedCache();

    String getConvertedCache(String key);

    Map<String, List<String>> getImgCache();

    List<String> getImgCache(String key);

    Integer getPdfImageCache(String key);

    void putPdfImageCache(String pdfFilePath, int num);

    void cleanCache();

    void addQueueTask(String url);

    String takeQueueTask() throws InterruptedException;

    /**
     * 增加临时文件缓存
     *
     * @param key   key
     * @param value value
     */
    void putTempFileCache(String key, Map<String, String> value);

    /**
     * 根据key获取缓存
     *
     * @param key key
     */
    Map<String, String> getTempFileCache(String key);

    /**
     * JDK缓存初始化
     *
     * @param capacity 缓存容量
     */
    void initTempFileCache(Integer capacity);

    /**
     * 获取临时文件缓存中的临时文件名称
     *
     * @param key key
     * @return 临时文件名称
     */
    default String getTempFileName(String key) {
        Map<String, String> map = getTempFileCache(key);
        if (map == null) {
            return null;
        }
        return getTempFileCache(key).get(TEMP_FILE_NAME_KEY);
    }

    /**
     * 获取临时文件缓存中的源文件修改时间
     *
     * @param key key
     * @return 源文件修改时间
     */
    default String getTempFileSourceUpdateTime(String key) {
        Map<String, String> map = getTempFileCache(key);
        if (map == null) {
            return null;
        }
        return map.get(SOURCE_FILE_MODIFIED_TIME_KEY);
    }

    /**
     * 清理转换过的文件缓存
     *
     * @param key key
     */
    void cleanConvertedCache(String key);
}
