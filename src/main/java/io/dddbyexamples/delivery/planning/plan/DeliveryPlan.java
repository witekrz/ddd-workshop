package io.dddbyexamples.delivery.planning.plan;

import io.dddbyexamples.delivery.planning.Amounts;
import io.dddbyexamples.delivery.planning.commands.CompletePlan;
import io.dddbyexamples.delivery.planning.events.DeliveryEvents;
import io.dddbyexamples.delivery.planning.events.PlanningCompleted;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DeliveryPlan {
    private Object id;
    private PlanCompleteness planCompleteness;

    private DemandForecasting forecasting;
    private CompletenessPolicy policy;
    private DeliveryEvents events;

    void complete(CompletePlan command) {
        Amounts demandsDiff = planCompleteness.getDiff();
        if (!policy.fulfills(demandsDiff, command)) {
            return;
        }
        Amounts change = demandsDiff.filter(command.decisionToChangeDemands());
        forecasting.changeDemands(change);

        Amounts reminder = demandsDiff.filter(command.decisionToDeliverDiffNextDay());
        events.emit(new PlanningCompleted(id, reminder));
    }
}
