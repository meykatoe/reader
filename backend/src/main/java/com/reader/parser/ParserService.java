package com.reader.parser;

import com.reader.book.BookFormat;
import com.reader.common.ErrorCode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ParserService {

    private static final Logger log = LoggerFactory.getLogger(ParserService.class);

    private final Map<BookFormat, BookParser> parsers = new EnumMap<>(BookFormat.class);

    public ParserService(List<BookParser> parserList) {
        parserList.forEach(parser -> parsers.put(parser.format(), parser));
    }

    public ParsedBook parse(BookFormat format, Path file) {
        BookParser parser = parsers.get(format);
        if (parser == null) {
            throw new ParseException(ErrorCode.FORMAT_UNSUPPORTED);
        }
        try {
            return parser.parse(file);
        } catch (ParseException ex) {
            throw ex;
        } catch (IOException | RuntimeException ex) {
            log.warn("Failed to parse {} file", format, ex);
            throw new ParseException(ErrorCode.BOOK_PARSE_FAILED);
        }
    }
}
