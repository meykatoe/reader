package com.reader.storage;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LocalObjectStoreTest extends ObjectStoreContract {

    @Override
    protected ObjectStore store() {
        return new LocalObjectStore(directory.resolve("root"));
    }

    @Test
    void rejectsEscapingKeys() {
        assertThatThrownBy(() -> store().get("../secret"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
