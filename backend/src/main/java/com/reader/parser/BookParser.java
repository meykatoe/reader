package com.reader.parser;

import com.reader.book.BookFormat;
import java.io.IOException;
import java.nio.file.Path;

public interface BookParser {

    BookFormat format();

    ParsedBook parse(Path file) throws IOException;
}
