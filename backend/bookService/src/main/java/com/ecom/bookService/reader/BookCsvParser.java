package com.ecom.bookService.reader;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

@Service
public class BookCsvParser {
    
    private static final String[] CSV_HEADERS = {
        "isbn13", "isbn10", "title", "subtitle", "authors", "categories", 
        "thumbnail", "description", "published_year", "average_rating", 
        "num_pages", "ratings_count"
    };

    public List<BookCsvRecord> parseCsv(InputStream inputStream) throws IOException {
        List<BookCsvRecord> records = new ArrayList<>();

        try (Reader reader = new InputStreamReader(inputStream);
             CSVParser parser = new CSVParser(reader,
                 CSVFormat.DEFAULT.builder()
                     .setHeader(CSV_HEADERS)
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .setIgnoreEmptyLines(true)
                     .build())) {
            
            for (CSVRecord csvRecord : parser) {
                BookCsvRecord record = mapCsvToRecord(csvRecord);
                records.add(record);
            }
        }
        
        return records;
    }

    private BookCsvRecord mapCsvToRecord(CSVRecord csvRecord) {
        BookCsvRecord record = new BookCsvRecord();
        
        record.setIsbn13(getCsvValue(csvRecord, "isbn13"));
        record.setIsbn10(getCsvValue(csvRecord, "isbn10"));
        record.setTitle(getCsvValue(csvRecord, "title"));
        record.setSubtitle(getCsvValue(csvRecord, "subtitle"));
        record.setAuthors(getCsvValue(csvRecord, "authors"));
        record.setCategories(getCsvValue(csvRecord, "categories"));
        record.setThumbnail(getCsvValue(csvRecord, "thumbnail"));
        record.setDescription(getCsvValue(csvRecord, "description"));
        record.setPublishedYear(getCsvValue(csvRecord, "published_year"));
        record.setAverageRating(getCsvValue(csvRecord, "average_rating"));
        record.setNumPages(getCsvValue(csvRecord, "num_pages"));
        record.setRatingsCount(getCsvValue(csvRecord, "ratings_count"));

        return record;
    }

    private String getCsvValue(CSVRecord csvRecord, String column) {
        try {
            return csvRecord.get(column);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
