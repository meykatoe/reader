package com.reader.parser.epub;

import com.reader.parser.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

// 套件描述檔
record EpubPackage(
        String title,
        String author,
        List<String> spine,
        Map<String, String> tocTitles,
        String coverPath) {

    private static final String CONTAINER = "META-INF/container.xml";

    private record Item(String path, String mediaType, String properties) {
    }

    static EpubPackage read(EpubArchive archive) {
        Element rootFile = archive.readXml(CONTAINER).selectFirst("rootfile[full-path]");
        if (rootFile == null) {
            throw new ParseException("EPUB 結構不完整");
        }
        String opfPath = rootFile.attr("full-path");
        Document opf = archive.readXml(opfPath);
        Map<String, Item> manifest = readManifest(opf, opfPath);

        List<String> spine = new ArrayList<>();
        for (Element reference : opf.select("spine > itemref, opf|spine > opf|itemref")) {
            Item item = manifest.get(reference.attr("idref"));
            if (item != null) {
                spine.add(item.path());
            }
        }

        return new EpubPackage(
                firstText(opf, "dc|title"),
                firstText(opf, "dc|creator"),
                spine,
                readToc(archive, opf, manifest),
                findCover(opf, manifest).orElse(null));
    }

    private static Map<String, Item> readManifest(Document opf, String opfPath) {
        Map<String, Item> manifest = new LinkedHashMap<>();
        for (Element item : opf.select("manifest > item, opf|manifest > opf|item")) {
            manifest.put(item.attr("id"), new Item(
                    EpubPaths.resolve(opfPath, item.attr("href")),
                    item.attr("media-type"),
                    item.attr("properties")));
        }
        return manifest;
    }

    private static String firstText(Document opf, String selector) {
        Element element = opf.selectFirst(selector);
        return element == null ? null : element.text();
    }

    // 新版優先
    private static Map<String, String> readToc(
            EpubArchive archive, Document opf, Map<String, Item> manifest) {
        Map<String, String> titles = new LinkedHashMap<>();
        manifest.values().stream()
                .filter(item -> item.properties().contains("nav") && archive.exists(item.path()))
                .findFirst()
                .ifPresent(nav -> readNav(archive, nav.path(), titles));
        if (!titles.isEmpty()) {
            return titles;
        }
        manifest.values().stream()
                .filter(item -> item.mediaType().equals("application/x-dtbncx+xml")
                        && archive.exists(item.path()))
                .findFirst()
                .ifPresent(ncx -> readNcx(archive, ncx.path(), titles));
        return titles;
    }

    private static void readNav(EpubArchive archive, String path, Map<String, String> titles) {
        Document nav = archive.readXml(path);
        Element toc = nav.selectFirst("nav[epub:type=toc]");
        if (toc == null) {
            toc = nav.selectFirst("nav");
        }
        if (toc != null) {
            toc.select("a[href]").forEach(link ->
                    addTitle(titles, path, link.attr("href"), link.text()));
        }
    }

    private static void readNcx(EpubArchive archive, String path, Map<String, String> titles) {
        for (Element point : archive.readXml(path).select("navPoint")) {
            Element label = point.selectFirst("> navLabel > text");
            Element content = point.selectFirst("> content");
            if (label != null && content != null) {
                addTitle(titles, path, content.attr("src"), label.text());
            }
        }
    }

    private static void addTitle(Map<String, String> titles, String base, String href, String text) {
        if (!text.isBlank()) {
            titles.putIfAbsent(EpubPaths.resolve(base, href), text.strip());
        }
    }

    private static Optional<String> findCover(Document opf, Map<String, Item> manifest) {
        Optional<Item> declared = manifest.values().stream()
                .filter(item -> item.properties().contains("cover-image"))
                .findFirst();
        if (declared.isEmpty()) {
            Element meta = opf.selectFirst("meta[name=cover]");
            declared = Optional.ofNullable(meta).map(m -> manifest.get(m.attr("content")));
        }
        return declared.map(Item::path);
    }
}
