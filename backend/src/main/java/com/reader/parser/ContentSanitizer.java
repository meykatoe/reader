package com.reader.parser;

import java.util.Optional;
import java.util.function.Function;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

// 內容過濾
public final class ContentSanitizer {

    public static final String ASSET_PREFIX = "asset:";

    private static final Safelist SAFELIST = new Safelist()
            .addTags("p", "br", "hr", "div", "section", "span", "blockquote", "pre", "code",
                    "h1", "h2", "h3", "h4", "h5", "h6",
                    "ul", "ol", "li", "dl", "dt", "dd",
                    "em", "i", "strong", "b", "u", "s", "sub", "sup", "small",
                    "table", "caption", "thead", "tbody", "tr", "th", "td",
                    "figure", "figcaption", "ruby", "rt", "rp", "img")
            .addAttributes("img", "src", "alt")
            .addAttributes("td", "colspan", "rowspan")
            .addAttributes("th", "colspan", "rowspan");

    private ContentSanitizer() {
    }

    // 空章節略過
    public static Optional<ParsedChapter> toChapter(
            String title, Element body, Function<Element, Optional<String>> imageResolver) {
        rewriteImages(body, imageResolver);

        Document dirty = Document.createShell("");
        dirty.body().html(body.html());
        Document clean = new Cleaner(SAFELIST).clean(dirty);
        clean.outputSettings().prettyPrint(false);

        int charCount = clean.body().text().length();
        if (charCount == 0 && clean.body().selectFirst("img") == null) {
            return Optional.empty();
        }
        return Optional.of(new ParsedChapter(title, clean.body().html(), charCount));
    }

    private static void rewriteImages(Element body, Function<Element, Optional<String>> resolver) {
        // 處理向量封面
        body.getElementsByTag("image").forEach(image -> image.tagName("img"));

        for (Element image : body.getElementsByTag("img")) {
            Optional<String> name = resolver.apply(image);
            if (name.isPresent()) {
                image.attr("src", ASSET_PREFIX + name.get());
            } else {
                image.remove();
            }
        }
        // 解開外層標籤
        body.getElementsByTag("svg").forEach(Element::unwrap);
    }
}
