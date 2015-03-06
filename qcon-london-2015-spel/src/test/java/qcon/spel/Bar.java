package qcon.spel;

import reactor.core.support.UUIDUtils;

import java.util.UUID;

public class Bar {

    private UUID uuid;

    public Bar() {
        this.uuid = UUIDUtils.create();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

}

