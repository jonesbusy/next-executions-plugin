package hudson.plugins.nextexecutions.columns;

import hudson.Extension;
import io.jenkins.plugins.extended_timer_trigger.ExtendedTimerTrigger;
import org.kohsuke.stapler.DataBoundConstructor;

public class ExtendedNextExecutionColumn extends NextExecutionColumn {

    @DataBoundConstructor
    public ExtendedNextExecutionColumn() {
        triggerClass = ExtendedTimerTrigger.class;
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends NextExecutionColumn.DescriptorImpl {

        @Override
        public String getDisplayName() {
            return Messages.ParameterizedExecutions_ColumnName();
        }
    }
}
