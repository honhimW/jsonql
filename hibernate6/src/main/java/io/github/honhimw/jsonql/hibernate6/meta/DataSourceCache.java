package io.github.honhimw.jsonql.hibernate6.meta;

import io.github.honhimw.jsonql.common.DataSourceInfo;
import io.github.honhimw.jsonql.hibernate6.DataSourceHolder;
import io.github.honhimw.jsonql.hibernate6.MetadataExtractorIntegrator;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * @author hon_him
 * @since 2024-06-28
 */

@Slf4j
public class DataSourceCache {

    private Map<Long, DataSourceHolderSupplier> dataSourceCacheMap = new ConcurrentHashMap<>();

    private final EntityManager em;

    private final DataSourceHolder embedded;

    public DataSourceCache(EntityManager em) {
        this.em = em;
        embedded = new DataSourceHolder(null, em, null, MetadataExtractorIntegrator.INSTANCE) {
            @Override
            public void close() throws Exception {
                throw new UnsupportedOperationException("current data source can't be close.");
            }
        };
    }

    public DataSourceInfo get(Long id) {
        DataSourceHolderSupplier dataSourceHolderSupplier = dataSourceCacheMap.get(id);
        if (Objects.isNull(dataSourceHolderSupplier)) {
            throw new NoSuchElementException("data-source not exists: %s".formatted(id));
        }
        return dataSourceHolderSupplier.getInfo();
    }

    public DataSourceHolder getDataSourceHolder(Long id) {
        DataSourceHolderSupplier dataSourceHolderSupplier = dataSourceCacheMap.get(id);
        if (Objects.isNull(dataSourceHolderSupplier)) {
            throw new NoSuchElementException("data-source not exists: %s".formatted(id));
        }
        return dataSourceHolderSupplier.get();
    }

    public DataSourceHolder getEmbedded() {
        return embedded;
    }

    private Map<Long, DataSourceHolderSupplier> getDataSourceCacheMap() {
        return dataSourceCacheMap;
    }

    private static class DataSourceHolderSupplier implements Supplier<DataSourceHolder> {

        private final DataSourceInfo dto;

        private final AtomicReference<DataSourceHolder> holder = new AtomicReference<>(null);

        private final AtomicLong latestInvoked = new AtomicLong(System.currentTimeMillis());

        public DataSourceHolderSupplier(DataSourceInfo dto) {
            this.dto = dto;
        }

        @Override
        public DataSourceHolder get() {
            this.latestInvoked.updateAndGet(operand -> System.currentTimeMillis());
            return holder.updateAndGet(dataSourceHolder -> {
                if (Objects.isNull(dataSourceHolder)) {
                    dataSourceHolder = DataSourceHolder.getInstance(dto);
                }
                return dataSourceHolder;
            });
        }

        public DataSourceInfo getInfo() {
            return dto;
        }

        public boolean initialized() {
            return Objects.nonNull(holder.get());
        }

        public LocalDateTime getLatest() {
            long l = latestInvoked.get();
            return LocalDateTime.from(Instant.ofEpochMilli(l));
        }

    }


}
