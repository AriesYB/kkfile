package cn.keking.service.cache.impl;

import cn.keking.config.ConfigConstants;
import cn.keking.service.cache.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @auther: chenjh
 * @time: 2019/4/2 18:02
 * @description
 */
@Slf4j
@ConditionalOnExpression("'${cache.type:default}'.equals('redis')")
@Service
public class CacheServiceRedisImpl implements CacheService {

    private final RedissonClient redissonClient;

    public CacheServiceRedisImpl(Config config) {
        this.redissonClient = Redisson.create(config);
    }

    @Override
    public void initPDFCachePool(Integer capacity) {
    }

    @Override
    public void initIMGCachePool(Integer capacity) {
    }

    @Override
    public void initPdfImagesCachePool(Integer capacity) {
    }

    @Override
    public void putConvertedCache(String key, String value) {
        RMapCache<String, String> convertedList = redissonClient.getMapCache(FILE_PREVIEW_FILE_KEY);
        convertedList.fastPut(key, value);
    }

    @Override
    public void putImgCache(String key, List<String> value) {
        RMapCache<String, List<String>> convertedList = redissonClient.getMapCache(FILE_PREVIEW_IMGS_KEY);
        convertedList.fastPut(key, value);
    }

    @Override
    public Map<String, String> getConvertedCache() {
        return redissonClient.getMapCache(FILE_PREVIEW_FILE_KEY);
    }

    @Override
    public String getConvertedCache(String key) {
        RMapCache<String, String> convertedList = redissonClient.getMapCache(FILE_PREVIEW_FILE_KEY);
        return convertedList.get(key);
    }

    @Override
    public Map<String, List<String>> getImgCache() {
        return redissonClient.getMapCache(FILE_PREVIEW_IMGS_KEY);
    }

    @Override
    public List<String> getImgCache(String key) {
        RMapCache<String, List<String>> convertedList = redissonClient.getMapCache(FILE_PREVIEW_IMGS_KEY);
        return convertedList.get(key);
    }

    @Override
    public Integer getPdfImageCache(String key) {
        RMapCache<String, Integer> convertedList = redissonClient.getMapCache(FILE_PREVIEW_PDF_IMGS_KEY);
        return convertedList.get(key);
    }

    @Override
    public void putPdfImageCache(String pdfFilePath, int num) {
        RMapCache<String, Integer> convertedList = redissonClient.getMapCache(FILE_PREVIEW_PDF_IMGS_KEY);
        convertedList.fastPut(pdfFilePath, num);
    }

    @Override
    public void cleanCache() {
        cleanPdfCache();
        cleanImgCache();
        cleanPdfImgCache();
        cleanTempFileCache();
        cleanConvertingFileCache();
    }

    @Override
    public void addQueueTask(String url) {
        RBlockingQueue<String> queue = redissonClient.getBlockingQueue(TASK_QUEUE_NAME);
        queue.addAsync(url);
    }

    @Override
    public String takeQueueTask() throws InterruptedException {
        RBlockingQueue<String> queue = redissonClient.getBlockingQueue(TASK_QUEUE_NAME);
        return queue.take();
    }

    private void cleanPdfCache() {
        RMapCache<String, String> pdfCache = redissonClient.getMapCache(FILE_PREVIEW_FILE_KEY);
        pdfCache.clear();
    }

    private void cleanImgCache() {
        RMapCache<String, List<String>> imgCache = redissonClient.getMapCache(FILE_PREVIEW_IMGS_KEY);
        imgCache.clear();
    }

    private void cleanPdfImgCache() {
        RMapCache<String, Integer> pdfImg = redissonClient.getMapCache(FILE_PREVIEW_PDF_IMGS_KEY);
        pdfImg.clear();
    }

    private void cleanTempFileCache() {
        RMapCache<String, Map<String, String>> tempFileCache = redissonClient.getMapCache(FILE_PREVIEW_TEMP_FILE_KEY);
        tempFileCache.clear();
    }

    @Override
    public void putTempFileCache(String key, Map<String, String> value) {
        RMapCache<String, Map<String, String>> tempFileCache = redissonClient.getMapCache(FILE_PREVIEW_TEMP_FILE_KEY);
        tempFileCache.fastPut(key, value);
    }

    @Override
    public Map<String, String> getTempFileCache(String key) {
        RMapCache<String, Map<String, String>> tempFileCache = redissonClient.getMapCache(FILE_PREVIEW_TEMP_FILE_KEY);
        return tempFileCache.get(key);
    }

    @Override
    public void initTempFileCache(Integer capacity) {
        //nothing
    }

    @Override
    public boolean putConvertingFileCache(String key, Integer value) {
        RMapCache<String, Integer> convertingFileCache = redissonClient.getMapCache(FILE_PREVIEW_CONVERTING_FILE);
        RLock lock = redissonClient.getLock("lock");
        lock.lock();
        try {
            //当前值已经被设置
            if (convertingFileCache.get(key)!=null){
                return false;
            }
            convertingFileCache.fastPut(key, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    public void removeConvertingFileCache(String key) {
        RMapCache<String, Integer> convertingFileCache = redissonClient.getMapCache(FILE_PREVIEW_CONVERTING_FILE);
        convertingFileCache.remove(key);
    }

    @Override
    public Integer getConvertingFileCache(String key) {
        RMapCache<String, Integer> convertingFileCache = redissonClient.getMapCache(FILE_PREVIEW_CONVERTING_FILE);
        return convertingFileCache.get(key);
    }

    @Override
    public void cleanConvertingFileCache() {
        RMapCache<String, Integer> convertingFileCache = redissonClient.getMapCache(FILE_PREVIEW_CONVERTING_FILE);
        convertingFileCache.clear();
    }

    @Override
    public void cleanConvertedCache(String key) {
        RMapCache<String, Map<String, String>> fileCache = redissonClient.getMapCache(FILE_PREVIEW_FILE_KEY);
        RMapCache<String, Map<String, String>> pdfImgCache = redissonClient.getMapCache(FILE_PREVIEW_PDF_IMGS_KEY);
        RMapCache<String, Map<String, String>> imgCache = redissonClient.getMapCache(FILE_PREVIEW_IMGS_KEY);
        RMapCache<String, String> convertedList = redissonClient.getMapCache(FILE_PREVIEW_FILE_KEY);
        String tempName = convertedList.get(key);
        log.debug("tempName:{}", tempName);
        log.debug("删除pdfImgCache,key={} 返回值={}", key, pdfImgCache.remove(ConfigConstants.getFileDir() + tempName));
        log.debug("删除压缩包内的imgCache,key={} 返回值={}", key, imgCache.remove(key));
        log.debug("删除fileCache,key={} 返回值={}", key, fileCache.remove(key));
    }
}
