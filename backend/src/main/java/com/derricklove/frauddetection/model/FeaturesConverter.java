package com.derricklove.frauddetection.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JPA {@link AttributeConverter} that serializes a {@code List<Double>} to a
 * comma-separated {@code String} for storage and reconstructs the list on read.
 *
 * <p>We chose this representation (rather than a separate child table or a
 * JSON column) to keep the schema portable — it works on H2, Postgres, and
 * MySQL without dialect-specific JSON support — and because the feature list
 * is fixed-length (28 PCA components) and only ever read/written as a whole
 * alongside its parent {@link Transaction}.</p>
 *
 * <p>{@code autoApply = false} so this converter only kicks in on fields
 * explicitly annotated with {@code @Convert}, avoiding accidental conversion
 * of unrelated {@code List<Double>} fields elsewhere.</p>
 */
@Converter(autoApply = false)
public class FeaturesConverter implements AttributeConverter<List<Double>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<Double> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        // Use String.valueOf() so any Double (including special values) is
        // round-tripped via its canonical textual form.
        return attribute.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public List<Double> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }
        List<Double> result = new ArrayList<>();
        for (String part : dbData.split(DELIMITER)) {
            result.add(Double.parseDouble(part.trim()));
        }
        return result;
    }
}
