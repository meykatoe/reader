package com.reader.parser.epub;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

final class EpubPaths {

    private EpubPaths() {
    }

    // 解析相對路徑
    static String resolve(String baseFile, String href) {
        int fragment = href.indexOf('#');
        String target = fragment >= 0 ? href.substring(0, fragment) : href;
        if (target.isEmpty()) {
            return baseFile;
        }
        // 保留加號
        target = URLDecoder.decode(target.replace("+", "%2B"), StandardCharsets.UTF_8);

        Deque<String> segments = new ArrayDeque<>();
        int slash = baseFile.lastIndexOf('/');
        String joined = slash >= 0 ? baseFile.substring(0, slash + 1) + target : target;
        for (String segment : joined.split("/")) {
            if (segment.equals("..")) {
                segments.pollLast();
            } else if (!segment.isEmpty() && !segment.equals(".")) {
                segments.addLast(segment);
            }
        }
        return String.join("/", segments);
    }
}
