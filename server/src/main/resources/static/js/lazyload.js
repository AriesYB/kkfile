function isInSight(el) {
    var bound = el.getBoundingClientRect();
    var clientHeight = window.innerHeight;
    //只考虑向下滚动加载
    //const clientWidth=window.innerWeight;
    return bound.top <= clientHeight + 100;
}

var index = 0;

function checkImgs() {
    var imgs = document.querySelectorAll('.my-photo');
    loadImgMore(imgs.length);
    for (var i = index; i < imgs.length; i++) {
        if (isInSight(imgs[i])) {
            loadImg(imgs[i]);
            index = i;
        }
    }
}

function loadImg(el) {
    var source = el.getAttribute("data-src");
    if (el.src !== source) {
        //只设置未设置src的图片
        el.src = source;
    }
}

// var mustRun = 500
// function throttle(fn, mustRun) {
//     var timer = null;
//     var previous = null;
//     return function() {
//         var now = new Date();
//         var context = this;
//         var args = arguments;
//         if (!previous) {
//             previous = now;
//         }
//         var remaining = now - previous;
//         if (mustRun && remaining >= mustRun) {
//             fn.apply(context, args);
//             previous = now;
//         }
//     }
// }


function throttle(fn) {
    var timer = null;
    var previous = null;
    return function () {
        var now = new Date();
        var context = this;
        var args = arguments;
        if (!previous) {
            previous = now;
        }
        var remaining = now - previous;
        setTimeout(refresh(fn, remaining, context, args, previous, now));
    }
}

function refresh(fn, remaining, context, args, previous, now) {
    if (remaining >= 500) {
        fn.apply(context, args);
        previous = now;
    }
}

//加载更多图片区
function loadImgMore(length) {
    //当图片数更新时，添加新的图片区域
    if (length < window.num) {
        let imgs = $('.dynamic-img:last');
        let tempImg = $(imgs).clone();
        let temp = $(tempImg[0].children[0]);
        temp.attr('data-src', window.imgUrl + (length + 1).toString() + '.jpg');
        imgs.after(tempImg);
    }
    if (length == window.num) {
        updateImgNum();
    }
}

//ajax请求状态，当翻页过快时会不断发起更新图片数请求，产生资源浪费。故使更新图片数请求同一时刻只存在一次。
var ajaxStatus = false;

//更新图片数
function updateImgNum() {
    if (ajaxStatus){
        return;
    }
    if (!window.finshed) {
        ajaxStatus = true;
        $.ajax({
            type: "GET",
            url: "pdfImgNum",
            data: "fileName=" + window.fileName,
            success: function (data) {
                if (data.imgNum != null && data.imgNum>window.num) {
                    window.num = data.imgNum;
                }
                if (data.finished != null) {
                    window.finshed = data.finished;
                }
                ajaxStatus = false;
            }, error: function () {
                console.log("图片继续加载失败!");
                ajaxStatus = false;
            }
        })
    }
}
