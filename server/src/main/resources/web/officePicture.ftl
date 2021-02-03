<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <title>PDF图片预览</title>
    <script src="js/lazyload.js"></script>
    <#include "*/commonHeader.ftl">
    <style>
        body {
            background-color: #404040;
        }
        .container {
            width: 100%;
            height: 100%;
        }
        .img-area {
            text-align: center
        }

    </style>
</head>
<body>
<div class="container">
    <#list imgurls as img>
        <div class="img-area dynamic-img">
            <img class="my-photo" alt="loading"  data-src="${img}" src="images/loading.gif">
        </div>
    </#list>
</div>
<#if "false" == switchDisabled>
    <img src="images/pdf.svg" width="63" height="63" style="position: fixed; cursor: pointer; top: 40%; right: 48px; z-index: 999;" alt="使用PDF预览" title="使用PDF预览" onclick="changePreviewType('pdf')"/>
</#if>
<script>
    //当前文件名称
    window.fileName="${file.name}"
    //图片在后端是否转换完毕
    window.finshed=false;
    //图片的数量
    window.num = "${imgurls?size}"
    let tempUrl = "${imgurls[0]}";
    //图片访问地址 http://localhost:8012/xxx/
    window.imgUrl = tempUrl.substring(0,tempUrl.lastIndexOf('/')+1);
    window.onload = function () {
        /*初始化水印*/
        initWaterMark();
        checkImgs();
    };
    window.onscroll = throttle(checkImgs);
    function changePreviewType(previewType) {
        var url = window.location.href;
        if (url.indexOf("officePreviewType=image") !== -1) {
            url = url.replace("officePreviewType=image", "officePreviewType="+previewType);
        } else {
            url = url + "&officePreviewType="+previewType;
        }
        if ('allImages' === previewType) {
            window.open(url)
        } else {
            window.location.href = url;
        }
    }
</script>
</body>
</html>