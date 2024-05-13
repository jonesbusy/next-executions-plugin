package hudson.plugins.nextexecutions.utils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.plugins.nextexecutions.NextBuilds;
import hudson.scheduler.CronTab;
import hudson.scheduler.CronTabList;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import io.jenkins.plugins.extended_timer_trigger.ExtendedCronTab;
import io.jenkins.plugins.extended_timer_trigger.ExtendedCronTabList;
import io.jenkins.plugins.extended_timer_trigger.ExtendedTimerTrigger;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import jenkins.model.ParameterizedJobMixIn;

@SuppressWarnings({"rawtypes", "unchecked", "java:S3011"})
public class ExtendedNextExecutionsUtils {

    private ExtendedNextExecutionsUtils() {}

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE")
    public static NextBuilds getNextBuild(
            ParameterizedJobMixIn.ParameterizedJob project, Class<? extends Trigger> triggerClass) {
        Calendar cal = null;
        TimeZone timezone = null;

        // Skip all disabled jobs
        try {
            Method isDisabledMethod = project.getClass().getMethod("isDisabled");
            isDisabledMethod.setAccessible(true);
            if ((Boolean) isDisabledMethod.invoke(project)) {
                return null;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Do nothing
        }

        Map<TriggerDescriptor, Trigger<?>> triggers = project.getTriggers();
        Iterator<Map.Entry<TriggerDescriptor, Trigger<?>>> iterator =
                triggers.entrySet().iterator();
        while (iterator.hasNext()) {
            Trigger trigger = iterator.next().getValue();
            if (trigger.getClass().equals(triggerClass) && triggerClass.equals(ExtendedTimerTrigger.class)) {
                try {
                    Field triggerTabsField = ExtendedTimerTrigger.class.getDeclaredField("extendedCronTabList");
                    triggerTabsField.setAccessible(true);

                    ExtendedCronTabList parameterizedCronTabList = (ExtendedCronTabList) triggerTabsField.get(trigger);

                    Field crontablistTabsField = ExtendedCronTabList.class.getDeclaredField("cronTabs");
                    crontablistTabsField.setAccessible(true);
                    List<ExtendedCronTab> parameterizedCrons =
                            (ArrayList<ExtendedCronTab>) crontablistTabsField.get(parameterizedCronTabList);

                    for (ExtendedCronTab parameterizedCron : parameterizedCrons) {
                        Field crontablistField = ExtendedCronTab.class.getDeclaredField("cron");
                        crontablistField.setAccessible(true);
                        CronTabList list = (CronTabList) crontablistField.get(parameterizedCron);
                        Field crontablistTabsField1 = CronTabList.class.getDeclaredField("tabs");
                        crontablistTabsField1.setAccessible(true);
                        List<CronTab> crons = (Vector<CronTab>) crontablistTabsField1.get(list);
                        for (CronTab cronTab : crons) {
                            timezone = cronTab.getTimeZone() != null ? cronTab.getTimeZone() : TimeZone.getDefault();
                            Calendar now = new GregorianCalendar(timezone);
                            cal = (cal == null || cal.compareTo(cronTab.ceil(now)) > 0) ? cronTab.ceil(now) : cal;
                        }
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // Do nothing
                }
            }
        }
        if (cal != null) {
            return new NextBuilds(project, cal);
        } else {
            return null;
        }
    }
}
