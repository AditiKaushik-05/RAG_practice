
package com.QnA.Backend.Services.indexing;

import com.QnA.Backend.Services.ai.RagSettings;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CodeChunker {

    private final CodeFileFilter fileFilter;
    private final int chunkSize;
    private final int overlapSize;

    public CodeChunker(
            @Value("${app.indexing.chunk-size:800}") int chunkSize,
            @Value("${app.indexing.chunk-overlap:100}") int overlapSize,
            CodeFileFilter fileFilter) {

        // Chunk size ko minimum 200 characters rakhenge
        this.chunkSize = Math.max(200, chunkSize);

        // Overlap ko chunk size ke aadhe se zyada nahi hone denge
        this.overlapSize = Math.max(
                0,
                Math.min(overlapSize, this.chunkSize / 2)
        );

        this.fileFilter = fileFilter;
    }

    public List<Document> chunkFile(
            String repoId,
            String filePath,
            String content) {

        // Agar file empty hai toh koi chunk banane ki zarurat nahi
        if (content == null || content.isBlank()) {
            return List.of();
        }

        // File extension ke basis par programming language detect karenge
        String language = fileFilter.detectLanguage(filePath);

        // Original code ko individual lines mein divide karenge
        // Isse har chunk ki actual line numbers track kar paayenge
        List<String> lines = content.lines().toList();

        if (lines.isEmpty()) {
            return List.of();
        }

        List<Document> documents = new ArrayList<>();

        int start = 0;
        int chunkIndex = 0;

        // Jab tak poori file process nahi ho jaati, chunks banate rahenge
        while (start < lines.size()) {

            int end = start;
            int currentSize = 0;

            // Chunk mein lines add karenge jab tak approximate size limit cross na ho
            while (end < lines.size()) {

                int lineSize = lines.get(end).length() + 1;

                // Agar next line add karne se size limit cross hoti hai
                // aur chunk mein pehle se ek line hai, toh yahin stop karenge
                if (currentSize + lineSize > chunkSize
                        && end > start) {
                    break;
                }

                currentSize += lineSize;
                end++;
            }

            // Java indexing 0 se hoti hai, lekin actual file line numbers 1 se
            // Isliye start index mein 1 add karenge
            int startLine = start + 1;

            // end exclusive index hai, jo yahan actual ending line number bhi hai
            int endLine = end;

            // Selected lines ko ek single string mein combine karenge
            String chunkText = String.join(
                    "\n",
                    lines.subList(start, end)
            );

            // Chunk ki additional information metadata mein store karenge
            // Ye information embedding ke liye code text ka part nahi hogi
            Map<String, Object> metadata = new HashMap<>();

            metadata.put(RagSettings.METADATA_REPO_ID, repoId);
            metadata.put("filePath", filePath);
            metadata.put("language", language);
            metadata.put("chunkIndex", chunkIndex);
            metadata.put("startLine", startLine);
            metadata.put("endLine", endLine);

            // Code text aur uska metadata combine karke Document banayenge
            documents.add(new Document(chunkText, metadata));

            chunkIndex++;

            // Agar poori file process ho gayi toh loop stop kar denge
            if (end >= lines.size()) {
                break;
            }

            // Next chunk mein previous chunk ki kuch lines repeat karenge
            // Isse chunks ke beech context maintain rahega
            int nextStart = end;
            int overlapChars = 0;

            while (nextStart > start + 1) {

                // Previous line ka approximate character size calculate karenge
                int previousLineSize =
                        lines.get(nextStart - 1).length() + 1;

                // Agar overlap limit cross ho rahi hai toh aur lines add nahi karenge
                if (overlapChars + previousLineSize > overlapSize) {
                    break;
                }

                overlapChars += previousLineSize;
                nextStart--;
            }

            // Next chunk ki starting position update karenge
            start = nextStart;
        }

        return documents;
    }
}

