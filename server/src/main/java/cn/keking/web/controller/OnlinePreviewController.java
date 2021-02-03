package cn.keking.web.controller;

import cn.keking.config.ConfigConstants;
import cn.keking.file.service.FileInfoService;
import cn.keking.model.FileAttribute;
import cn.keking.service.FileHandlerService;
import cn.keking.service.FilePreview;
import cn.keking.service.FilePreviewFactory;
import cn.keking.service.cache.CacheService;
import cn.keking.service.impl.OtherFilePreviewImpl;
import cn.keking.utils.WebUtils;
import io.mola.galimatias.GalimatiasParseException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.keking.service.FilePreview.PICTURE_FILE_PREVIEW_PAGE;

/**
 * @author yudian-it
 */
@Controller
public class OnlinePreviewController {

    public static final String BASE64_DECODE_ERROR_MSG = "Base64解码失败，请检查你的 %s 是否采用 Base64 + urlEncode 双重编码了！";
    private final Logger logger = LoggerFactory.getLogger(OnlinePreviewController.class);

    private final FilePreviewFactory previewFactory;
    private final CacheService cacheService;
    private final FileHandlerService fileHandlerService;
    private final OtherFilePreviewImpl otherFilePreview;
    private final FileInfoService fileInfoService;


    public OnlinePreviewController(FilePreviewFactory filePreviewFactory, FileHandlerService fileHandlerService, CacheService cacheService, OtherFilePreviewImpl otherFilePreview, FileInfoService fileInfoService) {
        this.previewFactory = filePreviewFactory;
        this.fileHandlerService = fileHandlerService;
        this.cacheService = cacheService;
        this.otherFilePreview = otherFilePreview;
        this.fileInfoService = fileInfoService;
    }

    @RequestMapping(value = "/onlinePreview")
    public String onlinePreview(String url, String fileId, Model model, HttpServletRequest req) {
        String fileUrl = "";
        try {
            if (url != null) {
                fileUrl = new String(Base64.decodeBase64(url));
            } else if (fileId != null) {
                fileId = new String(Base64.decodeBase64(fileId));
                fileUrl = fileInfoService.getFileUrlByIdAndStatus(fileId);
                if (fileUrl == null) {
                    model.addAttribute("msg", "未查询到文件记录!");
                    return FilePreview.FILE_NOT_FOUND_PAGE;
                }
            }
        } catch (Exception ex) {
            String errorMsg = String.format(BASE64_DECODE_ERROR_MSG, "url");
            //fileNotSupported.ftl中会访问file，因此必须添加该属性。
            model.addAttribute("file", new FileAttribute());
            return otherFilePreview.notSupportedFile(model, errorMsg);
        }
        FileAttribute fileAttribute = fileHandlerService.getFileAttribute(fileUrl, req);
        model.addAttribute("file", fileAttribute);
        FilePreview filePreview = previewFactory.get(fileAttribute);
        logger.info("预览文件url：{}，previewType：{}", fileUrl, fileAttribute.getType());
        return filePreview.filePreviewHandle(fileUrl, model, fileAttribute);
    }

    @RequestMapping(value = "/picturesPreview")
    public String picturesPreview(String urls, Model model, HttpServletRequest req) throws UnsupportedEncodingException {
        String fileUrls;
        try {
            fileUrls = new String(Base64.decodeBase64(urls));
        } catch (Exception ex) {
            String errorMsg = String.format(BASE64_DECODE_ERROR_MSG, "urls");
            return otherFilePreview.notSupportedFile(model, errorMsg);
        }
        logger.info("预览文件url：{}，urls：{}", fileUrls, urls);
        // 抽取文件并返回文件列表
        String[] images = fileUrls.split("\\|");
        List<String> imgUrls = Arrays.asList(images);
        model.addAttribute("imgUrls", imgUrls);

        String currentUrl = req.getParameter("currentUrl");
        if (StringUtils.hasText(currentUrl)) {
            String decodedCurrentUrl = new String(Base64.decodeBase64(currentUrl));
            model.addAttribute("currentUrl", decodedCurrentUrl);
        } else {
            model.addAttribute("currentUrl", imgUrls.get(0));
        }
        return PICTURE_FILE_PREVIEW_PAGE;
    }

    /**
     * 根据url获取文件内容
     * 当pdfjs读取存在跨域问题的文件时将通过此接口读取
     *
     * @param urlPath  url
     * @param response response
     */
    @RequestMapping(value = "/getCorsFile", method = RequestMethod.GET)
    public void getCorsFile(String urlPath, HttpServletResponse response) {
        logger.info("下载跨域pdf文件url：{}", urlPath);
        try {
            URL url = WebUtils.normalizedURL(urlPath);
            byte[] bytes = IOUtils.toByteArray(url);
            IOUtils.write(bytes, response.getOutputStream());
        } catch (IOException | GalimatiasParseException e) {
            logger.error("下载跨域pdf文件异常，url：{}", urlPath, e);
        }
    }

    /**
     * 通过api接口入队
     *
     * @param url 请编码后在入队
     */
    @RequestMapping("/addTask")
    @ResponseBody
    public String addQueueTask(String url) {
        logger.info("添加转码队列url：{}", url);
        cacheService.addQueueTask(url);
        return "success";
    }

    /**
     * 预加载某个文件
     *
     * @param url    需要预览的文件url
     * @param fileId 需要预览的文件id
     * @return {@link ResponseEntity}
     */
    @GetMapping("/preload")
    @ResponseBody
    public ResponseEntity<Boolean> preload(@RequestParam(value = "url", required = false) String url, @RequestParam(value = "fileId", required = false) String fileId, HttpServletRequest req) {
        //获得文件url
        String fileUrl = "";
        try {
            if (url != null) {
                fileUrl = new String(Base64.decodeBase64(url));
            } else if (fileId != null) {
                fileId = new String(Base64.decodeBase64(fileId));
                fileUrl = fileInfoService.getFileUrlByIdAndStatus(fileId);
                if (fileUrl == null) {
                    return ResponseEntity.ok(false);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.ok(false);
        }
        //根据url获得文件属性
        FileAttribute fileAttribute = fileHandlerService.getFileAttribute(fileUrl, req);
        FilePreview filePreview = previewFactory.get(fileAttribute);
        return ResponseEntity.ok(filePreview.preload(fileAttribute));
    }


    @GetMapping("/pdfImgNum")
    @ResponseBody
    public Map<String, Object> updateImgNum(@RequestParam("fileName") String fileName) {
        Map<String, Object> map = new HashMap<>(2);
        String key = ConfigConstants.getFileDir() + fileHandlerService.getConvertedFile(fileName);
        map.put("imgNum", fileHandlerService.getConvertedPdfImage(key));
        map.put("finished", fileHandlerService.pdf2jpgFinished(key));
        return map;
    }
}
