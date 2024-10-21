package cn.shuyu;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import jodd.jerry.Jerry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.List;

// 爬虫工具类
@UtilityClass
public class ShuyuSpider {
    // 需要爬取的URL文件路径
    private final String sourceNeedSpiderFile = "/Users/npc/git/shuyu-spider/need_spider_svg_urls.txt";
    // 爬取结果存储路径
    private final String targetFolder = "/Users/npc/Downloads/temp";

    public static void main(String[] args) {
        // 读取需要爬取的URL列表
        List<String> urls = FileUtil.readUtf8Lines(sourceNeedSpiderFile);
        for (String url : urls) {
            Console.log("处理地址:{}", url);
            // 爬取SVG信息
            SvgInfo spiderRes = ShuyuSpider.spider(url);
            // 以标题创建目标文件夹
            File svgFolder = new File(targetFolder, spiderRes.getTitle());
            if (FileUtil.exist(svgFolder)) {
                Console.log("已经存在目录:{},不重复爬取", svgFolder);
                return;
            }
            FileUtil.mkdir(svgFolder);
            // 下载SVG文件
            File svgImage = new File(svgFolder, FileUtil.getName(spiderRes.getSvgUrl()));
            HttpUtil.downloadFile(spiderRes.getSvgUrl(), svgImage);
            Console.log("下载svg完成:{}", svgImage.getAbsolutePath());
            // 保存标签信息
            File tagFile = new File(svgFolder, FileUtil.mainName(spiderRes.getSvgUrl())+".txt");
            FileUtil.writeUtf8Lines(spiderRes.getTags(), tagFile);
            Console.log("保存tags完成:{}", tagFile.getAbsolutePath());
        }
    }

    // 爬取单个URL的SVG信息
    public SvgInfo spider(String url) {
        // 获取页面内容
        String htmlContent = HttpUtil.get(url);
        Jerry jerry = Jerry.jerry(htmlContent);
        // 提取标题
        String title = StrUtil.trim(StrUtil.split(jerry.s(".mb-3").html(), ";").getLast());
        // 构建SVG URL
        String baseUrl = "https://svgsilh.com";
        String svgUrl = StrUtil.format("{}{}", baseUrl, jerry.s("#main-svg-img").attr("src"));
        // 提取标签
        List<String> tags = CollUtil.newArrayList();
        jerry.s(".col-md-4 p a").forEach(tag -> {
            tags.add(tag.html());
        });
        return new SvgInfo(title, svgUrl, tags);
    }

    // SVG信息数据类
    @Data
    @AllArgsConstructor
    public static class SvgInfo {
        private String title;    // SVG标题
        private String svgUrl;   // SVG文件URL
        private List<String> tags; // SVG标签列表
    }
}
