package com.reader.maintenance;

import com.reader.book.BookRepository;
import com.reader.storage.StorageProperties;
import com.reader.storage.s3.S3Clients;
import com.reader.storage.s3.S3ObjectStore;
import java.io.IOException;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

// 維運指令
@Component
@ConditionalOnProperty("reader.task")
class MaintenanceRunner implements ApplicationRunner {

    private final ConfigurableApplicationContext context;
    private final StorageProperties properties;
    private final BookRepository bookRepository;

    MaintenanceRunner(ConfigurableApplicationContext context,
                      StorageProperties properties,
                      BookRepository bookRepository) {
        this.context = context;
        this.properties = properties;
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        String task = context.getEnvironment().getRequiredProperty("reader.task");
        boolean succeeded;
        // 固定用物件端
        try (S3ObjectStore store = new S3ObjectStore(
                S3Clients.create(properties.s3()), properties.s3().bucket())) {
            succeeded = switch (task) {
                case "storage-check" -> new StorageCheck(store).run();
                case "storage-migrate" -> migrate(store);
                default -> throw new IllegalArgumentException("未知指令：" + task);
            };
        }
        System.out.println(succeeded ? "指令完成" : "指令失敗");
        int code = succeeded ? 0 : 1;
        System.exit(SpringApplication.exit(context, () -> code));
    }

    private boolean migrate(S3ObjectStore store) {
        List<StorageMigration.BookFiles> books = bookRepository.findAll().stream()
                .map(book -> new StorageMigration.BookFiles(
                        book.getStorageKey(), book.getFormat().extension()))
                .toList();
        StorageMigration.Result result =
                new StorageMigration(properties.root(), store).migrate(books);
        result.failures().forEach(failure -> System.out.println("FAIL " + failure));
        System.out.printf("書籍 %d 本，複製 %d 個，略過 %d 個，失敗 %d 個%n",
                books.size(), result.copied(), result.skipped(), result.failures().size());
        return result.succeeded();
    }
}
