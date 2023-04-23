package hudson.plugins.nextexecutions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.ComputerPanelBox;
import hudson.plugins.nextexecutions.utils.NextExecutionsUtils;
import hudson.triggers.TimerTrigger;


/**
 * Widget in the {@link Computer} page's sidebar with a list
 * of projects (tied to that Computer) and their next scheduled 
 * build's date. The list is sorted by date.
 * 
 * @author ialbors
 *
 */
@Extension
@SuppressWarnings("rawtypes")
public class NextExecutionsComputerWidget extends ComputerPanelBox {
	
	public List<NextBuilds> getBuilds() {
		List<NextBuilds> nblist = new ArrayList<>();

		List<AbstractProject> l = getComputer().getTiedJobs();
		
		for (AbstractProject<?, ?> project: l) {
			NextBuilds nb = NextExecutionsUtils.getNextBuild(project, TimerTrigger.class);
			if(nb != null)
				nblist.add(nb);
		}
		Collections.sort(nblist);
		return nblist;
		
	}
}
