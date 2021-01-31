package cn.keking.service.impl;

import cn.keking.config.ConfigConstants;
import cn.keking.model.FileAttribute;
import cn.keking.model.ReturnResponse;
import cn.keking.service.FileHandlerService;
import cn.keking.service.FilePreview;
import cn.keking.utils.DownloadUtils;
import cn.keking.web.filter.BaseUrlFilter;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

/**
 * Created by kl on 2018/1/17.
 * Content :处理pdf文件
 */
@Service
public class PdfFilePreviewImpl implements FilePreview {

    private final FileHandlerService fileHandlerService;
    private final OtherFilePreviewImpl otherFilePreview;
    private static final String FILE_DIR = ConfigConstants.getFileDir();

    public PdfFilePreviewImpl(FileHandlerService fileHandlerService, OtherFilePreviewImpl otherFilePreview) {
        this.fileHandlerService = fileHandlerService;
        this.otherFilePreview = otherFilePreview;
    }

    @Override
    public String filePreviewHandle(String url, Model model, FileAttribute fileAttribute) {
        String fileName = fileAttribute.getName();
        String officePreviewType = fileAttribute.getOfficePreviewType();
        String baseUrl = BaseUrlFilter.getBaseUrl();
        String pdfName = fileName.substring(0, fileName.lastIndexOf(".") + 1) + "pdf";
        String outFilePath = FILE_DIR + pdfName;
        if (OfficeFilePreviewImpl.OFFICE_PREVIEW_TYPE_IMAGE.equals(officePreviewType) || OfficeFilePreviewImpl.OFFICE_PREVIEW_TYPE_ALL_IMAGES.equals(officePreviewType)) {
            //获取临时文件或者下载文件
            String filePath = DownloadUtils.getAvailableTempFilePath(fileAttribute);
            if (filePath != null) {
                outFilePath = filePath;
            } else {
                ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, fileName);
                if (response.isFailure()) {
                    return otherFilePreview.notSupportedFile(model, fileAttribute, response.getMsg());
                }
                outFilePath = response.getContent();
            }
            //如果文件未转换或者未开启缓存
            if (!fileHandlerService.listConvertedFiles().containsKey(pdfName) || !ConfigConstants.isCacheEnabled()) {
                if (ConfigConstants.isCacheEnabled()) {
                    // 加入缓存
                    fileHandlerService.addConvertedFile(fileName, pdfName);
                }
            }
            //此方法会获取已转换的图片缓存
            List<String> imageUrls = fileHandlerService.pdf2jpg(outFilePath, pdfName, baseUrl);
            if (imageUrls == null || imageUrls.size() < 1) {
                return otherFilePreview.notSupportedFile(model, fileAttribute, "pdf转图片异常，请联系管理员");
            }
            model.addAttribute("imgurls", imageUrls);
            model.addAttribute("currentUrl", imageUrls.get(0));
            if (OfficeFilePreviewImpl.OFFICE_PREVIEW_TYPE_IMAGE.equals(officePreviewType)) {
                return OFFICE_PICTURE_FILE_PREVIEW_PAGE;
            } else {
                return PICTURE_FILE_PREVIEW_PAGE;
            }
        } else {
            //不输出(图片)直接输出pdf文档
            //获取临时文件
            String filePath = DownloadUtils.getAvailableTempFilePath(fileAttribute);
            if (filePath != null) {
                model.addAttribute("pdfUrl", fileHandlerService.getRelativePath(filePath));
            } else if (url != null && !url.toLowerCase().startsWith("http")) {
                // 不是http开头，浏览器不能直接访问，需下载到本地
                if (!fileHandlerService.listConvertedFiles().containsKey(pdfName) || !ConfigConstants.isCacheEnabled()) {
                    ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, pdfName);
                    if (response.isFailure()) {
                        return otherFilePreview.notSupportedFile(model, fileAttribute, response.getMsg());
                    }
                    model.addAttribute("pdfUrl", fileHandlerService.getRelativePath(response.getContent()));
                    if (ConfigConstants.isCacheEnabled()) {
                        // 加入缓存
                        fileHandlerService.addConvertedFile(fileName, pdfName);
                    }
                } else {
                    model.addAttribute("pdfUrl", pdfName);
                }
            } else {
                model.addAttribute("pdfUrl", url);
            }
        }
        return PDF_FILE_PREVIEW_PAGE;
    }


    @Override
    public boolean preload(FileAttribute fileAttribute) {
        //pdf文件预加载：下载、转换为图片
        String fileName = fileAttribute.getName();
        String baseUrl = BaseUrlFilter.getBaseUrl();
        String pdfName = fileName.substring(0, fileName.lastIndexOf(".") + 1) + "pdf";
        //获取临时文件
        String filePath = DownloadUtils.getAvailableTempFilePath(fileAttribute);
        //没有临时文件或者临时文件未转化时进行转换
        if (filePath == null || fileHandlerService.getConvertedFile(pdfName) == null) {
            ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, fileName);
            if (response.isFailure()) {
                return false;
            }
            filePath = response.getContent();
            if (ConfigConstants.isCacheEnabled()) {
                // 加入缓存
                fileHandlerService.addConvertedFile(fileName, pdfName);
            }
        }
        //此方法会获取已转换的图片缓存
        List<String> imageUrls = fileHandlerService.pdf2jpg(filePath, pdfName, baseUrl);
        return imageUrls != null && imageUrls.size() >= 1;
    }
}
