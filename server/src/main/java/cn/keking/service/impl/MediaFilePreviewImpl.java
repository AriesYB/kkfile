package cn.keking.service.impl;

import cn.keking.model.FileAttribute;
import cn.keking.model.ReturnResponse;
import cn.keking.service.FileHandlerService;
import cn.keking.service.FilePreview;
import cn.keking.utils.DownloadUtils;
import cn.keking.web.filter.BaseUrlFilter;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 * @author : kl
 * @authorboke : kailing.pub
 * @create : 2018-03-25 上午11:58
 * @description:
 **/
@Service
public class MediaFilePreviewImpl implements FilePreview {

    private final FileHandlerService fileHandlerService;
    private final OtherFilePreviewImpl otherFilePreview;

    public MediaFilePreviewImpl(FileHandlerService fileHandlerService, OtherFilePreviewImpl otherFilePreview) {
        this.fileHandlerService = fileHandlerService;
        this.otherFilePreview = otherFilePreview;
    }

    @Override
    public String filePreviewHandle(String url, Model model, FileAttribute fileAttribute) {
        //获取临时文件
        String filePath = DownloadUtils.getAvailableTempFilePath(fileAttribute);
        if (filePath != null) {
            model.addAttribute("mediaUrl", BaseUrlFilter.getBaseUrl() + fileHandlerService.getRelativePath(filePath));
        } else if (url != null && !url.toLowerCase().startsWith("http")) {
            // 不是http开头，浏览器不能直接访问，需下载到本地
            ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, fileAttribute.getName());
            if (response.isFailure()) {
                return otherFilePreview.notSupportedFile(model, fileAttribute, response.getMsg());
            } else {
                model.addAttribute("mediaUrl", BaseUrlFilter.getBaseUrl() + fileHandlerService.getRelativePath(response.getContent()));
            }
        } else {
            model.addAttribute("mediaUrl", url);
        }
        return MEDIA_FILE_PREVIEW_PAGE;
    }

    @Override
    public boolean preload(FileAttribute fileAttribute) {
        //媒体文件预加载只需要下载到临时文件目录
        //获取临时文件
        String filePath = DownloadUtils.getAvailableTempFilePath(fileAttribute);
        if (filePath == null) {
            ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, fileAttribute.getName());
            if (response.isFailure()) {
                return false;
            }
            filePath = response.getContent();
        }
        return filePath != null;
    }
}
