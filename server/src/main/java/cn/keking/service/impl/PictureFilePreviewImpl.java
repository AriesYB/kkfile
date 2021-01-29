package cn.keking.service.impl;

import cn.keking.model.FileAttribute;
import cn.keking.model.ReturnResponse;
import cn.keking.service.FileHandlerService;
import cn.keking.service.FilePreview;
import cn.keking.utils.DownloadUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kl on 2018/1/17.
 * Content :图片文件处理
 */
@Service
public class PictureFilePreviewImpl implements FilePreview {

    private final FileHandlerService fileHandlerService;
    private final OtherFilePreviewImpl otherFilePreview;

    public PictureFilePreviewImpl(FileHandlerService fileHandlerService, OtherFilePreviewImpl otherFilePreview) {
        this.fileHandlerService = fileHandlerService;
        this.otherFilePreview = otherFilePreview;
    }

    @Override
    public String filePreviewHandle(String url, Model model, FileAttribute fileAttribute) {
        List<String> imgUrls = new ArrayList<>();
        imgUrls.add(url);
        String fileKey = fileAttribute.getFileKey();
        List<String> zipImgUrls = fileHandlerService.getImgCache(fileKey);
        //压缩包内图片
        if (!CollectionUtils.isEmpty(zipImgUrls)) {
            imgUrls.addAll(zipImgUrls);
        }
        String filePath = DownloadUtils.getAvailableTempFilePath(fileAttribute);
        //说明该文件是(http、file、ftp)预加载或者(file、ftp)已经下载过了
        if (filePath != null) {
            String file = fileHandlerService.getRelativePath(filePath);
            imgUrls.clear();
            imgUrls.add(file);
            model.addAttribute("imgUrls", imgUrls);
            model.addAttribute("currentUrl", file);
        } else if (url != null && !url.toLowerCase().startsWith("http")) {
            // 不是http开头，浏览器不能直接访问，需下载到本地
            ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, null);
            if (response.isFailure()) {
                return otherFilePreview.notSupportedFile(model, fileAttribute, response.getMsg());
            } else {
                String file = fileHandlerService.getRelativePath(response.getContent());
                imgUrls.clear();
                imgUrls.add(file);
                model.addAttribute("imgUrls", imgUrls);
                model.addAttribute("currentUrl", file);
            }
        } else {
            model.addAttribute("imgUrls", imgUrls);
            model.addAttribute("currentUrl", url);
        }
        return PICTURE_FILE_PREVIEW_PAGE;
    }
}
