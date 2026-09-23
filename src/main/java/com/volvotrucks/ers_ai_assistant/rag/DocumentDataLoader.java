package com.volvotrucks.ers_ai_assistant.rag;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentDataLoader {

    private final VectorStore vectorStore;

    @Value("classpath:/documents/*.pdf")
    private Resource[] files;

    public DocumentDataLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void loadDocuments() {

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMaxNumChunks(400)
                .withKeepSeparator(true)
                .build();

        for (Resource file : files) {

            try {
                System.out.println("Loading: " + file.getFilename());

                TikaDocumentReader tikaReader =
                        new TikaDocumentReader(file);

                List<Document> rawDocuments =
                        tikaReader.get();

                List<Document> splitDocuments =
                        splitter.apply(rawDocuments);

                vectorStore.accept(splitDocuments);

                System.out.println(
                        "Loaded successfully: " + file.getFilename()
                );

            } catch (Exception e) {

                System.err.println(
                        "Failed to load: " + file.getFilename()
                );

                e.printStackTrace();
            }
        }

        System.out.println("All documents processed.");
    }
}