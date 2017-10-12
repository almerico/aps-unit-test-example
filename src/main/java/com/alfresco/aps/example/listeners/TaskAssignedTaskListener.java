package com.alfresco.aps.example.listeners;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

@Component("taskAssignedTaskListener")
public class TaskAssignedTaskListener implements TaskListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateTask task) {
		if(DateTime.now().getDayOfMonth() % 2 == 0){
			task.setVariable("oddOrEven", "EVENDATE");
		} else {
			task.setVariable("oddOrEven", "ODDDATE");
		}
	}

}
