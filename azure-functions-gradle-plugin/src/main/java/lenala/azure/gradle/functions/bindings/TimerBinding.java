package lenala.azure.gradle.functions.bindings;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.microsoft.azure.functions.annotation.TimerTrigger;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TimerBinding extends BaseBinding {
    public static final String TIMER_TRIGGER = "timerTrigger";

    private String schedule;

    public TimerBinding(final TimerTrigger timerTrigger) {
        super(timerTrigger.name(), TIMER_TRIGGER, Direction.IN, timerTrigger.dataType());

        schedule = timerTrigger.schedule();
    }

    @JsonGetter
    public String getSchedule() {
        return schedule;
    }
}
