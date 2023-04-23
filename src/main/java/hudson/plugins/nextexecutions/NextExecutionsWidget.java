package hudson.plugins.nextexecutions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.Stapler;
import hudson.Extension;
import hudson.model.Api;
import hudson.model.Queue;
import hudson.model.Queue.Item;
import hudson.model.Queue.WaitingItem;
import hudson.model.View;
import hudson.model.TopLevelItem;
import hudson.plugins.nextexecutions.NextBuilds.DescriptorImpl;
import hudson.plugins.nextexecutions.utils.NextExecutionsUtils;
import hudson.triggers.Trigger;
import hudson.triggers.TimerTrigger;
import hudson.widgets.Widget;
import jenkins.model.ParameterizedJobMixIn;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Widget in the main sidebar with a list
 * of projects and their next scheduled 
 * build's date. The list is sorted by date.
 * 
 * It includes those in the Queue with an execution
 * delay of more than a minute.
 * 
 * @author ialbors
 *
 */
@ExportedBean
@Extension
public class NextExecutionsWidget extends Widget {
	 
	protected Class<? extends Trigger<?>> triggerClass;
	public NextExecutionsWidget() {
		triggerClass = TimerTrigger.class;
	}

	public Api getApi() { return new Api(this); }

	@Exported(name = "next_executions")
	@SuppressWarnings("rawtypes")
	public List<NextBuilds> getBuilds() {

		List<NextBuilds> nblist = new ArrayList<>();
		List<ParameterizedJobMixIn.ParameterizedJob> l = new ArrayList<>();
		
		View v = Stapler.getCurrentRequest().findAncestorObject(View.class);
		Jenkins j = Jenkins.getInstanceOrNull();

		if (j == null) {
			return nblist;
		}

		DescriptorImpl d = (DescriptorImpl)(j.getDescriptorOrDie(NextBuilds.class));
		List<ParameterizedJobMixIn.ParameterizedJob> vector = new ArrayList<ParameterizedJobMixIn.ParameterizedJob>();
		
		// Filter by view
		if(d.getFilterByView() && v != null) {
			Collection<TopLevelItem> tli = v.getItems();
			for (TopLevelItem topLevelItem : tli) {
				if(topLevelItem instanceof ParameterizedJobMixIn.ParameterizedJob){
					vector.add((ParameterizedJobMixIn.ParameterizedJob<?, ?>)topLevelItem);
				}
			}
			l = vector;
		}

		// Don't filter by view
		else {
			l = j.getItems(ParameterizedJobMixIn.ParameterizedJob.class);
		}
		
		for (ParameterizedJobMixIn.ParameterizedJob<?, ?> project: l) {
			NextBuilds nb = NextExecutionsUtils.getNextBuild(project, triggerClass);
			if(nb != null)
				nblist.add(nb);
		}

		// Get also the items in the queue but only those that have a waiting
		// period of more than a minute.
		
		// Only for this class. Not its children
		if (this.getClass() == NextExecutionsWidget.class) {
			Item[] queueItems = Queue.getInstance().getItems();
			for (Item item : queueItems) {
				if(item instanceof WaitingItem && item.task instanceof ParameterizedJobMixIn.ParameterizedJob) {
					WaitingItem waitingItem = (WaitingItem)item;
					Calendar now = Calendar.getInstance();
					long nowMilliseconds = now.getTimeInMillis();
					now.setTimeInMillis(nowMilliseconds + 60 * 1000);
					if(waitingItem.timestamp.after(now)) {
						NextBuilds nb = new NextBuilds((ParameterizedJobMixIn.ParameterizedJob<?, ?>)item.task, waitingItem.timestamp);
						nblist.add(nb);
					}
			
				}
			}
		}

		Collections.sort(nblist);
		return nblist;
		
	}
	
	public String getWidgetName() {
		return Messages.NextExec_WidgetName();

	}
	
	public String getWidgetId() {
		return "next-exec";
	}
	
	public boolean showWidget() {
		return true;
	}

	// Default displayMode will be 1
    public int getDisplayMode() {
		Jenkins j = Jenkins.getInstanceOrNull();
        DescriptorImpl d = j != null ? (DescriptorImpl)(j.getDescriptorOrDie(NextBuilds.class)) : null;

		if (d == null) {
			return 1;
		}
        return d.getDisplayMode();
    }
	
}
