package io.dddbyexamples.delivery.planning.delivery;

import io.dddbyexamples.delivery.planning.Amounts;
import io.dddbyexamples.delivery.planning.commands.EditDelivery;
import io.dddbyexamples.delivery.planning.events.DeliveryChanged;
import io.dddbyexamples.delivery.planning.events.DeliveryEvents;

import java.time.LocalDateTime;

public class Delivery {
    private String id;
    private LocalDateTime time;
    private TransportType type;
    private Payload payload;

    private PayloadCapacityPolicy payloadCapacityPolicy;
    private DeliveryEvents events;

    public Delivery(String id, PayloadCapacityPolicy payloadCapacityPolicy, DeliveryEvents events) {
        this.id = id;
        this.payloadCapacityPolicy = payloadCapacityPolicy;
        this.events = events;
        time = LocalDateTime.MIN;
        type = TransportType.unspecified();
        payload = Payload.empty();
    }

    public Amounts editDelivery(EditDelivery command) {
        Amounts exceeded = payloadCapacityPolicy.calculateExceeded(
                command.getType(),
                command.getPayload().amountOfUnitTypes()
        );
        if (capacity(exceeded)) {
            return exceeded;
        }

        Amounts diff = command.getPayload().amountOfProducts()
                .diff(this.payload.amountOfProducts());

        this.time = command.getTime();
        this.type = command.getType();
        this.payload = command.getPayload();

        if (!diff.isEmpty()) {
            events.emit(new DeliveryChanged(id, diff));
        }
        return Amounts.empty();
    }

    public void cancelDelivery() {
        Amounts diff = this.payload.amountOfProducts().negative();
        this.type = TransportType.unspecified();
        this.payload = Payload.empty();
        events.emit(new DeliveryChanged(id, diff));
    }

    private boolean capacity(Amounts exceeded) {
        return !exceeded.isEmpty();
    }
}
