package org.jabref.logic.integrity;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.jabref.logic.importer.ParserResult;
import org.jabref.logic.util.ErrorFormat;
import org.jabref.model.entry.field.Field;
import org.jabref.model.entry.field.InternalField;

public class IntegrityCheckResultErrorFormatWriter extends IntegrityCheckResultWriter {

    private final ParserResult parserResult;
    private final Path inputFile;

    public IntegrityCheckResultErrorFormatWriter(Writer writer, List<IntegrityMessage> messages, ParserResult parserResult, Path inputFile) {
        super(writer, messages);
        this.parserResult = parserResult;
        this.inputFile = inputFile;
    }

    // [impl->req~jabkit.cli.check-errorformat-output~1]
    @Override
    public void writeFindings() throws IOException {
        for (IntegrityMessage message : messages) {
            // Entry-level findings (e.g. on the citation key itself) carry only the citation key;
            // field-level findings additionally carry the field name.
            Field field = message.field();
            writer.append(ErrorFormat.line(
                    inputFile,
                    parserResult.getFieldRange(message.entry(), field),
                    message.entry().getCitationKey().orElse(message.entry().getAuthorTitleYear(5)),
                    field == InternalField.KEY_FIELD ? Optional.empty() : Optional.of(field),
                    message.message()) + "\n");
        }
    }
}
