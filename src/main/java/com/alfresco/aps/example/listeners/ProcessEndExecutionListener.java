package com.alfresco.aps.example.listeners;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;
import org.joda.time.DateTime;

@Component("processEndExecutionListener")
public class ProcessEndExecutionListener implements ExecutionListener {
	
	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		if(DateTime.now().getDayOfMonth() % 2 == 0){
			execution.setVariable("oddOrEven", "EVEN");
		} else {
			execution.setVariable("oddOrEven", "ODD");
		}
	}

}
